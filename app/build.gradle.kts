plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.safeArgs)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.glitchcraftlabs.qrstorage"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.glitchcraftlabs.qrstorage"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.android.compiler)
    implementation(libs.glide)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.room.runtime)
    ksp (libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    implementation(libs.play.services.code.scanner)
    implementation("com.google.zxing:core:3.5.3")
    implementation(libs.audience.network.sdk)

    implementation(platform(libs.firebase.bom))
    //implementation (libs.firebase.ui.auth)
    implementation(libs.firebase.auth)
    implementation("com.google.android.gms:play-services-auth:21.2.0")

}