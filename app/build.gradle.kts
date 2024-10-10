plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.colorietracker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.colorietracker"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
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
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.appcompat)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.v100alpha01)
    implementation(libs.androidx.datastore.preferences.core)
    implementation(libs.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.datastore.preferences)
    implementation (libs.play.services.location)
    implementation(libs.kotlinx.coroutines.android)



    val lifecycle_version = "2.7.0"
        val arch_version = "2.2.0"

        // ViewModel
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        // ViewModel utilities for Compose
        implementation(libs.androidx.lifecycle.viewmodel.compose)
        // LiveData
        implementation(libs.androidx.lifecycle.livedata.ktx)
        // Lifecycles only (without ViewModel or LiveData)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        // Lifecycle utilities for Compose
        implementation(libs.androidx.lifecycle.runtime.compose)

        // Saved state module for ViewModel
        implementation(libs.androidx.lifecycle.viewmodel.savedstate)

        // Annotation processor
        kapt("androidx.lifecycle:lifecycle-compiler:$lifecycle_version")
        // alternately - if using Java8, use the following instead of lifecycle-compiler
        implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycle_version")

        // optional - helpers for implementing LifecycleOwner in a Service
        implementation("androidx.lifecycle:lifecycle-service:$lifecycle_version")

        // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
        implementation("androidx.lifecycle:lifecycle-process:$lifecycle_version")

        // optional - ReactiveStreams support for LiveData
        implementation("androidx.lifecycle:lifecycle-reactivestreams-ktx:$lifecycle_version")

        // optional - Test helpers for LiveData
        testImplementation("androidx.arch.core:core-testing:$arch_version")

        // optional - Test helpers for Lifecycle runtime
        testImplementation ("androidx.lifecycle:lifecycle-runtime-testing:$lifecycle_version")


    val work_version = "2.9.0"

    // (Java only)
    implementation(libs.androidx.work.runtime)

    // Kotlin + coroutines
    implementation(libs.androidx.work.runtime.ktx)


    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")

    kapt("androidx.room:room-compiler:$roomVersion")
    implementation (libs.play.services.maps)

}