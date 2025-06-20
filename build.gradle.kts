// Top-level build file where you can add configuration options common to all sub-projects/modules.
// build.gradle.kts (root project)

buildscript {
    // Repositories are now defined in settings.gradle.kts -> pluginManagement
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        classpath("com.google.gms:google-services:4.4.1")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.50")
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
}

// The allprojects block for repositories is now redundant and should be removed,
// as dependency repositories are defined in settings.gradle.kts
// allprojects {
//     repositories {
//         google()
//         mavenCentral()
//         maven { url = uri("https://repo.itextsupport.com/releases") }
//         maven { url = uri("https://repo1.maven.org/maven2") }
//     }
// }

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}