plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
}

android {
    namespace = "com.example.videorecordapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.videorecordapp"
        minSdk = 27
        targetSdk = 35
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
    implementation(libs.accompanist.permissions)
    implementation(libs.kotlinx.coroutine.guava)
//
//    val cameraX = "1.4.0"
//
//    implementation("androidx.camera:camera-core:$cameraX")
//    implementation("androidx.camera:camera-camera2:$cameraX")
//    implementation("androidx.camera:camera-lifecycle:$cameraX")
//
//    implementation("androidx.camera:camera-view:$cameraX") {
//        exclude("androidx.camera", "camera-video")
//    }
//
//    val thinkletCameraX = "1.4.0"
//    implementation("ai.fd.thinklet:camerax-camera-video:$thinkletCameraX")
//
//    val thinkletSdk = "0.1.6"
//    val thinkletCameraXMic = "0.0.1"
//
//    implementation("ai.fd.thinklet:camerax-mic-core:$thinkletCameraXMic")
//    implementation("ai.fd.thinklet:sdk-audio:$thinkletSdk")
//    implementation("ai.fd.thinklet:camerax-mic-multi-channel:$thinkletCameraXMic")
//    implementation("ai.fd.thinklet:camerax-mic-xfe:$thinkletCameraXMic")

    // AndroidX標準のCameraXを追加
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    // 代わりに，THINKLETカスタムのCamera－Videoを追加．
    implementation(thinkletLibs.camerax.video)

    // THINKLET向けのマイクを追加
    implementation(thinkletLibs.camerax.mic.core)
    implementation(thinkletLibs.camerax.mic.multi.channel)
    implementation(thinkletLibs.camerax.mic.xfe)
    // THINKLET向けのSDKを追加
    implementation(thinkletLibs.sdk.audio)

    implementation("androidx.lifecycle:lifecycle-process:2.7.0")
    implementation("androidx.lifecycle:lifecycle-service:2.7.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
}