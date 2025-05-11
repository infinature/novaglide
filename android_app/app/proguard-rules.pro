# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in build.gradle.
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If you use reflection or JNI in an SDK, see the SDK documentation or
# contact the SDK vendor for specific ProGuard rules.
# Using Jackson JSON processor?
#-keep class org.codehaus.jackson.** { *; }
#-keepnames class org.codehaus.jackson.** { *; }

# Using Google GSON library?
#-keep class com.google.gson.stream.** { *; }

# Application classes that will be serialized/deserialized over Gson
#-keep class com.yourpackage.YourClass
#-keep class com.yourpackage.YourOtherClass

# If you are using Realm, you must add the following lines:
#-keep class io.realm.annotations.RealmModule
#-keep @io.realm.annotations.RealmModule class *
#-keep class io.realm.internal.Keep
#-keep @io.realm.internal.Keep class *
#-dontwarn javax.**
#-dontwarn io.realm.**

# If you are using Retrofit, uncomment the following lines
#-dontwarn retrofit2.**
#-keep class retrofit2.** { *; }
#-keepattributes Signature
#-keepattributes Exceptions

# If using RxJava
#-dontwarn sun.misc.**
#-keepclassmembers class rx.internal.util.unsafe.* {
#    public static int JPM_MAX_BUCKET_SIZE;
#}
#-keepclassmembers class rx.internal.util.atomic.SppscLinkedArrayQueue {
#    long p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15;
#}
#-keepclassmembers class rx.internal.util.atomic.SpscLinkedArrayQueue {
#    long p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15;
#}


# For Kotlin Coroutines, if you are using ProGuard and R8, you might need the following rules:
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.flow.** { *; }
-keepclassmembers class kotlinx.coroutines.flow.** { *; }
-keep class kotlinx.coroutines.flow.internal.SafeCollector_commonKt

# For Jetpack Compose, ensure the following rules are present if ProGuard/R8 is enabled.
# (These might be included by default with AGP 7.0+)
-keep class androidx.compose.runtime.Composable
-keep class androidx.compose.runtime.NonRestartableComposable
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
    @androidx.compose.runtime.NonRestartableComposable <methods>;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keepclassmembers class androidx.compose.ui.tooling.preview.PreviewParameterProvider
-keepclassmembers class * implements androidx.compose.ui.tooling.preview.PreviewParameterProvider { <init>(...); } 