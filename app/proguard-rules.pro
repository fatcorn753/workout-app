# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.tatu.workout.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.tatu.workout.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
