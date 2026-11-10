

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.dazliss.thermatrack"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dazliss.thermatrack"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }
}
