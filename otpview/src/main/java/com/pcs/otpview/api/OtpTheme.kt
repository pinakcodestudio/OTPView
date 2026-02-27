package com.pcs.otpview.api

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Complete visual theme for [OTPView].
 *
 * Every color, border, shape, and size is independently configurable.
 * For typography, use [OtpTypography]. For spacing, use [OtpSpacing].
 *
 * ```kotlin
 * OTPView(
 *     theme = OtpTheme(
 *         activeColor      = MaterialTheme.colorScheme.primary,
 *         errorColor       = MaterialTheme.colorScheme.error,
 *         backgroundColor  = MaterialTheme.colorScheme.surfaceVariant,
 *         textColor        = MaterialTheme.colorScheme.onSurface,
 *         cornerRadius     = 12.dp,
 *         borderWidth      = 1.5.dp,
 *     )
 * )
 * ```
 *
 * @param activeColor        Border + cursor color on the focused field.
 * @param inactiveColor      Border + placeholder color on empty unfocused fields.
 * @param filledColor        Border color on filled unfocused fields.
 * @param errorColor         Border + error message text color in error state.
 * @param successColor       Success message text color.
 * @param backgroundColor    Per-box background fill color.
 * @param textColor          Digit text color. Also used for the ● in secure mode.
 * @param titleColor         Title text color. Defaults to [textColor].
 * @param subtitleColor      Subtitle text color. Defaults to [inactiveColor].
 * @param loaderColor        Circular progress indicator color. Defaults to [activeColor].
 * @param resendTextColor    Resend button label color. Defaults to [activeColor].
 * @param timerColor         Countdown timer text color. Defaults to [inactiveColor].
 * @param cornerRadius       Corner radius of each OTP box. 0.dp = square, 50.dp = pill.
 * @param borderWidth        Width of the box border stroke.
 * @param fontScaleFactor    Digit font size = box width × this value. Default: 0.40f.
 * @param boxAspectRatio     Width ÷ height of each box. 1.0f = perfect square.
 */
public data class OtpTheme(
    val activeColor     : Color = Color(0xFF00F15E),
    val inactiveColor   : Color = Color(0xFF555555),
    val filledColor     : Color = Color(0xFF00B847),
    val errorColor      : Color = Color(0xFFFF4444),
    val successColor    : Color = Color(0xFF00F15E),
    val backgroundColor : Color = Color(0xFF262626),
    val textColor       : Color = Color(0xFF00F15E),
    val titleColor      : Color = Color(0xFFFFFFFF),
    val subtitleColor   : Color = Color(0xFF888888),
    val loaderColor     : Color = Color(0xFF00F15E),
    val resendTextColor : Color = Color(0xFF00F15E),
    val timerColor      : Color = Color(0xFF888888),
    val cornerRadius    : Dp    = 8.dp,
    val borderWidth     : Dp    = 2.dp,
    val fontScaleFactor : Float = 0.40f,
    val boxAspectRatio  : Float = 1.0f,
) {
    public companion object {

        /** Default dark / fintech theme. Same as `OtpTheme()`. */
        public fun default(): OtpTheme = OtpTheme()

        /** Light theme for apps with white or light-grey backgrounds. */
        public fun light(): OtpTheme = OtpTheme(
            activeColor     = Color(0xFF1565C0),
            inactiveColor   = Color(0xFFBDBDBD),
            filledColor     = Color(0xFF1976D2),
            errorColor      = Color(0xFFD32F2F),
            successColor    = Color(0xFF388E3C),
            backgroundColor = Color(0xFFFFFFFF),
            textColor       = Color(0xFF1A1A1A),
            titleColor      = Color(0xFF1A1A1A),
            subtitleColor   = Color(0xFF666666),
            loaderColor     = Color(0xFF1565C0),
            resendTextColor = Color(0xFF1565C0),
            timerColor      = Color(0xFF666666),
        )
    }
}
