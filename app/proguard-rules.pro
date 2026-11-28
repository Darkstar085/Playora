-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keep @androidx.annotation.Keep class * { *; }
-keepclassmembers class * {
    @androidx.annotation.Keep <fields>;
    @androidx.annotation.Keep <methods>;
}

-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static final <1>$Companion Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class **$$serializer {
    kotlinx.serialization.KSerializer[] childSerializers();
    kotlinx.serialization.descriptors.SerialDescriptor getDescriptor();
}

-keep class com.sipun.nextlib.** { *; }
-dontwarn com.sipun.nextlib.**

-keep class io.github.itshedimisawi.vitro.** { *; }
-dontwarn io.github.itshedimisawi.vitro.**
