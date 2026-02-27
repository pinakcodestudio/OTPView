package com.pcs.otpview.api

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Typography settings for each text element inside [OTPView].
 *
 * Every text element is independently configurable.
 * Pass `null` for any font family to use the system default.
 *
 * ```kotlin
 * OTPView(
 *     typography = OtpTypography(
 *         titleSize       = 28.sp,
 *         titleWeight     = FontWeight.ExtraBold,
 *         subtitleSize    = 15.sp,
 *         digitWeight     = FontWeight.Bold,
 *         errorSize       = 13.sp,
 *         resendSize      = 14.sp,
 *     )
 * )
 * ```
 */
public data class OtpTypography(
    /** Font size of the title text.                       Default: 24.sp */
    val titleSize        : TextUnit   = 24.sp,
    /** Font weight of the title text.                     Default: Bold */
    val titleWeight      : FontWeight = FontWeight.Bold,
    /** Font family of the title text. null = system.      Default: null */
    val titleFontFamily  : FontFamily? = null,

    /** Font size of the subtitle text.                    Default: 14.sp */
    val subtitleSize     : TextUnit   = 14.sp,
    /** Font weight of the subtitle text.                  Default: Normal */
    val subtitleWeight   : FontWeight = FontWeight.Normal,
    /** Font family of the subtitle. null = system.        Default: null */
    val subtitleFontFamily: FontFamily? = null,

    /** Font weight of the OTP digit characters.           Default: SemiBold */
    val digitWeight      : FontWeight = FontWeight.SemiBold,
    /** Font family of the OTP digits. null = system.      Default: null */
    val digitFontFamily  : FontFamily? = null,

    /** Font size of the success message text.             Default: 16.sp */
    val successSize      : TextUnit   = 16.sp,
    /** Font weight of the success message.                Default: SemiBold */
    val successWeight    : FontWeight = FontWeight.SemiBold,

    /** Font size of the error message text.               Default: 14.sp */
    val errorSize        : TextUnit   = 14.sp,
    /** Font weight of the error message.                  Default: Medium */
    val errorWeight      : FontWeight = FontWeight.Medium,

    /** Font size of the resend / timer text.              Default: 14.sp */
    val resendSize       : TextUnit   = 14.sp,
    /** Font weight of the resend / timer text.            Default: Medium */
    val resendWeight     : FontWeight = FontWeight.Medium,
)
