package com.pcs.otpview.internal.ui

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pcs.otpview.api.OtpTheme
import com.pcs.otpview.api.OtpTypography

/**
 * Internal single OTP digit input box.
 *
 * ── BACKSPACE FIX — SENTINEL CHARACTER ───────────────────────────────────────
 *
 * Root problem: custom Compose keyboards call InputConnection.deleteSurroundingText(1,0).
 * When the field text is EMPTY, there is nothing to delete — onValueChange NEVER fires.
 * onKeyEvent also does NOT fire for custom keyboards. Backspace on an empty field
 * was completely invisible.
 *
 * Fix: every field always holds an invisible sentinel character \u200B (zero-width
 * space) so it is never truly empty. deleteSurroundingText always has something to
 * remove, guaranteeing onValueChange fires for every backspace on every keyboard type.
 *
 *   Empty field  → "\u200B"   cursor at pos 1
 *   Filled field → "5\u200B"  cursor at pos 2
 *
 *   Case A — backspace on filled field "5\u200B":
 *     deleteSurroundingText removes "5" → text = "\u200B"
 *     onValueChange("\u200B") → onNumberChanged(null) → digit cleared, stay here
 *
 *   Case B — backspace on empty field "\u200B":
 *     deleteSurroundingText removes "\u200B" → text = ""
 *     onValueChange("") → onKeyboardBack() → move to previous field, clear it
 *
 * ── CURSOR VISIBILITY ─────────────────────────────────────────────────────────
 *
 * The BasicTextField cursor is hidden (Color.Transparent cursorBrush) because with
 * textAlign=Center on a transparent single-character string, the caret sits
 * off-center to the right — unreliable visually.
 *
 * Instead a custom 2dp bottom-bar cursor is drawn in the decoration box when the
 * field is focused and empty. This is the standard OTP field cursor pattern.
 */

private const val SENTINEL = "\u200B" // zero-width space — invisible, safe sentinel

@Composable
internal fun OtpInputField(
    number         : Int?,
    index          : Int,
    totalFields    : Int,
    focusRequester : FocusRequester,
    onFocusChanged : (Boolean) -> Unit,
    onNumberChanged: (Int?) -> Unit,
    onKeyboardBack : () -> Unit,
    onPaste        : (String) -> Unit,
    modifier       : Modifier      = Modifier,
    isEnabled      : Boolean       = true,
    isSecureInput  : Boolean       = false,
    hasError       : Boolean       = false,
    fontSize       : TextUnit      = 24.sp,
    theme          : OtpTheme      = OtpTheme.default(),
    typography     : OtpTypography = OtpTypography(),
) {
    // number == null → "\u200B"   (cursor at end, pos 1)
    // number == digit → "5\u200B" (cursor at end, pos 2)
    // Cursor at end means deleteSurroundingText(1,0) always removes the char
    // immediately before it — "5" for a filled field, "\u200B" for an empty one.
    val internalText by remember(number) {
        mutableStateOf(
            if (number != null) {
                TextFieldValue(text = "$number$SENTINEL", selection = TextRange(2))
            } else {
                TextFieldValue(text = SENTINEL, selection = TextRange(1))
            }
        )
    }

    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = when {
            hasError       -> theme.errorColor
            isFocused      -> theme.activeColor
            number != null -> theme.filledColor
            else           -> theme.inactiveColor
        },
        animationSpec = tween(150),
        label         = "otpBorderColor_$index",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .border(theme.borderWidth, borderColor, RoundedCornerShape(theme.cornerRadius))
            .background(theme.backgroundColor, RoundedCornerShape(theme.cornerRadius))
            .semantics {
                contentDescription = "OTP digit ${index + 1} of $totalFields, " +
                        if (number != null) "entered" else "empty"
            }
    ) {
        BasicTextField(
            value         = internalText,
            onValueChange = { newValue ->
                val realChars = newValue.text.replace(SENTINEL, "")

                when {
                    // Paste / SMS auto-fill: 2+ real digits at once
                    realChars.length > 1 -> {
                        onPaste(realChars)
                    }

                    // Case B: sentinel was deleted from an already-empty field
                    // → move to previous field
                    newValue.text.isEmpty() -> {
                        onKeyboardBack()
                    }

                    // Case A: digit was deleted, only sentinel remains
                    // → clear digit, stay on this field
                    newValue.text == SENTINEL -> {
                        onNumberChanged(null)
                    }

                    // Normal digit entry
                    realChars.length == 1 && realChars[0].isDigit() -> {
                        onNumberChanged(realChars[0].digitToInt())
                    }

                    // Non-digit non-sentinel — ignore
                }
            },
            // Hide built-in cursor — we draw a custom bottom-bar cursor below
            cursorBrush   = SolidColor(theme.activeColor),
            singleLine    = true,
            enabled       = isEnabled,
            textStyle     = TextStyle(
                textAlign  = TextAlign.Center,
                fontWeight = typography.digitWeight,
                fontFamily = typography.digitFontFamily,
                fontSize   = fontSize,
                color      = Color.Transparent, // hide sentinel + digit from raw text layer
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier        = Modifier
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    isFocused = state.isFocused
                    onFocusChanged(state.isFocused)
                }
                // Hardware keyboard only — do NOT consume (return false).
                // Let BasicTextField process via InputConnection → onValueChange fires.
                .onKeyEvent { event ->
                    val isBackspace = event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DEL
                    val isKeyDown   = event.nativeKeyEvent.action == KeyEvent.ACTION_DOWN
                    if (!isBackspace || !isKeyDown) return@onKeyEvent false
                    false
                },
            decorationBox = { innerBox ->
                // innerBox first — transparent input surface underneath all overlays
                innerBox()

                // Digit — normal mode
                if (number != null && !isSecureInput) {
                    Text(
                        text       = number.toString(),
                        textAlign  = TextAlign.Center,
                        color      = theme.textColor,
                        fontSize   = fontSize,
                        fontWeight = typography.digitWeight,
                        fontFamily = typography.digitFontFamily,
                        modifier   = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }

                // Bullet — secure / PIN mode
                if (isSecureInput && number != null) {
                    Text(
                        text      = "●",
                        textAlign = TextAlign.Center,
                        color     = theme.textColor,
                        fontSize  = fontSize * 0.6f,
                        modifier  = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }

                // Dash — empty and not focused
                if (number == null && !isFocused) {
                    Text(
                        text       = "—",
                        textAlign  = TextAlign.Center,
                        color      = theme.inactiveColor,
                        fontSize   = fontSize * 0.8f,
                        fontWeight = typography.digitWeight,
                        modifier   = Modifier.fillMaxSize().wrapContentSize(),
                    )
                }

                // Custom cursor bar — focused and empty
                // Replaces the hidden built-in cursor with a clearly visible
                // 2dp colored line at the bottom of the box.

                /** We have removed below as we don't wanted to show horizontal cursor we have used predefined with active color  */
               /* if (isFocused && number == null) {
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                            .padding(bottom = 5.dp),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(theme.activeColor)
                        )
                    }
                }*/
            }
        )
    }
}