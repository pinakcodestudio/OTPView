package com.pcs.otpview.api

/**
 * The result returned from the [OTPView] `onVerify` suspend lambda.
 *
 * ```kotlin
 * OTPView(
 *     onVerify = { otp ->
 *         val ok = apiService.verifyOtp(otp)
 *         if (ok) OtpResult.Success
 *         else OtpResult.Failure("Incorrect OTP. Please try again.")
 *     }
 * )
 * ```
 */
public sealed class OtpResult {

    /** OTP verified. OTPView shows the success state and fires `onSuccess`. */
    public data object Success : OtpResult()

    /**
     * Verification failed.
     * The error [message] is displayed below the OTP row.
     * All fields are cleared automatically so the user can retry.
     */
    public data class Failure(
        val message: String = "Incorrect OTP. Please try again."
    ) : OtpResult()
}
