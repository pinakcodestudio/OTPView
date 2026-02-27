package com.pcs.otpview.internal.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * All OTP business logic.
 *
 * ── BACKSPACE ────────────────────────────────────────────────────────────────
 * Handled via the sentinel approach in OtpInputField.
 * Case A (clear digit)  → OnEnterNumber(null, index)
 * Case B (empty field)  → OnKeyboardBack
 *
 * ── CUSTOM POSITION INPUT ────────────────────────────────────────────────────
 * onFocusChanged does NOT redirect. It only records the index.
 *
 * The redirect is done synchronously in OtpScreen via Modifier.clickable on
 * each box. When a box past firstEmptyIndex is tapped, clickable calls
 * requestFocus(firstEmpty) immediately — before the keyboard attaches.
 * This is synchronous, so there is no async race condition.
 *
 * WHY NOT IN onFocusChanged:
 * Redirecting there sets focusedIndex → LaunchedEffect fires requestFocus
 * on the next frame (async). If the user types before that frame executes,
 * the digit hits the wrong box. onEnterNumber then changes focusedIndex again,
 * cancelling the pending LaunchedEffect before requestFocus ran. With rapid
 * typing, multiple state changes collapse into one frame and LaunchedEffect
 * only fires for the final value — skipping intermediate boxes entirely.
 * This produced the "stuck at box 3" symptom in the video.
 *
 * ── DIGIT PLACEMENT ──────────────────────────────────────────────────────────
 * onEnterNumber always places digits at firstEmptyIndex() regardless of which
 * box physically fired the event. This guarantees left-to-right fill order
 * even if the physical keyboard briefly lags behind the logical focus.
 */
internal class OtpStateHolder(
    private val otpLength        : Int,
    private val isSecureInput    : Boolean,
    private val timerTotalSeconds: Int,
    private val scope            : CoroutineScope,
    private val onVerifyInternal : suspend (otp: String) -> OtpValidationState,
    private val onResendCallback : () -> Unit,
    initialState                 : OtpState,
) {
    var otpState        by mutableStateOf(initialState)
        private set

    var validationState by mutableStateOf<OtpValidationState>(OtpValidationState.Idle)
        private set

    private var verifyJob: Job? = null
    private var timerJob : Job? = null

    init {
        if (!otpState.timerExpired) {
            startTimer(otpState.timerRemainingSeconds)
        }
    }

    fun onAction(action: OtpAction) {
        when (action) {
            is OtpAction.OnChangeFieldFocused -> onFocusChanged(action.index)
            is OtpAction.OnEnterNumber        -> onEnterNumber(action.number, action.index)
            is OtpAction.OnPaste              -> onPaste(action.pastedText)
            OtpAction.OnKeyboardBack          -> onKeyboardBack()
            OtpAction.OnResend                -> onResend()
            OtpAction.OnTimerTick             -> onTimerTick()
        }
    }

    // ── Focus ─────────────────────────────────────────────────────────────────

    /**
     * Records which box has focus. No redirect logic here.
     * Redirect is done synchronously in OtpScreen's clickable interceptor.
     */
    private fun onFocusChanged(touchedIndex: Int) {
        if (otpState.focusedIndex != touchedIndex) {
            otpState = otpState.copy(focusedIndex = touchedIndex)
        }
    }

    // ── Digit entry ───────────────────────────────────────────────────────────

    private fun onEnterNumber(number: Int?, index: Int) {
        if (number == null) {
            // Backspace Case A: clear digit at index, stay here
            if (validationState is OtpValidationState.Error) {
                validationState = OtpValidationState.Idle
            }
            otpState = otpState.copy(
                code         = otpState.code.toMutableList().apply { set(index, null) },
                focusedIndex = index,
            )
            return
        }

        val currentCode = otpState.code
        // If tapped box is already filled, honour the overwrite position.
        // Otherwise always fill at firstEmptyIndex — enforces left-to-right order.
        val targetIndex = when {
            currentCode[index] != null -> index
            else                       -> firstEmptyIndex() ?: index
        }

        val newCode   = currentCode.toMutableList().apply { set(targetIndex, number) }
        val nextFocus = firstEmptyIndexAfterFill(newCode) ?: targetIndex

        if (validationState is OtpValidationState.Error) {
            validationState = OtpValidationState.Idle
        }

        otpState = otpState.copy(code = newCode, focusedIndex = nextFocus)

        if (newCode.none { it == null }) {
            submitOtp(newCode.joinToString("") { it.toString() })
        }
    }

    // ── Backspace Case B ──────────────────────────────────────────────────────

    private fun onKeyboardBack() {
        val current  = otpState.focusedIndex ?: return
        val previous = (current - 1).coerceAtLeast(0)
        otpState = otpState.copy(
            code         = otpState.code.toMutableList().apply { set(previous, null) },
            focusedIndex = previous,
        )
    }

    // ── Paste ─────────────────────────────────────────────────────────────────

    private fun onPaste(pastedText: String) {
        val digits = pastedText.filter { it.isDigit() }.take(otpLength)
        if (digits.isEmpty()) return

        val newCode = List(otpLength) { i -> digits.getOrNull(i)?.digitToInt() }
        validationState = OtpValidationState.Idle
        otpState = otpState.copy(
            code         = newCode,
            focusedIndex = (digits.length - 1).coerceIn(0, otpLength - 1),
        )
        if (newCode.none { it == null }) {
            submitOtp(newCode.joinToString("") { it.toString() })
        }
    }

    // ── Resend ────────────────────────────────────────────────────────────────

    private fun onResend() {
        verifyJob?.cancel()
        validationState = OtpValidationState.Idle
        otpState = OtpState(
            otpLength             = otpLength,
            code                  = List(otpLength) { null },
            focusedIndex          = 0,
            isSecureInput         = isSecureInput,
            isEnabled             = true,
            timerRemainingSeconds = timerTotalSeconds,
        )
        startTimer(timerTotalSeconds)
        onResendCallback()
    }

    // ── Timer ─────────────────────────────────────────────────────────────────

    private fun onTimerTick() {
        val current = otpState.timerRemainingSeconds
        if (current > 0) {
            otpState = otpState.copy(timerRemainingSeconds = current - 1)
        }
    }

    private fun startTimer(fromSeconds: Int) {
        timerJob?.cancel()
        if (fromSeconds <= 0) return
        timerJob = scope.launch {
            var remaining = fromSeconds
            while (remaining > 0) {
                delay(1_000)
                remaining--
                otpState = otpState.copy(timerRemainingSeconds = remaining)
            }
        }
    }

    // ── Verification ──────────────────────────────────────────────────────────

    private fun submitOtp(otp: String) {
        verifyJob?.cancel()
        timerJob?.cancel()
        verifyJob = scope.launch {
            validationState = OtpValidationState.Loading
            otpState = otpState.copy(isEnabled = false)

            val result = runCatching { onVerifyInternal(otp) }
                .getOrElse { OtpValidationState.Error("Something went wrong. Please try again.") }

            validationState = result

            when (result) {
                is OtpValidationState.Error -> {
                    otpState = otpState.copy(
                        code         = List(otpLength) { null },
                        focusedIndex = 0,
                        isEnabled    = true,
                    )
                    if (!otpState.timerExpired) {
                        startTimer(otpState.timerRemainingSeconds)
                    }
                }
                OtpValidationState.Success -> { }
                else -> { }
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun firstEmptyIndex(): Int? =
        otpState.code.indexOfFirst { it == null }.takeIf { it >= 0 }

    private fun firstEmptyIndexAfterFill(newCode: List<Int?>): Int? =
        newCode.indexOfFirst { it == null }.takeIf { it >= 0 }
}

// ── Factory ───────────────────────────────────────────────────────────────────

@Composable
internal fun rememberOtpStateHolder(
    otpLength        : Int,
    isSecureInput    : Boolean,
    timerTotalSeconds: Int,
    onVerify         : suspend (otp: String) -> OtpValidationState,
    onResend         : () -> Unit,
): OtpStateHolder {
    val scope = rememberCoroutineScope()

    return rememberSaveable(
        otpLength, isSecureInput, timerTotalSeconds,
        saver = androidx.compose.runtime.saveable.Saver(
            save    = { it.otpState },
            restore = { saved ->
                OtpStateHolder(
                    otpLength         = otpLength,
                    isSecureInput     = isSecureInput,
                    timerTotalSeconds = timerTotalSeconds,
                    scope             = scope,
                    onVerifyInternal  = onVerify,
                    onResendCallback  = onResend,
                    initialState      = saved,
                )
            }
        )
    ) {
        OtpStateHolder(
            otpLength         = otpLength,
            isSecureInput     = isSecureInput,
            timerTotalSeconds = timerTotalSeconds,
            scope             = scope,
            onVerifyInternal  = onVerify,
            onResendCallback  = onResend,
            initialState      = OtpState(
                otpLength             = otpLength,
                code                  = List(otpLength) { null },
                focusedIndex          = null,
                isSecureInput         = isSecureInput,
                isEnabled             = true,
                timerRemainingSeconds = timerTotalSeconds,
            ),
        )
    }
}