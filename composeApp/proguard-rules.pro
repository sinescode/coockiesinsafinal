# Keep Firebase classes
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# Keep encryption classes
-keep class javax.crypto.** { *; }
-keep class javax.security.auth.** { *; }
-keep class org.bouncycastle.** { *; }

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.** { *; }
-keepclasseswithmembers class com.turjaun.cookiesuploader.data.models.** {
    <init>(...);
    *** get*();
    void set*(***);
}

# Keep HTTP client
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# General rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }