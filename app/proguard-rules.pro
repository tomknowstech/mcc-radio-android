# Add project specific ProGuard rules here.

# Keep Ktor
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class org.mccmarion.radio.**$$serializer { *; }
-keepclassmembers class org.mccmarion.radio.** {
    *** Companion;
}
-keepclasseswithmembers class org.mccmarion.radio.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Media3
-keep class androidx.media3.** { *; }

# SLF4J
-dontwarn org.slf4j.impl.StaticLoggerBinder
