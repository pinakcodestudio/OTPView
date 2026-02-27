package com.pcs.otpview.api

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing configuration for [OTPView].
 *
 * Controls the gaps between every UI section and the OTP boxes.
 *
 * ```kotlin
 * OTPView(
 *     spacing = OtpSpacing(
 *         titleToSubtitle    = 4.dp,
 *         subtitleToBoxes    = 24.dp,
 *         boxesToStatus      = 20.dp,
 *         statusToResend     = 24.dp,
 *         boxSpacing         = 12.dp,
 *         horizontalPadding  = 32.dp,
 *     )
 * )
 * ```
 */
public data class OtpSpacing(
    /** Gap between title and subtitle.                    Default: 8.dp  */
    val titleToSubtitle   : Dp = 8.dp,
    /** Gap between subtitle (or title if no subtitle) and OTP boxes. Default: 32.dp */
    val subtitleToBoxes   : Dp = 32.dp,
    /** Gap between OTP boxes row and the status area.     Default: 24.dp */
    val boxesToStatus     : Dp = 24.dp,
    /** Gap between status area and resend button.         Default: 16.dp */
    val statusToResend    : Dp = 16.dp,
    /** Gap between individual OTP boxes.                  Default: 8.dp  */
    val boxSpacing        : Dp = 8.dp,
    /** Left and right padding of the OTP boxes row.       Default: 24.dp */
    val horizontalPadding : Dp = 24.dp,
)
