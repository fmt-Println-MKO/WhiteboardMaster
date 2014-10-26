# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/matthiaskoch/Development/adt-bundle-mac-x86_64-20140321/sdk/tools/proguard/proguard-android.txt
# You can edit the include imageFileName and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#-keep class org.apache.http.** {*;}
#-dontwarn org.apache.http.**
#-dontwarn net.spy.memcached.**
#-dontwarn net.sf.ehcache.**
#-dontwarn org.ietf.jgss.**
#-dontwarn javax.naming.**
#-dontwarn javax.servlet.**
#-keep class org.apache.log.** {*;}
#-dontwarn org.apache.log.**
#-keep class org.apache.log4j.** {*;}
#-dontwarn org.apache.log4j.**
#-dontwarn org.apache.avalon.**
