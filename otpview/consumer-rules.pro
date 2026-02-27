# Retain all public API types (sealed subclasses can be stripped by R8 without this)
-keep class com.pcs.otpview.api.** { *; }

# Retain Compose-generated singleton lambda objects
-keep class **$ComposableSingletons** { *; }