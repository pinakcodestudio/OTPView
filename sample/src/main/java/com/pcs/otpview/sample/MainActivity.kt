package com.pcs.otpview.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pcs.otpview.api.OtpConfig
import com.pcs.otpview.api.OtpResendConfig
import com.pcs.otpview.api.OtpResult
import com.pcs.otpview.api.OtpSpacing
import com.pcs.otpview.api.OtpTheme
import com.pcs.otpview.api.OtpTypography
import com.pcs.otpview.api.OTPView
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { SampleApp() }
    }
}

private enum class Screen(val label: String) {
    PICKER              ("Picker"),
    DEMO_DEFAULT        ("Default — Full UI + 2 min timer"),
    DEMO_30_SEC_TIMER   ("30-Second Resend Timer"),
    DEMO_NO_TITLE       ("Hide Title & Subtitle"),
    DEMO_NO_RESEND      ("Hide Resend Section"),
    DEMO_LOADER_ONLY    ("Loader only — no error/success text"),
    DEMO_4_DIGIT        ("4-Digit OTP"),
    DEMO_SECURE_PIN     ("Secure PIN (● mode)"),
    DEMO_LIGHT          ("Light Theme Preset"),
    DEMO_MATERIAL3      ("Material3 Color Scheme"),
    DEMO_CUSTOM_SPACING ("Custom Spacing & Typography"),
    DEMO_ALWAYS_FAIL    ("Always Fails — Test Error UX"),
}

@Composable
private fun SampleApp() {
    var screen by remember { mutableStateOf(Screen.PICKER) }
    when (screen) {
        Screen.PICKER -> Picker(onSelect = { screen = it })
        else          -> Demo(screen = screen, onBack = { screen = Screen.PICKER })
    }
}

@Composable
private fun Picker(onSelect: (Screen) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1C))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        Text("OTPView", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00F15E))
        Spacer(Modifier.height(4.dp))
        Text("by PCS  •  v1.0.0", fontSize = 13.sp, color = Color(0xFF888888))
        Spacer(Modifier.height(8.dp))
        Text("Tap a demo to test it", fontSize = 14.sp, color = Color(0xFF888888))
        Spacer(Modifier.height(24.dp))

        Screen.entries.filter { it != Screen.PICKER }.forEach { s ->
            Button(
                onClick  = { onSelect(s) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            ) { Text(s.label, fontSize = 13.sp) }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Demo(screen: Screen, onBack: () -> Unit) {
    // BackHandler intercepts the system back button/gesture.
    // This is completely separate from keyboard backspace.
    // Keyboard backspace removes OTP digits — that is handled inside OtpInputField.
    // System back navigates away — that is handled here.
    BackHandler { onBack() }
    val context = LocalContext.current

    when (screen) {

        // ── Default: all sections visible, 2-minute timer ── Correct OTP: 123456
        Screen.DEMO_DEFAULT -> Scaffold(containerColor = Color(0xFF1C1C1C)) { p ->
            OTPView(
                otpLength = 6,
                modifier  = Modifier.fillMaxSize().padding(p),
                onVerify  = { otp ->
                    delay(1500)
                    if (otp == "123456") OtpResult.Success
                    else OtpResult.Failure("Incorrect OTP. Please try again.")
                },
                onSuccess = {
                    Toast.makeText(context, "✓ Verified!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                onResendOtp = {
                    Toast.makeText(context, "OTP resent!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ── 30-second resend timer ── Correct OTP: 123456
        Screen.DEMO_30_SEC_TIMER -> Scaffold(containerColor = Color(0xFF1C1C1C)) { p ->
            OTPView(
                otpLength    = 6,
                resendConfig = OtpResendConfig(minute = 0, second = 30),
                modifier     = Modifier.fillMaxSize().padding(p),
                onVerify     = { otp ->
                    delay(1500)
                    if (otp == "123456") OtpResult.Success
                    else OtpResult.Failure("Incorrect OTP. Please try again.")
                },
                onSuccess = {
                    Toast.makeText(context, "✓ Verified!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                onResendOtp = {
                    Toast.makeText(context, "OTP resent!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ── Title and subtitle hidden ── Correct OTP: 123456
        Screen.DEMO_NO_TITLE -> Scaffold(containerColor = Color(0xFF1C1C1C)) { p ->
            OTPView(
                otpLength    = 6,
                resendConfig = OtpResendConfig(minute = 0, second = 30),
                config       = OtpConfig(showTitle = false, showSubtitle = false),
                modifier     = Modifier.fillMaxSize().padding(p),
                onVerify     = { otp ->
                    delay(1500)
                    if (otp == "123456") OtpResult.Success
                    else OtpResult.Failure("Incorrect OTP. Please try again.")
                },
                onSuccess    = {
                    Toast.makeText(context, "✓ Verified!", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            )
        }

        // ── Resend section hidden ── Correct OTP: 123456
        Screen.DEMO_NO_RESEND -> Scaffold(containerColor = Color(0xFF1C1C1C)) { p ->
            OTPView(
                otpLength = 6,
                config    = OtpConfig(showResend = false),
                modifier  = Modifier.fillMaxSize().padding(p),
                onVerify  = { otp ->
                    delay(1500)
                    if (otp == "123456") OtpResult.Success
                    else OtpResult.Failure("Incorrect OTP. Please try again.")
                },
                onSuccess = {
                    Toast.makeText(context, "✓ Verified!", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            )
        }

        // ── Loader visible, but no error/success text ── Correct OTP: 123456
        Screen.DEMO_LOADER_ONLY -> Scaffold(containerColor = Color(0xFF1C1C1C)) { p ->
            OTPView(
                otpLength    = 6,
                resendConfig = OtpResendConfig(minute = 0, second = 30),
                config       = OtpConfig(showError = false, showSuccess = false),
                modifier     = Modifier.fillMaxSize().padding(p),
                onVerify     = { otp ->
                    delay(1500)
                    if (otp == "123456") OtpResult.Success
                    else OtpResult.Failure("Incorrect OTP.")
                },
                onSuccess = {
                    Toast.makeText(context, "✓ Verified!", Toast.LENGTH_SHORT).show()
                    onBack()
                }
            )
        }

        // ── 4-digit OTP ── Correct OTP: 1234
        Screen.DEMO_4_DIGIT -> Scaffold(containerColor = Color(0xFF1C1C1C)) { p ->
            OTPView(
                otpLength    = 4,
                title        = "Verify Email",
                subtitle     = "We sent a 4-digit code to your email",
                resendConfig = OtpResendConfig(minute = 0, second = 30),
                modifier     = Modifier.fillMaxSize().padding(p),
                onVerify     = { otp ->
                    delay(1500)
                    if (otp == "1234") OtpResult.Success
                    else OtpResult.Failure("Incorrect code. Please check your email.")
                },
                onSuccess = {
                    Toast.makeText(context, "✓ Email verified!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                onResendOtp = {
                    Toast.makeText(context, "Code resent to email!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ── 4-digit secure PIN (digits shown as ●) ── Correct PIN: 1234
        Screen.DEMO_SECURE_PIN -> Scaffold(containerColor = Color(0xFF1C1C1C)) { p ->
            OTPView(
                otpLength    = 4,
                title        = "Enter PIN",
                subtitle     = "Your 4-digit security PIN",
                resendConfig = OtpResendConfig(
                    minute      = 0,
                    second      = 30,
                    resendLabel = "Forgot PIN?",
                    timerLabel  = "PIN reset available in %s",
                ),
                config  = OtpConfig(
                    isSecureInput  = true,
                    successMessage = "✓  PIN Accepted!",
                ),
                modifier = Modifier.fillMaxSize().padding(p),
                onVerify = { pin ->
                    delay(800)
                    if (pin == "1234") OtpResult.Success
                    else OtpResult.Failure("Wrong PIN. Please try again.")
                },
                onSuccess = {
                    Toast.makeText(context, "✓ PIN accepted!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                onResendOtp = {
                    Toast.makeText(context, "PIN reset email sent!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ── Light theme preset ── Correct OTP: 123456
        Screen.DEMO_LIGHT -> Scaffold(containerColor = Color(0xFFF5F5F5)) { p ->
            OTPView(
                otpLength    = 6,
                theme        = OtpTheme.light(),
                resendConfig = OtpResendConfig(minute = 0, second = 30),
                modifier     = Modifier.fillMaxSize().padding(p),
                onVerify     = { otp ->
                    delay(1500)
                    if (otp == "123456") OtpResult.Success
                    else OtpResult.Failure("Incorrect OTP. Please try again.")
                },
                onSuccess = {
                    Toast.makeText(context, "✓ Verified!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                onResendOtp = {
                    Toast.makeText(context, "OTP resent!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ── Material3 color scheme ── Correct OTP: 123456
        Screen.DEMO_MATERIAL3 -> MaterialTheme {
            val colors = MaterialTheme.colorScheme
            Scaffold(containerColor = colors.background) { p ->
                OTPView(
                    otpLength    = 6,
                    resendConfig = OtpResendConfig(minute = 0, second = 30),
                    theme        = OtpTheme(
                        activeColor     = colors.primary,
                        filledColor     = colors.primaryContainer,
                        inactiveColor   = colors.outline,
                        errorColor      = colors.error,
                        successColor    = colors.primary,
                        backgroundColor = colors.surfaceVariant,
                        textColor       = colors.onSurface,
                        titleColor      = colors.onBackground,
                        subtitleColor   = colors.onSurfaceVariant,
                        loaderColor     = colors.primary,
                        resendTextColor = colors.primary,
                        timerColor      = colors.onSurfaceVariant,
                    ),
                    modifier  = Modifier.fillMaxSize().padding(p),
                    onVerify  = { otp ->
                        delay(1500)
                        if (otp == "123456") OtpResult.Success
                        else OtpResult.Failure("Incorrect OTP. Please try again.")
                    },
                    onSuccess = {
                        Toast.makeText(context, "✓ Verified!", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    onResendOtp = {
                        Toast.makeText(context, "OTP resent!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // ── Custom spacing and typography ── Correct OTP: 123456
        Screen.DEMO_CUSTOM_SPACING -> Scaffold(containerColor = Color(0xFF0D0D0D)) { p ->
            OTPView(
                otpLength    = 6,
                title        = "Two-Factor Auth",
                subtitle     = "Enter the 6-digit code from your authenticator app",
                resendConfig = OtpResendConfig(minute = 0, second = 30),
                theme        = OtpTheme(
                    activeColor     = Color(0xFF7C3AED),
                    filledColor     = Color(0xFF6D28D9),
                    inactiveColor   = Color(0xFF374151),
                    errorColor      = Color(0xFFEF4444),
                    successColor    = Color(0xFF10B981),
                    backgroundColor = Color(0xFF1F2937),
                    textColor       = Color(0xFFE5E7EB),
                    titleColor      = Color(0xFFFFFFFF),
                    subtitleColor   = Color(0xFF9CA3AF),
                    loaderColor     = Color(0xFF7C3AED),
                    resendTextColor = Color(0xFF7C3AED),
                    timerColor      = Color(0xFF6B7280),
                    cornerRadius    = 12.dp,
                    borderWidth     = 1.5.dp,
                ),
                typography = OtpTypography(
                    titleSize     = 28.sp,
                    titleWeight   = FontWeight.ExtraBold,
                    subtitleSize  = 14.sp,
                    digitWeight   = FontWeight.Bold,
                    successSize   = 18.sp,
                    errorSize     = 13.sp,
                    resendSize    = 15.sp,
                ),
                spacing = OtpSpacing(
                    titleToSubtitle  = 6.dp,
                    subtitleToBoxes  = 28.dp,
                    boxesToStatus    = 20.dp,
                    statusToResend   = 20.dp,
                    boxSpacing       = 12.dp,
                    horizontalPadding= 20.dp,
                ),
                modifier  = Modifier.fillMaxSize().padding(p),
                onVerify  = { otp ->
                    delay(1500)
                    if (otp == "123456") OtpResult.Success
                    else OtpResult.Failure("Incorrect verification code.")
                },
                onSuccess = {
                    Toast.makeText(context, "✓ Access granted!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                onResendOtp = {
                    Toast.makeText(context, "New code sent!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ── Always fails — tests error UX ── Enter any 6 digits
        Screen.DEMO_ALWAYS_FAIL -> Scaffold(containerColor = Color(0xFF1C1C1C)) { p ->
            OTPView(
                otpLength    = 6,
                subtitle     = "Enter any 6 digits to see the error UX",
                resendConfig = OtpResendConfig(minute = 0, second = 10),
                modifier     = Modifier.fillMaxSize().padding(p),
                onVerify     = { _ ->
                    delay(1200)
                    OtpResult.Failure("Wrong OTP. Please check and try again.")
                },
                onSuccess   = { /* never reached */ },
                onResendOtp = {
                    Toast.makeText(context, "OTP resent!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        Screen.PICKER -> Unit
    }
}
