# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 混淆请在主模块中添加
##保持自定义控件类不被混淆
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet);
#}
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
#-keep public class * extends android.view.View{
#    *** get*();
#    void set*(***);
#    public <init>(android.content.Context);
#    public <init>(android.content.Context, android.util.AttributeSet);
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
##引用的v4\v7包不被混淆
#-dontwarn android.support.**
#-dontwarn android.support.v4.**
#-keep class android.support.**{*;}
#-keep class android.support.v4.** { *; }
##Service
#-keep public class * extends android.app.Service
#-keep public class * extends android.os.Binder
##java对象
#-keep class com.music.player.lib.bean.**{*;}
##保持 Serializable 不被混淆
#-keepnames class * implements java.io.Serializable
##保持 Serializable 不被混淆并且enum 类也不被混淆
#-keepclassmembers class * implements java.io.Serializable {
#    static final long serialVersionUID;
#    private static final java.io.ObjectStreamField[] serialPersistentFields;
#    !static !transient <fields>;
#    !private <fields>;
#    !private <methods>;
#    private void writeObject(java.io.ObjectOutputStream);
#    private void readObject(java.io.ObjectInputStream);
#    java.lang.Object writeReplace();
#    java.lang.Object readResolve();
#}
##图片加载
#-keep public class * implements com.bumptech.glide.module.GlideModule
#-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
#    **[] $VALUES;
#    public *;
#}
##金山云MediaPlayer
#-keep class com.ksyun.media.player.**{ *; }
#-keep class com.ksy.statlibrary.**{ *;}
##用到的反射
#-keep class com.music.player.lib.service.** { *; }