# OTPView — XML / View-Based Integration Guide

OTPView is a Jetpack Compose library that can be embedded in any existing XML/View-based project using `ComposeView` — Android's official bridge between the View system and Compose.

This guide covers complete setup, common issues, and solutions for production-ready integration.


---

## Prerequisites

- **Android Studio**: Ladybug (2024.2.1) or later recommended
- **minSdk**: 21 (Android 5.0) or higher
- **compileSdk**: 35 or higher
- **Kotlin**: 1.9.0 or higher (2.0+ requires additional plugin setup)
- **AndroidX**: Your project must use AndroidX (not old `android.support`)

---

## Quick Setup

If you already know Compose setup, here's the minimal configuration:

**Root `build.gradle` or `build.gradle.kts`:**
```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}
```

**App `build.gradle` or `build.gradle.kts`:**
```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    buildFeatures {
        compose = true
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

> **⚠️ Kotlin 2.0+ Users**: The `org.jetbrains.kotlin.plugin.compose` Gradle plugin is **required** when using Kotlin 2.0 or higher. See [Compose Compiler Setup](#step-2b-compose-compiler-setup-kotlin-20-vs-19x) for details.

---

## Step-by-Step Integration

### Step 1 — Add JitPack Repository

OTPView is hosted on JitPack. Add it to your **root `settings.gradle` or `settings.gradle.kts`** (not `build.gradle`):

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

> **❌ Common Mistake**: Adding JitPack only to the project-level `build.gradle` `repositories` block. It **must** be in `settings.gradle` with `dependencyResolutionManagement`.

---

### Step 2a — Enable Compose & Add Dependencies (Kotlin 1.9.x)

If you're using **Kotlin 1.9.x**, use the legacy `composeOptions` block:

**Groovy DSL (`build.gradle`):**
```groovy
android {
    compileSdk 35

    defaultConfig {
        minSdk 21
    }

    buildFeatures {
        compose true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
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
    // OTPView library
    implementation 'com.github.pinakcodestudio:OTPView:1.0.0'

    // Compose — required for ComposeView
    implementation platform('androidx.compose:compose-bom:2024.09.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.activity:activity-compose:1.9.2'
}
```

---

### Step 2b — Compose Compiler Setup (Kotlin 2.0+ vs 1.9.x)

**⚠️ CRITICAL: Kotlin 2.0+ requires the Compose Compiler Gradle Plugin**

Starting with Kotlin 2.0, the old `composeOptions.kotlinCompilerExtensionVersion` approach is deprecated. You **must** use the `org.jetbrains.kotlin.plugin.compose` Gradle plugin.

#### For Kotlin 2.0 or Higher (Recommended)

**Root `build.gradle` or `build.gradle.kts`:**

**Groovy DSL:**
```groovy
plugins {
    id 'com.android.application' version '8.7.0' apply false
    id 'org.jetbrains.kotlin.android' version '2.0.21' apply false
    id 'org.jetbrains.kotlin.plugin.compose' version '2.0.21' apply false  // ← ADD THIS
}
```

**Kotlin DSL:**
```kotlin
plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false  // ← ADD THIS
}
```

**App `build.gradle` or `build.gradle.kts`:**

**Groovy DSL:**
```groovy
plugins {
    id 'com.android.application'
    id 'kotlin-android'
    id 'org.jetbrains.kotlin.plugin.compose'  // ← ADD THIS (order matters)
    // ... your other plugins
}

android {
    compileSdk 35

    defaultConfig {
        minSdk 21
    }

    buildFeatures {
        compose true
    }

    // ❌ Remove composeOptions block — not needed with Kotlin 2.0+
    // composeOptions {
    //     kotlinCompilerExtensionVersion = "..."
    // }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation 'com.github.pinakcodestudio:OTPView:1.0.0'
    
    implementation platform('androidx.compose:compose-bom:2024.09.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.activity:activity-compose:1.9.2'
}
```

**Kotlin DSL:**
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")  // ← ADD THIS
    // ... your other plugins
}

android {
    compileSdk = 35
    
    defaultConfig {
        minSdk = 21
    }

    buildFeatures {
        compose = true
    }

    // ❌ Remove composeOptions block — not needed with Kotlin 2.0+

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

#### Kotlin Version Compatibility Reference

| Kotlin Version | Compose Compiler Plugin | `kotlinCompilerExtensionVersion` |
|---------------|------------------------|--------------------------------|
| 2.2.x | `org.jetbrains.kotlin.plugin.compose` 2.2.x | ❌ Not used |
| 2.0.x - 2.1.x | `org.jetbrains.kotlin.plugin.compose` 2.0.x | ❌ Not used |
| 1.9.x | ❌ Not used | "1.5.10" - "1.5.15" |
| 1.8.x | ❌ Not used | "1.4.8" |

> **🔗 Reference**: [Compose Kotlin Compatibility Map](https://developer.android.com/jetpack/androidx/releases/compose-kotlin)

---

### Step 3 — Add ComposeView to XML Layout

Add `ComposeView` anywhere in your XML layout where you want OTPView to appear:

**`res/layout/fragment_otp.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.core.widget.NestedScrollView 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center"
        android:padding="16dp">

        <!-- OTPView hosted inside ComposeView -->
        <androidx.compose.ui.platform.ComposeView
            android:id="@+id/otp_compose_view"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="32dp" />

    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

---

### Step 4 — Wire Up in Activity

```kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import com.pcs.otpview.api.OTPView
import com.pcs.otpview.api.OtpResult
import com.pcs.otpview.api.OtpTheme
import com.pcs.otpview.api.OtpResendConfig

class OtpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        val composeView = findViewById<androidx.compose.ui.platform.ComposeView>(R.id.otp_compose_view)

        composeView.setContent {
            MaterialTheme {
                OTPView(
                    otpLength = 6,
                    theme = OtpTheme.default(),
                    resendConfig = OtpResendConfig(minute = 2),
                    onVerify = { otp ->
                        val ok = apiService.verifyOtp(otp)
                        if (ok) OtpResult.Success
                        else OtpResult.Failure("Incorrect OTP. Please try again.")
                    },
                    onSuccess = {
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

> **Important**: If your Activity uses `setContent { }` (the `activity-compose` extension), **do not** use `ComposeView`. Call `OTPView(...)` directly inside your composable tree instead.

---

### Step 5 — Wire Up in Fragment

For Fragments, use `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed` to prevent memory leaks and state loss:

```kotlin
import android.os.Bundle
import android.view.View
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.pcs.otpview.api.OTPView
import com.pcs.otpview.api.OtpResult
import com.pcs.otpview.api.OtpResendConfig
import com.pcs.otpview.api.OtpTheme

class OtpFragment : Fragment(R.layout.fragment_otp) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val composeView = view.findViewById<androidx.compose.ui.platform.ComposeView>(R.id.otp_compose_view)
        
        // ⚠️ CRITICAL: Set composition strategy for proper lifecycle handling
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )

        composeView.setContent {
            MaterialTheme {
                OTPView(
                    otpLength = 6,
                    theme = OtpTheme.default(),
                    resendConfig = OtpResendConfig(minute = 2),
                    onVerify = { otp ->
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
```

---

## Common Issues & Solutions

### Issue 1: "Compose Compiler Gradle plugin is required"

**Error Message:**
```
Starting in Kotlin 2.0, the Compose Compiler Gradle plugin is required
when compose is enabled. See the following link for more information:
https://d.android.com/r/studio-ui/compose-compiler
```

**Cause:** Using Kotlin 2.0+ without the `org.jetbrains.kotlin.plugin.compose` plugin.

**Solution:**
1. Add plugin to root `build.gradle`:
   ```kotlin
   id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
   ```
2. Apply plugin in app `build.gradle`:
   ```kotlin
   id("org.jetbrains.kotlin.plugin.compose")
   ```
3. Remove `composeOptions { kotlinCompilerExtensionVersion = "..." }` if present.

---

### Issue 2: "Could not find com.github.pinakcodestudio:OTPView:1.0.0"

**Error Message:**
```
Could not resolve all dependencies for configuration ':app:debugRuntimeClasspath'.
> Could not find com.github.pinakcodestudio:OTPView:1.0.0.
```

**Cause:** JitPack repository is not properly configured.

**Solution:**
- Ensure JitPack is in `settings.gradle` (not `build.gradle`) under `dependencyResolutionManagement`
- Check internet connection
- Verify the version number from [releases](https://github.com/pinakcodestudio/OTPView/releases)

---

### Issue 3: "Unresolved reference: ComposeView" or "Unresolved reference: MaterialTheme"

**Cause:** Missing Compose dependencies.

**Solution:** Add Compose BOM and dependencies to app `build.gradle`:
```kotlin
implementation(platform("androidx.compose:compose-bom:2024.09.00"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.activity:activity-compose:1.9.2")
```

---

### Issue 4: "Unresolved reference: OTPView" or "Unresolved reference: OtpResult"

**Cause:** Missing OTPView import or dependency.

**Solution:**
1. Ensure dependency is added:
   ```kotlin
   implementation("com.github.pinakcodestudio:OTPView:1.0.0")
   ```
2. Add imports in your Kotlin file:
   ```kotlin
   import com.pcs.otpview.api.OTPView
   import com.pcs.otpview.api.OtpResult
   import com.pcs.otpview.api.OtpTheme
   import com.pcs.otpview.api.OtpResendConfig
   ```

---

### Issue 5: "Type mismatch: inferred type is Int but OtpResendConfig was expected"

**Cause:** Passing `minute = 2` directly to OTPView without creating `OtpResendConfig`.

**Solution:** Use `OtpResendConfig` object:
```kotlin
resendConfig = OtpResendConfig(minute = 2)
```

---

### Issue 6: OTP state lost when navigating back from another Fragment

**Cause:** Default `DisposeOnDetachedFromWindow` strategy disposes composition when Fragment is on back stack.

**Solution:** Use `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed`:
```kotlin
composeView.setViewCompositionStrategy(
    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
)
```

---

### Issue 7: Keyboard overlaps OTP input boxes

**Cause:** Missing `windowSoftInputMode` configuration.

**Solution:** Add to `AndroidManifest.xml`:
```xml
<activity
    android:name=".MainActivity"
    android:windowSoftInputMode="adjustResize">
</activity>
```

---

## Fragment Integration Best Practices

### Use the Correct Composition Strategy

```kotlin
composeView.setViewCompositionStrategy(
    ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
)
```

**Why?** Fragments on the back stack have their views detached but not destroyed. The default strategy disposes composition on detach, losing OTP state when returning. `DisposeOnViewTreeLifecycleDestroyed` keeps state alive matching Fragment lifecycle.

### Find ComposeView Efficiently

```kotlin
// ✅ Good — findViewById once in onViewCreated
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val composeView = view.findViewById<ComposeView>(R.id.otp_compose_view)
    // ...
}
```

### Handle Configuration Changes

OTPView state is automatically saved across configuration changes (rotation). No additional code required.

---

## Keyboard Handling

### Basic Setup

In `AndroidManifest.xml`:
```xml
<activity
    android:name=".OtpActivity"
    android:windowSoftInputMode="adjustResize" />
```

### Edge-to-Edge Apps (Android 15+)

For apps using edge-to-edge with `WindowCompat.setDecorFitsSystemWindows(window, false)`:

```kotlin
import androidx.compose.foundation.layout.imePadding

composeView.setContent {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding() // Handles keyboard insets
        ) {
            OTPView(
                otpLength = 6,
                onVerify = { otp -> apiService.verify(otp) },
                onSuccess = { finish() }
            )
        }
    }
}
```

---

## Complete Examples

### Minimal Activity Example

```kotlin
class MinimalOtpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)

        findViewById<ComposeView>(R.id.otp_compose_view).apply {
            setContent {
                MaterialTheme {
                    OTPView(
                        otpLength = 6,
                        onVerify = { otp ->
                            if (otp == "123456") OtpResult.Success
                            else OtpResult.Failure("Incorrect OTP")
                        },
                        onSuccess = { finish() }
                    )
                }
            }
        }
    }
}
```

### Full Fragment with Navigation

```kotlin
class OtpVerificationFragment : Fragment(R.layout.fragment_otp) {

    private val viewModel: AuthViewModel by viewModels()
    private val phoneNumber: String by lazy {
        arguments?.getString("phoneNumber") ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ComposeView>(R.id.otp_compose_view).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )

            setContent {
                MaterialTheme {
                    OTPView(
                        otpLength = 6,
                        title = "Verify your number",
                        subtitle = "Enter the 6-digit code sent to $phoneNumber",
                        theme = OtpTheme.default(),
                        resendConfig = OtpResendConfig(minute = 2),
                        onVerify = { otp ->
                            val result = viewModel.verifyOtp(phoneNumber, otp)
                            if (result.isSuccess) OtpResult.Success
                            else OtpResult.Failure(result.errorMessage)
                        },
                        onSuccess = {
                            findNavController().navigate(
                                R.id.action_otp_to_home
                            )
                        },
                        onResendOtp = {
                            viewModel.resendOtp(phoneNumber)
                        }
                    )
                }
            }
        }
    }
}
```

---

## Troubleshooting Checklist

Before opening an issue, verify:

- [ ] JitPack repository is in `settings.gradle` under `dependencyResolutionManagement`
- [ ] OTPView dependency is in app `build.gradle`
- [ ] Compose BOM and dependencies are included
- [ ] For Kotlin 2.0+: `org.jetbrains.kotlin.plugin.compose` plugin is applied
- [ ] For Kotlin 1.9.x: `kotlinCompilerExtensionVersion` is set in `composeOptions`
- [ ] `buildFeatures { compose = true }` is enabled
- [ ] `compileSdk` is 35 or higher
- [ ] `minSdk` is 21 or higher
- [ ] All imports are correct (`com.pcs.otpview.api.*`)
- [ ] `windowSoftInputMode="adjustResize"` is set in manifest
- [ ] `ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed` is set for Fragments

---

## Getting Help

- **Documentation**: [Full OTPView API Reference](https://github.com/pinakcodestudio/OTPView/blob/main/README.md)
- **Issues**: [GitHub Issues](https://github.com/pinakcodestudio/OTPView/issues)
- **Releases**: [Check latest version](https://github.com/pinakcodestudio/OTPView/releases)

---

## License

MIT License — See [LICENSE](https://github.com/pinakcodestudio/OTPView/blob/main/LICENSE) for details.
