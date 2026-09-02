import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.screenshot)
}

val reviewSigningProperties =
    Properties().apply {
        rootProject
            .file("keystore.properties")
            .takeIf { it.isFile }
            ?.inputStream()
            ?.use(::load)
    }
val reviewKeystorePath =
    providers.environmentVariable("DOPE_REVIEW_KEYSTORE").orNull
        ?: reviewSigningProperties.getProperty("storeFile")
val reviewStorePassword =
    providers.environmentVariable("DOPE_REVIEW_STORE_PASSWORD").orNull
        ?: reviewSigningProperties.getProperty("storePassword")
val reviewKeyAlias =
    providers.environmentVariable("DOPE_REVIEW_KEY_ALIAS").orNull
        ?: reviewSigningProperties.getProperty("keyAlias")
val reviewKeyPassword =
    providers.environmentVariable("DOPE_REVIEW_KEY_PASSWORD").orNull
        ?: reviewSigningProperties.getProperty("keyPassword")
val reviewKeystoreFile = reviewKeystorePath?.let(rootProject::file)
val reviewSigningReady =
    listOf(reviewKeystorePath, reviewStorePassword, reviewKeyAlias, reviewKeyPassword).all { !it.isNullOrBlank() } &&
        requireNotNull(reviewKeystoreFile).isFile

android {
    namespace = "za.co.dope.ballistics"
    compileSdk = 37

    defaultConfig {
        applicationId = "za.co.bdstudio.dope"
        minSdk = 28
        targetSdk = 37
        versionCode = 15
        versionName = "0.7.0-m7-review"

        buildConfigField("boolean", "OPEN_METEO_ENABLED", "true")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    signingConfigs {
        if (reviewSigningReady) {
            create("review") {
                storeFile = requireNotNull(reviewKeystoreFile)
                storePassword = reviewStorePassword
                keyAlias = reviewKeyAlias
                keyPassword = reviewKeyPassword
            }
        }
    }

    buildTypes {
        debug {
            if (reviewSigningReady) signingConfig = signingConfigs.getByName("review")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        abortOnError = true
        warningsAsErrors = true
        checkReleaseBuilds = true
        disable += "AndroidGradlePluginVersion"
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(project(":ballistics"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.video)
    implementation(libs.google.ar.core)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.serialization.json)

    ksp(libs.androidx.room.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit4)
    testImplementation(libs.androidx.test.core)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.room.testing)

    screenshotTestImplementation(libs.screenshot.validation.api)
    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
}
