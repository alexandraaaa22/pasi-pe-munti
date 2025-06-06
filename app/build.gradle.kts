plugins {
    id("kotlin-kapt")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.pasipemunti"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pasipemunti"
        minSdk = 26
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
        compose = true
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.ui.tooling.preview.android)
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.androidx.room.runtime.android)
    implementation(libs.androidx.animation.core.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

//    val material3_version = "1.3.2"
//    implementation("androidx.compose.material3:material3:$material3_version")
//
//    val compose_version = "1.8.0"
//    implementation("androidx.compose.material:material-icons-extended:1.5.0")
//    implementation("androidx.compose.ui:ui:$compose_version")
//    implementation("androidx.compose.material:material:$compose_version")
//    implementation( "androidx.compose.ui:ui-tooling:$compose_version")
//
//    val activity_version = "1.10.1"
//    //implementation("androidx.activity:activity-ktx:1.7.2")
//    implementation("androidx.activity:activity-compose:$activity_version")

    val compose_version = "1.6.5"
    val material3_version = "1.2.1"
    val activity_version = "1.8.2"

    implementation("androidx.compose.ui:ui:$compose_version")
    implementation("androidx.compose.ui:ui-tooling:$compose_version")
    implementation("androidx.compose.material:material:$compose_version")
    implementation("androidx.compose.material:material-icons-extended:$compose_version")

    implementation("androidx.compose.material3:material3:$material3_version")
    implementation("androidx.activity:activity-compose:$activity_version")

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    implementation("org.osmdroid:osmdroid-android:6.1.16")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    //for graphs
    implementation("co.yml:ycharts:2.1.0")

    //maybe better graphs
    //implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

//    implementation("io.data2viz.charts:core:1.1.0-eap1")
//    implementation("io.data2viz.d2v:viz:0.8.12")
//    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    //navigare
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    val room_version = "2.7.1"
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")//
    implementation("androidx.room:room-ktx:$room_version")
}