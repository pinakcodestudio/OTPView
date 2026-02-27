package com.pcs.otpview.internal.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pcs.otpview.api.OtpConfig
import com.pcs.otpview.api.OtpResendConfig
import com.pcs.otpview.api.OtpSpacing
import com.pcs.otpview.api.OtpTheme
import com.pcs.otpview.api.OtpTypography
import com.pcs.otpview.internal.model.OtpAction
import com.pcs.otpview.internal.model.OtpState
import com.pcs.otpview.internal.model.OtpValidationState

@Composable
internal fun OtpScreen(
    otpState    : OtpState,
    validState  : OtpValidationState,
    onAction    : (OtpAction) -> Unit,
    title       : String,
    subtitle    : String,
    config      : OtpConfig,
    theme       : OtpTheme,
    typography  : OtpTypography,
    spacing     : OtpSpacing,
    resendConfig: OtpResendConfig,
    modifier    : Modifier = Modifier,
) {
    val focusRequesters    = remember(otpState.otpLength) { List(otpState.otpLength) { FocusRequester() } }
    val focusManager       = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val shakeOffset        = remember { Animatable(0f) }

    // Focus field[0] on first composition
    LaunchedEffect(Unit) {
        focusRequesters.firstOrNull()?.requestFocus()
    }

    // State-driven focus transitions (digit entry, backspace, paste, reset)
    LaunchedEffect(otpState.focusedIndex) {
        otpState.focusedIndex?.let { idx ->
            focusRequesters.getOrNull(idx)?.requestFocus()
        }
    }

    // Dismiss keyboard when all fields are filled
    LaunchedEffect(otpState.isComplete) {
        if (otpState.isComplete) {
            focusRequesters.forEach { it.freeFocus() }
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    // Shake on error then re-focus field[0]
    LaunchedEffect(validState) {
        if (validState is OtpValidationState.Error) {
            repeat(3) {
                shakeOffset.animateTo(12f,  spring(stiffness = Spring.StiffnessHigh))
                shakeOffset.animateTo(-12f, spring(stiffness = Spring.StiffnessHigh))
            }
            shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
            focusRequesters.firstOrNull()?.requestFocus()
        }
    }

    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        // 1. Title
        if (config.showTitle) {
            Text(
                text       = title,
                fontSize   = typography.titleSize,
                fontWeight = typography.titleWeight,
                fontFamily = typography.titleFontFamily ?: FontFamily.Default,
                color      = theme.titleColor,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            )
            Spacer(Modifier.height(spacing.titleToSubtitle))
        }

        // 2. Subtitle
        if (config.showSubtitle) {
            Text(
                text       = subtitle,
                fontSize   = typography.subtitleSize,
                fontWeight = typography.subtitleWeight,
                fontFamily = typography.subtitleFontFamily ?: FontFamily.Default,
                color      = theme.subtitleColor,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
            )
        }

        if (config.showTitle || config.showSubtitle) {
            Spacer(Modifier.height(spacing.subtitleToBoxes))
        }

        // 3. OTP boxes
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.horizontalPadding)
                .graphicsLayer { translationX = shakeOffset.value }
        ) {
            val totalSpacing = spacing.boxSpacing * (otpState.otpLength - 1)
            val fieldWidthDp = (maxWidth - totalSpacing) / otpState.otpLength
            val density      = LocalDensity.current
            val fontSizeSp   = with(density) {
                (fieldWidthDp.toPx() * theme.fontScaleFactor).toSp()
            }

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.boxSpacing),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                otpState.code.forEachIndexed { index, number ->

                    // ── Synchronous tap interceptor ─────────────────────────────
                    // When the user taps a box that is PAST the first empty box,
                    // redirect focus to firstEmptyIndex synchronously — before
                    // Android assigns natural focus to the tapped box.
                    //
                    // This is the critical fix for custom position input.
                    // The previous approach redirected inside onFocusChanged which
                    // is async (LaunchedEffect → next frame). With rapid typing the
                    // frame never arrived before the next state change cancelled it.
                    // clickable fires synchronously on the UI thread, so the
                    // keyboard InputConnection attaches to the correct box immediately.
                    val firstEmptyIndex = otpState.code
                        .indexOfFirst { it == null }
                        .takeIf { it >= 0 }

                    val tapInterceptModifier = if (firstEmptyIndex != null && index > firstEmptyIndex) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication        = null,
                        ) {
                            focusRequesters.getOrNull(firstEmptyIndex)?.requestFocus()
                        }
                    } else {
                        Modifier
                    }

                    OtpInputField(
                        number          = number,
                        index           = index,
                        totalFields     = otpState.otpLength,
                        focusRequester  = focusRequesters[index],
                        onFocusChanged  = { isFocused ->
                            if (isFocused) onAction(OtpAction.OnChangeFieldFocused(index))
                        },
                        onNumberChanged = { newNumber ->
                            // freeFocus only on digit entry, not backspace.
                            // Backspace must keep focus on the current field.
                            if (newNumber != null) {
                                focusRequesters[index].freeFocus()
                            }
                            onAction(OtpAction.OnEnterNumber(newNumber, index))
                        },
                        onKeyboardBack  = { onAction(OtpAction.OnKeyboardBack) },
                        onPaste         = { pasted -> onAction(OtpAction.OnPaste(pasted)) },
                        isEnabled       = otpState.isEnabled,
                        isSecureInput   = otpState.isSecureInput,
                        hasError        = validState is OtpValidationState.Error,
                        fontSize        = fontSizeSp,
                        theme           = theme,
                        typography      = typography,
                        modifier        = tapInterceptModifier
                            .weight(1f)
                            .aspectRatio(theme.boxAspectRatio),
                    )
                }
            }
        }

        Spacer(Modifier.height(spacing.boxesToStatus))

        // 4. Loading indicator
        if (config.showLoader) {
            AnimatedVisibility(
                visible = validState is OtpValidationState.Loading,
                enter   = fadeIn(tween(200)),
                exit    = fadeOut(tween(150)),
            ) {
                CircularProgressIndicator(
                    color       = theme.loaderColor,
                    strokeWidth = 2.5.dp,
                    modifier    = Modifier.size(26.dp),
                )
            }
        }

        // 5. Error message
        if (config.showError) {
            AnimatedVisibility(
                visible = validState is OtpValidationState.Error,
                enter   = fadeIn() + slideInVertically { it / 2 },
                exit    = fadeOut(tween(150)),
            ) {
                val msg = (validState as? OtpValidationState.Error)?.message.orEmpty()
                Text(
                    text       = msg,
                    color      = theme.errorColor,
                    fontSize   = typography.errorSize,
                    fontWeight = typography.errorWeight,
                    textAlign  = TextAlign.Center,
                    modifier   = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                )
            }
        }

        // 6. Success message
        if (config.showSuccess) {
            AnimatedVisibility(
                visible = validState is OtpValidationState.Success,
                enter   = fadeIn() + slideInVertically { it / 2 },
                exit    = fadeOut(tween(150)),
            ) {
                Text(
                    text       = config.successMessage,
                    color      = theme.successColor,
                    fontSize   = typography.successSize,
                    fontWeight = typography.successWeight,
                    textAlign  = TextAlign.Center,
                )
            }
        }

        // 7. Resend section
        if (config.showResend && validState !is OtpValidationState.Success) {

            Spacer(Modifier.height(spacing.statusToResend))

            val isLoading = validState is OtpValidationState.Loading

            if (!otpState.timerExpired) {
                val label = resendConfig.timerLabel.format(otpState.timerFormatted)
                Text(
                    text       = label,
                    color      = theme.timerColor,
                    fontSize   = typography.resendSize,
                    fontWeight = typography.resendWeight,
                    textAlign  = TextAlign.Center,
                )
            } else {
                Button(
                    onClick   = { onAction(OtpAction.OnResend) },
                    enabled   = !isLoading,
                    colors    = ButtonDefaults.buttonColors(
                        containerColor         = Color.Transparent,
                        contentColor           = theme.resendTextColor,
                        disabledContentColor   = theme.inactiveColor,
                        disabledContainerColor = Color.Transparent,
                    ),
                    elevation = null,
                    shape     = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text       = resendConfig.resendLabel,
                        fontSize   = typography.resendSize,
                        fontWeight = typography.resendWeight,
                    )
                }
            }
        }
    }
}