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

# POI Excel library rules
-dontwarn java.awt.**
-dontwarn javax.xml.stream.**
-dontwarn net.sf.saxon.**
-dontwarn org.apache.batik.**
-dontwarn org.osgi.framework.**

# Keep POI classes
-keep class org.apache.poi.** { *; }
-keep class org.apache.xmlbeans.** { *; }

# ZXing barcode library
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }

# Room database
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Camera2 API
-keep class androidx.camera.** { *; }

# Dexter permissions
-keep class com.karumi.dexter.** { *; }