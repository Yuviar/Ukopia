plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.ukopia"
    compileSdk = 36

    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.example.ukopia"
        minSdk = 24
        targetSdk = 36
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
    implementation(libs.firebase.auth)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Firebase Bill of Materials (BoM) - Ini sudah benar
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))

    // Anda bisa menghapus analytics jika tidak diperlukan
    implementation("com.google.firebase:firebase-analytics")

    // WAJIB: Tambahkan dependensi ini untuk Firebase Authentication (Login, Register)
    implementation("com.google.firebase:firebase-auth-ktx")

    // WAJIB: Tambahkan dependensi ini untuk Firestore Database (Menyimpan data user)
    implementation("com.google.firebase:firebase-firestore-ktx")

}