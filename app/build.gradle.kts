plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.happyfridge"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.happyfridge"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation (libs.navigation.fragment)
    implementation (libs.navigation.ui)
    implementation (libs.lifecycle.viewmodel)
    implementation (libs.lifecycle.livedata)
    annotationProcessor (libs.lifecycle.compiler)
    implementation(libs.room.runtime)
    annotationProcessor (libs.room.compiler)
}
