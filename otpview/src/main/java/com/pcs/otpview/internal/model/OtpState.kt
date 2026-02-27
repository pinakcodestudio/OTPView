package com.pcs.otpview.internal.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Parcelable OTP input state — persisted across configuration changes (rotation).
 *
 * Design decisions:
 * - [OtpValidationState] is NOT saved. It is transient — a rotating device should
 *   not be mid-verification. The user re-enters the OTP after rotation.
 * - [timerRemainingSeconds] IS saved. After rotation the timer resumes correctly
 *   from where it left off.
 * - When the composable leaves composition entirely (navigation), the whole
 *   rememberSaveable bundle is discarded automatically — fresh state on re-entry.
 */
@Parcelize
internal data class OtpState(
    val otpLength             : Int        = 6,
    val code                  : List<Int?> = List(otpLength) { null },
    val focusedIndex          : Int?       = null,
    val isSecureInput         : Boolean    = false,
    val isEnabled             : Boolean    = true,
    /** Remaining countdown seconds. 0 = timer expired, resend button shown. */
    val timerRemainingSeconds : Int        = 0,
) : Parcelable {
    val isComplete    : Boolean get() = code.none { it == null }
    val otpString     : String  get() = code.joinToString("") { it?.toString() ?: "" }
    val timerExpired  : Boolean get() = timerRemainingSeconds <= 0
    val timerFormatted: String  get() {
        val m = timerRemainingSeconds / 60
        val s = timerRemainingSeconds % 60
        return "%02d:%02d".format(m, s)
    }
}

internal sealed class OtpValidationState {
    data object Idle    : OtpValidationState()
    data object Loading : OtpValidationState()
    data object Success : OtpValidationState()
    data class  Error(val message: String) : OtpValidationState()
}
