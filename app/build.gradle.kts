import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

val signingPropertiesFile = rootProject.file("keystore.properties")
val signingProperties = Properties().apply {
    if (signingPropertiesFile.exists()) {
        signingPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.sipun.playora"

    compileSdk = 37
    defaultConfig {
        minSdk = 24
        targetSdk = 37
        applicationId = "com.sipun.playora"
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = signingProperties.getProperty("storeFile")
                ?.takeIf { it.isNotBlank() }
                ?: System.getenv("ANDROID_KEYSTORE_FILE")
            if (!keystorePath.isNullOrBlank()) {
                storeFile = rootProject.file(keystorePath)
            }

            storePassword = signingProperties.getProperty("storePassword")
                ?.takeIf { it.isNotBlank() }
                ?: System.getenv("ANDROID_KEYSTORE_PASSWORD")
            keyAlias = signingProperties.getProperty("keyAlias")
                ?.takeIf { it.isNotBlank() }
                ?: System.getenv("ANDROID_KEY_ALIAS")
            keyPassword = signingProperties.getProperty("keyPassword")
                ?.takeIf { it.isNotBlank() }
                ?: System.getenv("ANDROID_KEY_PASSWORD")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        getByName("debug") {
            isDebuggable = true
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.biometric)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.lifecycle.viewModel.ktx)

    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.core.pip)

    implementation(libs.coil.compose)
    implementation(libs.reorderable)

    // Material Icons Extended
    implementation(libs.androidx.compose.material.iconsExtended)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore & Serialization
    implementation(libs.androidx.datastore.core)
    implementation(libs.kotlinx.serialization.json)

    // Media3
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.ui.compose)
    implementation(libs.androidx.media3.session)
    implementation(libs.nextlib.extended.media3ext)
    implementation(libs.nextlib.extended.mediainfo)
    implementation(libs.github.albfernandez.juniversalchardet)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.annotations)
    implementation(libs.koin.compose.viewmodel)

    implementation(libs.accompanist.permissions)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.guava)

    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
}
