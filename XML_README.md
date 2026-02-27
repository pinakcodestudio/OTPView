# OTPView — Using in XML / View-Based Projects

OTPView is a Jetpack Compose library. It can be embedded in any existing XML/View-based project using `ComposeView` — Android's official interoperability bridge between the View system and Compose.

No changes to your View hierarchy or navigation architecture are required. `ComposeView` is a standard `View` that hosts a Compose composition tree. You place it in your XML layout, then call `setContent { }` in your Activity or Fragment to render `OTPView` inside it.


---

## Prerequisites

Your project must already have `compileSdk 21+` and use `androidx`. If your project is on the old `android.support` library, migrate to AndroidX before proceeding.

---

## Step 1 — Install the library

In your root `settings.gradle` or `settings.gradle.kts`, add JitPack to the repository list:

**Groovy DSL (`settings.gradle`):**
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

**Kotlin DSL (`settings.gradle.kts`):**
```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

---

## Step 2 — Add dependencies

In your module's `build.gradle` or `build.gradle.kts`, add the library and the Compose dependencies required to host it. Compose requires the BOM for consistent versions.

**Groovy DSL (`build.gradle`):**
```groovy
android {
    compileSdk 35

    defaultConfig {
        minSdk 21
        // ... rest of your config
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // OTPView
    implementation 'com.github.pinakcodestudio:OTPView:1.0.0'

    // Compose — required to host ComposeView in a View-based project
    implementation platform('androidx.compose:compose-bom:2024.09.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.activity:activity-compose:1.9.2'

    // ... your other dependencies
}
```

**Kotlin DSL (`build.gradle.kts`):**
```kotlin
android {
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("com.github.pinakcodestudio:OTPView:1.0.0")

    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.2")
}
```

> **Note on Kotlin Compose compiler plugin:** If you are on Kotlin 2.0 or higher, use the `org.jetbrains.kotlin.plugin.compose` Gradle plugin instead of the old `composeOptions.kotlinCompilerExtensionVersion`. If you are on Kotlin 1.x, set `kotlinCompilerExtensionVersion` as shown above to `"1.5.14"` (or the version matching your Kotlin version — see the [Compose Kotlin compatibility map](https://developer.android.com/jetpack/androidx/releases/compose-kotlin)).

---

## Step 3 — Add ComposeView to your XML layout

`ComposeView` is a standard Android `View`. Add it anywhere in your layout file where you want the OTPView to appear.

**`res/layout/activity_otp.xml`** (Activity example):
```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"
    android:background="#1C1C1C">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center_horizontal"
        android:padding="16dp">

        <!-- Your existing XML views can go here -->

        <!-- OTPView hosted inside ComposeView -->
        <androidx.compose.ui.platform.ComposeView
            android:id="@+id/otp_compose_view"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="32dp" />

        <!-- More XML views can follow -->

    </LinearLayout>
</ScrollView>
```

**`res/layout/fragment_otp.xml`** (Fragment example):
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#1C1C1C">

    <androidx.compose.ui.platform.ComposeView
        android:id="@+id/otp_compose_view"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_gravity="center" />

</FrameLayout>
```

---

## Step 4 — Wire up in Activity

In your Activity, find the `ComposeView` by ID and call `setContent { }` on it. Wrap the content in `MaterialTheme` (or any Compose theme from your app) so that Material3 defaults are available.

```kotlin
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.pcs.otpview.api.OTPView
import com.pcs.otpview.api.OtpResult
import com.pcs.otpview.api.OtpTheme
import com.pcs.otpview.api.OtpResendConfig

class OtpActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        val composeView = findViewById<androidx.compose.ui.platform.ComposeView>(R.id.otp_compose_view)

        composeView.setContent {
            // Wrap in your app's theme so Material3 tokens are resolved correctly.
            // If you don't have a custom theme, MaterialTheme() with no arguments works.
            MaterialTheme {
                OTPView(
                    otpLength = 6,
                    theme     = OtpTheme.default(),
                    resendConfig = OtpResendConfig(minute = 2),
                    onVerify  = { otp ->
                        // This is a suspend function — you can call suspend APIs directly.
                        val ok = apiService.verifyOtp(otp)
                        if (ok) OtpResult.Success
                        else    OtpResult.Failure("Incorrect OTP. Please try again.")
                    },
                    onSuccess = {
                        // Navigate away, show a result, or finish the activity.
                        finish()
                    },
                    onResendOtp = {
                        apiService.resendOtp(phoneNumber)
                    }
                )
            }
        }
    }
}
```

> **Important — do not use `setContent` at the Activity level and also call `composeView.setContent`.**
> These are two different ways to host Compose. Use one or the other per Activity. If your Activity already uses `setContent { }` (the `activity-compose` extension), you do not need `ComposeView` at all — just call `OTPView(...)` directly inside your composable tree.
>
> `ComposeView.setContent { }` is the correct API when your Activity uses `setContentView(R.layout....)` (the traditional XML approach).

---

## Step 5 — Wire up in Fragment

In a Fragment, set the `ComposeView`'s content in `onCreateView` or `onViewCreated`. Use `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed` to align the Compose lifecycle with the Fragment's view lifecycle — this prevents memory leaks and recomposition issues when the Fragment is detached.

```kotlin
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pcs.otpview.api.OTPView
import com.pcs.otpview.api.OtpResult
import com.pcs.otpview.api.OtpResendConfig
import com.pcs.otpview.api.OtpTheme

class OtpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {

            // This strategy ties the Compose composition to the Fragment's view lifecycle.
            // It ensures the composition is disposed when the Fragment view is destroyed
            // (e.g., on back-stack pop), preventing memory leaks.
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                MaterialTheme {
                    OTPView(
                        otpLength    = 6,
                        title        = "Verify your number",
                        subtitle     = "Enter the 6-digit code we sent you",
                        theme        = OtpTheme.default(),
                        resendConfig = OtpResendConfig(minute = 2),
                        onVerify     = { otp ->
                            val result = apiService.verifyOtp(otp)
                            if (result.isSuccess) OtpResult.Success
                            else OtpResult.Failure(result.errorMessage)
                        },
                        onSuccess = {
                            findNavController().navigate(R.id.action_otp_to_home)
                        },
                        onResendOtp = {
                            apiService.resendOtp(phoneNumber)
                        }
                    )
                }
            }
        }
    }
}
```

**Why `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed`?**

Without this strategy, `ComposeView` defaults to `DisposeOnDetachedFromWindow`, which disposes the composition when the view is detached from the window. In Fragment back stacks, views are detached but the Fragment is not destroyed — this causes the OTPView state to be lost and the composition to be re-created unnecessarily. `DisposeOnViewTreeLifecycleDestroyed` keeps the composition alive as long as the Fragment's view lifecycle is alive, matching Fragment semantics exactly.

---

## Handling the soft keyboard

OTPView opens the keyboard automatically when it enters composition. For the keyboard not to overlap the OTP boxes, the Activity must be configured with `windowSoftInputMode="adjustResize"`.

In your `AndroidManifest.xml`:

```xml
<activity
    android:name=".OtpActivity"
    android:windowSoftInputMode="adjustResize">
</activity>
```

For Fragment-based apps where a single Activity hosts multiple Fragments, set this on the host Activity:

```xml
<activity
    android:name=".MainActivity"
    android:windowSoftInputMode="adjustResize">
</activity>
```

If your Activity uses `WindowCompat.setDecorFitsSystemWindows(window, false)` (edge-to-edge), you may need to add bottom padding equal to the IME inset to the `ComposeView` or its parent. The standard Compose `imePadding()` modifier handles this:

```kotlin
composeView.setContent {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding() // shifts content above keyboard
        ) {
            OTPView(
                otpLength = 6,
                onVerify  = { otp -> apiService.verify(otp) },
                onSuccess = { finish() }
            )
        }
    }
}
```

---

## Handling results and navigation

In a View-based project, your `onSuccess` callback typically does one of the following:

**Finish the Activity:**
```kotlin
onSuccess = { finish() }
```

**Start a new Activity:**
```kotlin
onSuccess = {
    startActivity(Intent(this, HomeActivity::class.java))
    finish()
}
```

**Navigate with NavController (if using Jetpack Navigation with Fragments):**
```kotlin
onSuccess = {
    findNavController().navigate(R.id.action_otp_to_home)
}
```

**Communicate back to the host Activity from a Fragment** (e.g., via a shared ViewModel):
```kotlin
// In your Fragment
private val sharedViewModel: AuthViewModel by activityViewModels()

// Inside setContent
onSuccess = {
    sharedViewModel.onOtpVerified()
}
```

---

## Full Fragment example

This is a complete, self-contained Fragment that can be dropped into any existing XML-based project using Jetpack Navigation.

```kotlin
package com.example.myapp.ui.otp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pcs.otpview.api.OTPView
import com.pcs.otpview.api.OtpConfig
import com.pcs.otpview.api.OtpResendConfig
import com.pcs.otpview.api.OtpResult
import com.pcs.otpview.api.OtpTheme
import kotlinx.coroutines.delay

class OtpVerificationFragment : Fragment() {

    // In a real app, inject or get this from a ViewModel
    private val phoneNumber: String by lazy {
        arguments?.getString("phoneNumber") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {

            // Correctly ties composition lifecycle to the Fragment's view lifecycle
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                // Use your app's real theme here
                MaterialTheme {
                    OTPView(
                        otpLength = 6,
                        title     = "Verify your number",
                        subtitle  = "We sent a 6-digit code to $phoneNumber",

                        config = OtpConfig(
                            showTitle      = true,
                            showSubtitle   = true,
                            showLoader     = true,
                            showError      = true,
                            showSuccess    = true,
                            showResend     = true,
                            successMessage = "✓  Number verified!",
                        ),

                        theme        = OtpTheme.default(),
                        resendConfig = OtpResendConfig(minute = 2),

                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1C1C))
                            .padding(vertical = 32.dp),

                        onVerify = { otp ->
                            // Simulate a network call
                            delay(1500)
                            if (otp == "123456") OtpResult.Success
                            else OtpResult.Failure("Incorrect OTP. Please try again.")
                        },

                        onSuccess = {
                            // Navigate to the next destination
                            findNavController().navigate(
                                OtpVerificationFragmentDirections.actionOtpToHome()
                            )
                        },

                        onResendOtp = {
                            // Trigger your resend API
                            Toast.makeText(
                                requireContext(),
                                "OTP resent to $phoneNumber",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}
```

---

## Limitations

When using OTPView inside a View-based project via `ComposeView`, be aware of the following:

**Compose version coupling.** Your app must include the Compose BOM and runtime. If your project previously had no Compose dependencies, adding them increases APK size. Compose tooling (such as Layout Inspector Compose support) works normally.

**Theme tokens.** OTPView does not automatically inherit colors from XML themes (`styles.xml`). Pass an `OtpTheme` instance explicitly with your brand colors, or map them from `MaterialTheme.colorScheme` inside the `setContent` block.

**Activity `windowSoftInputMode`.** OTPView opens the keyboard automatically. If your Activity does not set `windowSoftInputMode="adjustResize"`, the keyboard will overlap the OTP boxes on devices running Android 10 and below. On Android 11+ with edge-to-edge, use the `imePadding()` modifier as shown in the [Handling the soft keyboard](#handling-the-soft-keyboard) section.

**Fragment lifecycle.** Always set `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed` on the `ComposeView` inside Fragments. Omitting it causes the composition to be disposed when the Fragment is placed on the back stack, which resets all OTP state unexpectedly when the user returns.

**System Back.** OTPView does not intercept the system back gesture or button. Handle navigation back (exiting the OTP screen) in your Fragment's `OnBackPressedCallback` or Activity's `onBackPressed()` as you normally would.