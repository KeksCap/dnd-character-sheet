# --- Retrofit 2.9.0 ---
-keepattributes Signature, InnerClasses, AnnotationDefault
-keepattributes *Annotation*
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }

# --- Gson 2.13.2 ---
-keepattributes EnclosingMethod
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.** { *; }

# --- Room 2.8.4 ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }
-keep class com.example.dndhelper.data.** { *; } # Сохраняем DAO и сущности

# --- Jetpack Compose ---
-keep class androidx.compose.runtime.ParcelableSnapshotState { *; }
-keep @androidx.compose.runtime.Composable class * { *; }
-dontwarn androidx.compose.**

# --- Data Models (Защита от поломки Room и JSON) ---
# Так как твои модели лежат в пакете com.example.dndhelper.data, защищаем их целиком
-keep class com.example.dndhelper.data.** { *; }
-keepclassmembers class com.example.dndhelper.data.** { *; }

# Защита для сгенерированного кода (ZXing и прочее)
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.google.zxing.** { *; }
