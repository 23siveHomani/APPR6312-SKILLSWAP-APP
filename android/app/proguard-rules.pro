# SkillSwap ProGuard & R8 rules for Play Store Release Builds

# Keep data models intact for Gson serialization/deserialization
-keep class com.skillswap.app.data.model.** { *; }

# Keep Room entities and DAOs
-keep class com.skillswap.app.data.local.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>();
}

# Retrofit / OkHttp rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# Firebase
-dontwarn com.google.firebase.**
