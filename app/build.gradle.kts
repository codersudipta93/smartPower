plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
//    id("dagger.hilt.android.plugin")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")

}

android {
    namespace = "com.example.parkingagent"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.parkingagent"
        minSdk = 28
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
    buildFeatures{
        viewBinding=true
        dataBinding=true
    }
}

//kapt {
//    correctErrorTypes = true // Recommended for Hilt
//    useBuildCache = true // Speeds up builds by caching annotation processing results
//    mapDiagnosticLocations = true // Maps errors to the source files
//}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)

    implementation(libs.androidx.security.crypto.ktx)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidthings)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.retrofit)
    implementation(libs.gson)
    implementation(libs.converter.gson)
    implementation(libs.converter.scalars)
    implementation(libs.logging.interceptor)
    implementation(libs.okhttp)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation (libs.kotlinx.coroutines.android)

    // Coroutine Lifecycle Scopes
    implementation (libs.androidx.lifecycle.viewmodel.ktx)

    // Activity KTX for viewModels()
    implementation (libs.androidx.activity.ktx)

    //Dagger - Hilt
    implementation (libs.hilt.android)
    kapt (libs.hilt.android.compiler)
    kapt (libs.androidx.hilt.compiler)

    // navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.dynamic.features.fragment)


    implementation(libs.printerx)

    implementation(libs.lottie)
    implementation(libs.dotlottie.android)

    implementation(libs.sdp.android)

    implementation(libs.androidx.security.crypto.ktx)

    implementation(libs.zxing.android.embedded)



//    implementation(libs.paylib.release)

    implementation ("com.google.android.material:material:1.9.0")
    implementation(files("libs/PayLib-release-2.0.17.aar"))

    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")


    // SUNMI Pay SDK (local AAR)
//    implementation(files("libs/PayLib-release-2.0.17.aar"))  // Uncomment when using local AAR

    // EMV Split Library configuration
//    implementation(fileTree(mapOf(
//        "dir" to "libs",
//        "include" to listOf("*.jar")
//    )))
//
//    implementation(files("libs/sunmiemvl2split-1.0.1.jar"))
}