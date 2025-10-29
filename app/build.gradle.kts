plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.mushroom_lab.MushroomApp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.mushroomlab.MushroomApp"
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
    buildFeatures {
        viewBinding = true
    }
}
dependencies {
    implementation("org.osmdroid:osmdroid-android:6.1.6")
    //implementation("org.osmdroid:osmdroid-wms:6.1.6")
    //implementation("org.osmdroid:osmdroid-mapsforge:6.1.6")
    //implementation("org.osmdroid:osmdroid-geopackage:6.1.6")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.location)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}