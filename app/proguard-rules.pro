# Add project specific ProGuard rules here.
# Room / Hilt / Compose are handled by their consumer rules.

-keepattributes *Annotation*
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
