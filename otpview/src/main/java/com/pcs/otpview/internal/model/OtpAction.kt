package com.pcs.otpview.internal.model

internal sealed interface OtpAction {
    /** A digit was typed (0-9) or cleared (null) in field at [index]. */
    data class OnEnterNumber(val number: Int?, val index: Int) : OtpAction

    /** A field gained focus — keeps focusedIndex in sync with real Android focus. */
    data class OnChangeFieldFocused(val index: Int) : OtpAction

    /**
     * Backspace pressed on an empty field (Case B).
     * Clears the previous field and moves focus back one step.
     *
     * Case A (backspace on a field that HAS a digit) is handled via
     * OnEnterNumber(null, index) — dispatched from OtpInputField.onKeyEvent
     * directly, bypassing onValueChange to avoid race conditions on the
     * numeric keyboard.
     */
    data object OnKeyboardBack : OtpAction

    /** User pasted text. Raw string — may contain non-digits, will be filtered. */
    data class OnPaste(val pastedText: String) : OtpAction

    /** User tapped the Resend button after the timer expired. Resets fields + timer. */
    data object OnResend : OtpAction

    /** Internal — timer coroutine tick, decrements remaining seconds by 1. */
    data object OnTimerTick : OtpAction
}
