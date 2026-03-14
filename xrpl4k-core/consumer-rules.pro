# Keep annotations and inner classes metadata (required by kotlinx.serialization)
-keepattributes *Annotation*, InnerClasses

# Suppress notes about internal kotlinx.serialization API
-dontnote kotlinx.serialization.AnnotationsKt

# Keep kotlinx.serialization JSON internals
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep generated $serializer classes for all SDK types
-keep,includedescriptorclasses class org.xrpl.sdk.**$$serializer { *; }

# Keep Companion objects on all SDK classes (serializer() lives there)
-keepclassmembers class org.xrpl.sdk.** {
    *** Companion;
}

# Keep KSerializer factory methods on all SDK classes
-keepclasseswithmembers class org.xrpl.sdk.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all @Serializable-annotated classes and their members
-keep @kotlinx.serialization.Serializable class org.xrpl.** { *; }

# Keep enum entries (serialized by name)
-keepclassmembers enum org.xrpl.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    *;
}
