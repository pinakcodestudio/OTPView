package com.pcs.otpview.api

/**
 * Visibility and behavioral configuration for [OTPView].
 *
 * Every UI section is independently visible or hidden.
 * All parameters are optional — the defaults produce a full-featured UI.
 *
 * ```kotlin
 * // Show everything — the default
 * OTPView(config = OtpConfig())
 *
 * // Boxes + error only — hide everything else
 * OTPView(
 *     config = OtpConfig(
 *         showTitle      = false,
 *         showSubtitle   = false,
 *         showLoader     = false,
 *         showSuccess    = false,
 *         showResend     = false,
 *     )
 * )
 *
 * // Secure PIN entry
 * OTPView(
 *     config = OtpConfig(
 *         isSecureInput  = true,
 *         successMessage = "✓ PIN Accepted!",
 *     )
 * )
 * ```
 *
 * @param isSecureInput   Show ● instead of digits (PIN mode). Default: false.
 * @param showTitle       Show the title text. Default: true.
 * @param showSubtitle    Show the subtitle text. Default: true.
 * @param showLoader      Show the circular progress indicator while verifying. Default: true.
 * @param showError       Show the error message text on failure. Default: true.
 * @param showSuccess     Show the success message text on verification. Default: true.
 * @param showResend      Show the resend / countdown timer section. Default: true.
 * @param successMessage  Text shown in the success state. Default: "✓  OTP Verified!".
 */
public data class OtpConfig(
    val isSecureInput  : Boolean = false,
    val showTitle      : Boolean = true,
    val showSubtitle   : Boolean = true,
    val showLoader     : Boolean = true,
    val showError      : Boolean = true,
    val showSuccess    : Boolean = true,
    val showResend     : Boolean = true,
    val successMessage : String  = "✓  OTP Verified!",
)
