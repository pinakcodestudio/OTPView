package com.pcs.otpview.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.pcs.otpview.internal.model.OtpValidationState
import com.pcs.otpview.internal.model.rememberOtpStateHolder
import com.pcs.otpview.internal.ui.OtpScreen

/**
 * Production-ready OTP / PIN input component for Jetpack Compose.
 *
 * A single composable that covers every use-case through configuration.
 * No separate composables are needed for different layouts — all UI variations
 * are expressed through [config], [theme], [typography], [spacing], and
 * [resendConfig].
 *
 * ── FEATURES ─────────────────────────────────────────────────────────────────
 * • Auto-focus on first field, keyboard opens immediately
 * • Smart focus routing as digits are entered
 * • Correct two-scenario backspace chain (see below)
 * • Clipboard paste and SMS auto-fill support
 * • Shake animation on verification failure
 * • Auto-submit when last digit is entered
 * • Countdown timer with configurable duration
 * • Every UI section individually visible/hidden
 * • Every color, typography, spacing independently configurable
 *
 * ── BACKSPACE BEHAVIOUR ──────────────────────────────────────────────────────
 * Scenario A — field HAS a digit:
 *   The system keyboard's backspace fires BasicTextField.onValueChange("").
 *   The digit is cleared. Focus stays on the same field for the next press.
 *
 * Scenario B — field IS EMPTY:
 *   The system keyboard's backspace fires onKeyEvent(KEYCODE_DEL).
 *   The previous field is cleared and focus moves back one step.
 *
 * Result: pressing backspace on 12345_ removes 5, then 4, then 3, etc.
 * System Back (nav gesture / button) is handled by BackHandler in the host
 * screen and is completely independent of digit backspace.
 *
 * ── MINIMAL USAGE ────────────────────────────────────────────────────────────
 * ```kotlin
 * OTPView(
 *     otpLength = 6,
 *     onVerify  = { otp ->
 *         val ok = apiService.verify(otp)
 *         if (ok) OtpResult.Success else OtpResult.Failure("Wrong OTP")
 *     },
 *     onSuccess = { navController.navigate("home") }
 * )
 * ```
 *
 * ── FULL CUSTOMISATION ────────────────────────────────────────────────────────
 * ```kotlin
 * OTPView(
 *     otpLength    = 6,
 *     title        = "Verify your number",
 *     subtitle     = "Sent to +91 98765 43210",
 *     config       = OtpConfig(isSecureInput = false, showResend = true),
 *     theme        = OtpTheme(activeColor = MaterialTheme.colorScheme.primary),
 *     typography   = OtpTypography(titleSize = 28.sp, digitWeight = FontWeight.Bold),
 *     spacing      = OtpSpacing(subtitleToBoxes = 24.dp, boxSpacing = 12.dp),
 *     resendConfig = OtpResendConfig(minute = 1, second = 30),
 *     onVerify     = { otp -> ... },
 *     onSuccess    = { ... },
 *     onResendOtp  = { /* trigger API to resend OTP */ }
 * )
 * ```
 *
 * @param otpLength    Number of OTP digit boxes. Default: 6.
 * @param title        Title text. Shown when [OtpConfig.showTitle] is true.
 * @param subtitle     Subtitle text. Shown when [OtpConfig.showSubtitle] is true.
 * @param config       Visibility flags and behavioral settings (secure mode, messages).
 * @param theme        Colors, border width, corner radius, box shape.
 * @param typography   Per-element font sizes, weights, and families.
 * @param spacing      Gaps between every UI section and between OTP boxes.
 * @param resendConfig Countdown timer duration and label text.
 * @param modifier     Applied to the root Column.
 * @param onVerify     Required. Suspend lambda. Return [OtpResult.Success] or [OtpResult.Failure].
 *                     Any thrown exception is caught and treated as a generic failure.
 * @param onSuccess    Called exactly once when [onVerify] returns [OtpResult.Success].
 * @param onResendOtp  Called when the user taps the resend button after the timer expires.
 *                     Use this to trigger your API call to resend the OTP code.
 */
@Composable
public fun OTPView(
    otpLength    : Int              = 6,
    title        : String           = "Enter OTP",
    subtitle     : String           = "We sent a $otpLength-digit code to your number",
    config       : OtpConfig        = OtpConfig(),
    theme        : OtpTheme         = OtpTheme.default(),
    typography   : OtpTypography    = OtpTypography(),
    spacing      : OtpSpacing       = OtpSpacing(),
    resendConfig : OtpResendConfig  = OtpResendConfig(),
    modifier     : Modifier         = Modifier,
    onVerify     : suspend (otp: String) -> OtpResult,
    onSuccess    : () -> Unit        = {},
    onResendOtp  : () -> Unit        = {},
) {
    val holder = rememberOtpStateHolder(
        otpLength         = otpLength,
        isSecureInput     = config.isSecureInput,
        timerTotalSeconds = if (config.showResend) resendConfig.totalSeconds else 0,
        onVerify          = { otp ->
            val result = runCatching { onVerify(otp) }
                .getOrElse { OtpResult.Failure("Something went wrong. Please try again.") }
            when (result) {
                is OtpResult.Success -> OtpValidationState.Success
                is OtpResult.Failure -> OtpValidationState.Error(result.message)
            }
        },
        onResend          = onResendOtp,
    )

    LaunchedEffect(holder.validationState) {
        if (holder.validationState is OtpValidationState.Success) {
            onSuccess()
        }
    }

    OtpScreen(
        otpState      = holder.otpState,
        validState    = holder.validationState,
        onAction      = holder::onAction,
        title         = title,
        subtitle      = subtitle,
        config        = config,
        theme         = theme,
        typography    = typography,
        spacing       = spacing,
        resendConfig  = resendConfig,
        modifier      = modifier,
    )
}
