package com.pcs.otpview.api

/**
 * Configuration for the resend OTP countdown timer.
 *
 * The timer starts automatically when [OTPView] enters composition.
 * While it is counting down, the resend button is replaced by a countdown text.
 * When it reaches zero, the resend button appears and [onResendOtp] is available.
 * Clicking resend restarts the timer from the beginning.
 *
 * ```kotlin
 * // Default: 2 minutes
 * OTPView(
 *     resendConfig = OtpResendConfig()
 * )
 *
 * // Custom: 30 seconds
 * OTPView(
 *     resendConfig = OtpResendConfig(minute = 0, second = 30)
 * )
 *
 * // Custom: 1 minute 30 seconds
 * OTPView(
 *     resendConfig = OtpResendConfig(minute = 1, second = 30)
 * )
 * ```
 *
 * @param minute       Minutes component of the countdown. Default: 2.
 * @param second       Seconds component of the countdown. Default: 0.
 * @param timerLabel   Format string for the countdown. Use %s for the time value.
 *                     Default: "Resend OTP in %s"
 * @param resendLabel  Text shown on the button when the timer has expired.
 *                     Default: "Resend OTP"
 */
public data class OtpResendConfig(
    val minute      : Int    = 2,
    val second      : Int    = 0,
    val timerLabel  : String = "Resend OTP in %s",
    val resendLabel : String = "Resend OTP",
) {
    /** Total countdown duration in seconds. */
    internal val totalSeconds: Int get() = (minute * 60) + second

    internal companion object {
        /** Formats remaining seconds as MM:SS. */
        internal fun formatTime(remainingSeconds: Int): String {
            val m = remainingSeconds / 60
            val s = remainingSeconds % 60
            return "%02d:%02d".format(m, s)
        }
    }
}
