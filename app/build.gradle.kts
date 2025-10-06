plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.pictaboo.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pictaboo.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        // Tetap menggunakan Java 11
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
        // Mengatur versi bahasa Kotlin untuk stabilitas Kapt
        languageVersion = "1.9"
    }
}

dependencies {
    // ANDROIDX UTILITIES
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // TESTING
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // FIREBASE (Hanya Auth yang dipertahankan)
    implementation("com.google.firebase:firebase-auth:23.0.0")

    // START: ROOM DATABASE (LOKAL)
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
    implementation("androidx.room:room-ktx:$room_version") // Dukungan Coroutines
    kapt("androidx.room:room-compiler:$room_version")
    // END: ROOM DATABASE

    // START: GLIDE (LOAD GAMBAR)
    val glide_version = "4.16.0"
    implementation("com.github.bumptech.glide:glide:$glide_version")
    annotationProcessor("com.github.bumptech.glide:compiler:$glide_version")    // END: GLIDE

    // START: CAMERA X
    val camerax_version = "1.3.0"
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-extensions:$camerax_version")

    // CAMERA X FIXES
    implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
    implementation("com.google.guava:guava:33.2.1-android")
    implementation("androidx.exifinterface:exifinterface:1.3.6")
    // END: CAMERA X
}