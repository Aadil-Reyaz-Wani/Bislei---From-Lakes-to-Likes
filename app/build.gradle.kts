import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    id("dagger.hilt.android.plugin") // Added Hilt plugin
    id("org.jetbrains.kotlin.kapt") // Required for Hilt compiler
    id("kotlin-parcelize")
}

// Load the API key from apikey.properties
val apiKeyPropertiesFile = rootProject.file("apikey.properties")
val apiKeyProperties = Properties().apply {
    load(FileInputStream(apiKeyPropertiesFile))
}

android {
    namespace = "com.kashmir.bislei"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kashmir.bislei"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Inject the MAPS_API_KEY into the manifest
        manifestPlaceholders["MAPS_API_KEY"] = apiKeyProperties.getProperty("MAPS_API_KEY")
    }

    configurations.all {
        resolutionStrategy {
            force("androidx.compose.animation:animation-lint:1.8.0-rc03")
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.play.services.location)
    implementation(libs.play.services.maps)

    // Compose Runtime & LiveData
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.runtime.livedata)

    // Maps Compose
    implementation(libs.maps.compose.v641)

    // Accompanist Permissions
    implementation(libs.accompanist.permissions)

    // Coil (for image loading)
    implementation(libs.coil.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Icons & Animations
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.storage)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.material3)
    implementation(libs.play.services.maps.v1700)
    implementation(libs.play.services.location.v1700)

    implementation(libs.accompanist.swiperefresh)



}
