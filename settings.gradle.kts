pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "OTPView"

include(":otpview")   // the library module  →  produces com.pcs.otpview:otpview:<version>.aar
include(":sample")    // demo app            →  consumes :otpview locally for testing
