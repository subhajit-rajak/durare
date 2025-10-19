import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

val localProperties = rootProject.file("local.properties").inputStream().use { input ->
    Properties().apply { load(input) }
}
val openRouterApiKey: String = localProperties.getProperty("OPENROUTER_API_KEY") ?: "\"\""

android {
    namespace = "com.subhajitrajak.durare"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.subhajitrajak.durare"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "OPENROUTER_API_KEY", "\"$openRouterApiKey\"")
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
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ML Kit Face Detection
    implementation(libs.face.detection)
    
    // CameraX dependencies
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // fragment navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // flexbox layout
    implementation(libs.flexbox)

    // circle image view
    implementation(libs.circleimageview)

    // dots indicator
    implementation(libs.dotsindicator)

    // firebase auth
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    // firestore
    implementation(libs.firebase.firestore.ktx)
    // listenable future support
    implementation(libs.androidx.concurrent.futures)
    implementation(libs.guava.listenablefuture)
    implementation(libs.guava.android)

    // glide
    implementation(libs.glide)

    // swipe to refresh layout
    implementation(libs.androidx.swiperefreshlayout)

    // lottie
    implementation(libs.lottie)

    // splash api
    implementation(libs.androidx.core.splashscreen)

    // vico charts
    implementation(libs.vico.views)

    // room
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // markdown
    implementation(libs.noties.markwon.core)
}