rootProject.name = "ViaBlueberry"

pluginManagement {
    repositories {
        // mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://repo.blueberrymc.net/repository/maven-public/") }
    }
}

include("v1_20")
include("v1_19")
include("common")
