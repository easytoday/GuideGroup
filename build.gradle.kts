// build.gradle.kts (Project: GuideGroup)

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.secrets.gradle.plugin) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    alias(libs.plugins.ksp) apply false // <<-- AJOUTÉ
}

// Les dépôts sont déjà définis dans settings.gradle.kts sous pluginManagement et dependencyResolutionManagement.
// Il n'est pas nécessaire de les répéter ici.
buildscript {
    repositories {
        // google() // <-- SUPPRIMER OU COMMENTER
        // mavenCentral() // <-- SUPPRIMER OU COMMENTER
    }
    dependencies {
        // Ces classpaths sont souvent redondants avec le bloc plugins {} en haut.
        // Vous pouvez les laisser commentés ou les supprimer si vous êtes sûr qu'ils ne sont pas utilisés.
        // classpath("com.android.tools.build:gradle:8.1.2")
        // classpath("com.google.gms:google-services:4.4.0")
        // classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
        // classpath("androidx.room:room-compiler:2.7.1")
    }
}

// allprojects { ... } est également redondant si dependencyResolutionManagement est utilisé dans settings.gradle.kts.
// Comme votre settings.gradle.kts a dependencyResolutionManagement, ce bloc est inutile.
// allprojects {
//     repositories {
//         // google() // <-- SUPPRIMER OU COMMENTER
//         // mavenCentral() // <-- SUPPRIMER OU COMMENTER
//     }
// }

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}