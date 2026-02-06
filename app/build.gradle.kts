import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Load .env file
val envFile = rootProject.file(".env")
val envProperties = Properties().apply {
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
}

fun getEnvOrDefault(key: String, default: String = ""): String {
    return envProperties.getProperty(key) ?: System.getenv(key) ?: default
}

android {
    namespace = "com.sayar.assistant"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.sayar.assistant"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        // Inject .env variables into BuildConfig
        // Google OAuth
        buildConfigField("String", "GOOGLE_ANDROID_CLIENT_ID", "\"${getEnvOrDefault("GOOGLE_ANDROID_CLIENT_ID")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${getEnvOrDefault("GOOGLE_WEB_CLIENT_ID")}\"")

        // AI APIs
        buildConfigField("String", "OPENAI_API_KEY", "\"${getEnvOrDefault("OPENAI_API_KEY")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${getEnvOrDefault("GEMINI_API_KEY")}\"")
        buildConfigField("String", "OLLAMA_BASE_URL", "\"${getEnvOrDefault("OLLAMA_BASE_URL", "http://localhost:11434")}\"")

        // Google Drive
        buildConfigField("Boolean", "GOOGLE_DRIVE_ENABLED", getEnvOrDefault("GOOGLE_DRIVE_ENABLED", "false"))
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/INDEX.LIST"
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // DataStore
    implementation(libs.datastore.preferences)

    // Google Auth & Drive
    implementation(libs.google.auth)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services)
    implementation(libs.google.api.client)
    implementation(libs.google.drive)

    // ML Kit
    implementation(libs.mlkit.text.recognition)

    // Image loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
