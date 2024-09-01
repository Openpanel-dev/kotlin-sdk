plugins {
    id("com.android.library") version "8.1.0"
    kotlin("android") version "1.9.20"
    id("org.jetbrains.kotlin.jvm") version "1.9.20" apply false 
}

android {
    namespace = "dev.openpanel" 
    compileSdk = 33 // Use the latest stable SDK version

    defaultConfig {
        minSdk = 24 // Minimum SDK version you want to support
        targetSdk = 33 // Target SDK version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility("17")
        targetCompatibility("17")
    }

    kotlinOptions {
        jvmTarget="17"
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.json:json:20210307")

    // AndroidX dependencies
    implementation("androidx.core:core-ktx:1.10.0")
    implementation("androidx.appcompat:appcompat:1.6.1")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation(project(":main"))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}
