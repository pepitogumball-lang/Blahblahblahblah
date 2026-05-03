# Reglas ProGuard para FileExplorer
# Esta app es de uso privado, la ofuscación no es crítica,
# pero se mantienen reglas básicas por compatibilidad.

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }

# Compose
-keep class androidx.compose.** { *; }
