import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val secretProperties = Properties().apply {
    listOf("local.properties", "secrets.properties").forEach { fileName ->
        val propFile = rootProject.file(fileName)
        if (propFile.exists()) {
            propFile.inputStream().use { load(it) }
        }
    }
}

fun resolveSecret(name: String): String {
    val fromProps = secretProperties.getProperty(name)
    if (!fromProps.isNullOrBlank()) return fromProps.trim()
    return providers.environmentVariable(name).orNull?.trim().orEmpty()
}

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(project(":shared"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

android {
    namespace = "com.lumatech.healthup"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.lumatech.healthup"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 2
        versionName = "1.1.0"
        manifestPlaceholders["MAPS_API_KEY"] = resolveSecret("MAPS_API_KEY")
    }
    val keystorePath = resolveSecret("KEYSTORE_FILE")
    val keystorePassword = resolveSecret("KEYSTORE_PASSWORD")
    val releaseKeyAlias = resolveSecret("KEY_ALIAS")
    val releaseKeyPassword = resolveSecret("KEY_PASSWORD")
    val resolvedKeystoreFile = if (keystorePath.isNotEmpty()) rootProject.file(keystorePath) else null
    val hasReleaseSigning = resolvedKeystoreFile != null &&
        resolvedKeystoreFile.exists() &&
        keystorePassword.isNotEmpty() &&
        releaseKeyAlias.isNotEmpty() &&
        releaseKeyPassword.isNotEmpty()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = resolvedKeystoreFile
                storePassword = keystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    buildFeatures {
        compose = true
    }
}