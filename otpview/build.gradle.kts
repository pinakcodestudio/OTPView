plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    `maven-publish`
}

android {
    namespace  = "com.pcs.otpview"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    kotlin { explicitApi() }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId    = libs.versions.libraryGroup.get()
                artifactId = libs.versions.libraryArtifactId.get()
                version    = libs.versions.libraryVersion.get()

                pom {
                    name.set("OTPView")
                    description.set("Production-ready OTP & PIN input component for Jetpack Compose")
                    url.set("https://github.com/pinakcodestudio/OTPView")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    developers {
                        developer {
                            id.set("pinakcodestudio")
                            name.set("Pinak Code Studio")
                            email.set("pinakcodestudio@gmail.com")
                        }
                    }
                    scm {
                        connection.set("scm:git:github.com/pinakcodestudio/OTPView.git")
                        developerConnection.set("scm:git:ssh://github.com/pinakcodestudio/OTPView.git")
                        url.set("https://github.com/pinakcodestudio/OTPView/tree/main")
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.foundation)
    implementation(libs.compose.animation)
    implementation(libs.compose.material3)
    // lifecycle-runtime-compose for collectAsStateWithLifecycle (still used in tests/future)
    implementation(libs.androidx.lifecycle.compose)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.tooling.preview)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
