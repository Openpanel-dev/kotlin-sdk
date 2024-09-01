pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google() // Add Google Maven repository
    }
}

rootProject.name = "dev.openpanel"
include("main", "android")

