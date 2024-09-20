plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mabbureau.panoptes"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mabbureau.panoptes"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    

    // TensorFlow Lite (Optional, if using ML for feature detection)
    implementation (libs.tensorflow.lite)

// Camera Libraries for facial detection
    implementation (libs.camera.core)
    implementation (libs.camera.camera2)
    implementation (libs.camera.lifecycle)
    implementation (libs.androidx.camera.view )
    implementation (libs.material.vlatestversion)
    implementation (libs.face.detection)
//    Biometric detection library
    implementation (libs.biometric)
//    Default android libs
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
//    Navigation libs
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)



    // For lifecycle-aware components
    implementation (libs.androidx.lifecycle.runtime.ktx)
}