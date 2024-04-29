#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keep *Annotation*, Javadoc

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-keep class com.appsflyer.** { *; }
-keep class kotlin.jvm.internal.** { *; }
-keep public class com.android.installreferrer.** { *; }c
