# Library module ProGuard rules — only applied during development builds of the :otpview module.
# These rules are NOT packaged into the AAR and do NOT affect consumer apps.
# Consumer ProGuard rules belong in consumer-rules.pro.

# Keep the public API surface (same as consumer-rules.pro, for consistency in dev builds)
-keep public class com.pcs.otpview.api.** { public *; }
