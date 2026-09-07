# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.fatcorn753.workout.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.fatcorn753.workout.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
