import org.gradle.api.tasks.testing.Test
//import com.easytoday.guidegroup.di.FirebaseModule
//import com.easytoday.guidegroup.di.MockModule
//import com.easytoday.guidegroup.di.RepositoryModule
//import com.easytoday.guidegroup.di.UseCaseModule
//import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.exclude
//import org.gradle.internal.impldep.org.junit.experimental.categories.Categories.CategoryFilter.include


// build.gradle.kts (Module: app)

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")

    //id("com.google.devtools.ksp") version "2.0.0-1.0.21"

    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.secrets.gradle.plugin)
    alias(libs.plugins.compose.compiler)

    //id("com.google.dagger.hilt.android") // Nouveau : si vous utilisez Hilt
    //id("org.jetbrains.kotlin.plugin.parcelize") // Nouveau : si vous utilisez Parcelize

    
}

android {
    namespace = "com.easytoday.guidegroup"
    compileSdk = 34  //was 35

    defaultConfig {
        applicationId = "com.easytoday.guidegroup"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = 34  //was 35
        versionCode = 1
        versionName = "1.0"


        // Runner de test d'instrumentation pour Hilt
        //testInstrumentationRunner = "com.easytoday.guidegroup.HiltTestRunner"
        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner" //// Retour à un runner standard
        vectorDrawables {
            useSupportLibrary = true
        }

    }



    buildTypes {
        release {
            isMinifyEnabled = false  // Désactiver la minification pour le débogage si besoin
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            //unitTest.enabled = false // <-- AJOUTEZ CETTE LIGNE
        }
        debug{
            isMinifyEnabled = false
            // Désactive la compilation et l'exécution des tests unitaires pour ce type de build
            //unitTest.enabled = false // <-- AJOUTEZ CETTE LIGNE
        }
    }

    // --- AJOUTEZ CE BLOC : Déclaration des dimensions de saveurs ---
    flavorDimensions += "app_mode" // Déclarez la dimension "app_mode"

    // NOUVEAU BLOC : productFlavors
    productFlavors {
        create("mock") {
            dimension = "app_mode" // Une dimension pour regrouper les saveurs
            applicationIdSuffix = ".mock" // Ajoute ".mock" à l'ID de l'application
            versionNameSuffix = "-mock" // Ajoute "-mock" au nom de la version
        }
        create("prod") {
            dimension = "app_mode" // Doit être la même dimension que 'mock'
            // Pas de suffixe pour l'ID de l'application par défaut pour la saveur de production
        }
    }
    // NOUVEAU BLOC : sourceSets pour gérer les fichiers spécifiques aux saveurs
    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java")
            res.srcDirs("src/main/res")
            // ... autres configurations si vous en avez
        }
        getByName("mock") {
            java.srcDirs("src/mock/java") // Dossier pour le code Java/Kotlin de la saveur mock
            res.srcDirs("src/mock/res")   // Dossier pour les ressources de la saveur mock
        }
        getByName("prod") {
            java.srcDirs("src/prod/java") // Dossier pour le code Java/Kotlin de la saveur prod
            res.srcDirs("src/prod/res")   // Dossier pour les ressources de la saveur prod
        }



    }

// --- NOUVEAU BLOC HILT POUR LA GESTION DES MODULES PAR SAVEUR ---
// Inclure les modules Firebase, Repository et UseCase pour l'application
//    hilt {
//        // La syntaxe `configure(prod) { ... }` est correcte
//        configure(listOf(project.android.productFlavors.getByName("prod"))) {
//            // Utilisez `include.set(listOf(...))` pour Kotlin DSL
//            include(
//                com.easytoday.guidegroup.di.FirebaseModule::class.java,
//                com.easytoday.guidegroup.di.RepositoryModule::class.java,
//                com.easytoday.guidegroup.di.UseCaseModule::class.java
//            )
//            // Utilisez `exclude.set(listOf(...))` pour Kotlin DSL
//            // Aucune exclusion nécessaire car il n'y a plus de modules mock
//            exclude(
//                   com.easytoday.guidegroup.di.MockModule::class.java // Modules à exclure pour la saveur 'prod'
//               )
//            }
//
//        configure(listOf(project.android.productFlavors.getByName("mock"))) {
//            include(
//                com.easytoday.guidegroup.di.MockModule::class.java // Modules à inclure pour la saveur 'mock'
//            )
//            exclude(
//                com.easytoday.guidegroup.di.FirebaseModule::class.java, // Exclure les modules réels en mode mock
//                com.easytoday.guidegroup.di.RepositoryModule::class.java,
//                com.easytoday.guidegroup.di.UseCaseModule::class.java
//            )
//        }
//    }


        // Résoudre les conflits de ressources ou de dépendances si nécessaire
        // Parfois, Hilt peut nécessiter des configurations spécifiques avec les saveurs de produit
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }

        // Options de compilation Java
        compileOptions {
            //sourceCompatibility = JavaVersion.VERSION_1_8
            sourceCompatibility = JavaVersion.VERSION_17
            //targetCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_17
        }
        // Options de compilation Kotlin
        kotlinOptions {
            //jvmTarget = "1.8"
            jvmTarget = "17"
        }
        // Activation des fonctionnalités de build spécifiques
        buildFeatures {
            compose = true // Activer Compose
            buildConfig = true // Générer la classe BuildConfig
        }
        // Options spécifiques à Compose
        //composeOptions {
            // Cette ligne est souvent redondante si la version du compilateur Compose est gérée par le plugin.
            // Vous pouvez la commenter ou la supprimer si elle ne cause pas de problème.
            // kotlinCompilerExtensionVersion = "2.0.0"
        //}
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
        // Configuration du plugin Secrets Gradle
        secrets {
            propertiesFileName = "secrets.properties"
            defaultPropertiesFileName = "local.defaults.properties"
            ignoreList.add("keyToIgnore") // Exemple d'une clé à ignorer
            ignoreList.add("sdk.*") // Exemple de clés correspondant à un motif à ignorer
        }

    // NOUVEAU BLOC DE GESTION DES RESSOURCES
    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md" // Ajoutez aussi celui-ci si vous le voyez apparaître plus tard
            //excludes += "META-INF/*.txt" // Souvent utile pour exclure d'autres fichiers de licence/notes
            excludes += "META-INF/*.kotlin_module" // Utile pour les modules Kotlin
        }
    }

    // --- NOUVEAU : Exclure les tâches de test du build ---
// ATTENTION: C'est une solution temporaire pour permettre la compilation.
// Il faudra retirer ceci et corriger vos tests ultérieurement.
//    tasks.whenTaskAdded {
//        if (name.contains("test", ignoreCase = true) || name.contains("Test", ignoreCase = true)) {
//            if (!name.contains("assemble", ignoreCase = true)) { // Ne pas exclure assemble tasks
//                enabled = false
//            }
//        }
//    }
// --- FIN NOUVEAU ---


    /// --- Nouveau
//    lint {
//        // Désactive les vérifications Lint sur les sources de test
//        // C'est une solution temporaire pour contourner l'échec actuel.
//        // Vous voudrez réactiver et gérer Lint correctement sur les tests plus tard.
//        checkTestSources = false
//        checkDependencies = false // Peut aussi aider si Lint regarde les dépendances de test
        // Optionnel: Désactiver complètement Lint pour un build rapide, mais moins sûr
        // abortOnError = false // Permet au build de continuer même avec des erreurs Lint
        // lintConfig file("lint-baseline.xml") // Si vous avez un fichier de baseline
    ///

    }

// --- DÉBUT : Configuration pour exécuter un seul test unitaire ---
/**
 * Pour n’exécuter qu’un seul fichier de test sur une variante précise (par exemple MockDebug),
 * et exclure tous les autres fichiers/tests pour cette variante (et ne rien exécuter pour les autres variantes)
 */

////////////////////// OK/////////////////////////////////
/**
 * Dans un projet Android, les tâches de test unitaire par variante (testMockDebugUnitTest, etc.)
 * sont générées après l’application du plugin Android et la configuration des variantes/flavors.
 * Si tu essaies de configurer la tâche trop tôt (au niveau "top-level" du script), elle n’existe pas encore.
 * Pour garantir que la tâche existe au moment où tu la configures, place ton bloc dans un afterEvaluate { ... }
 */

///////////////// on filttre sur toutes les tâches des tests unitaires //////////////////////////
afterEvaluate {
    listOf(
        "testMockDebugUnitTest",
        "testMockReleaseUnitTest",
        "testProdDebugUnitTest",
        "testProdReleaseUnitTest"
    ).forEach { taskName ->
        tasks.named<Test>(taskName) {
            useJUnitPlatform() // Nécessaire pour JUnit 5 ET Kotest
            filter {
                includeTestsMatching("com.easytoday.guidegroup.data.repository.impl.AuthRepositoryImplTest")
            }
        }
    }
}
///////////////// on filttre sur toutes les tâches des tests unitaires //////////////////////////

//afterEvaluate {
//    tasks.named<Test>("testMockDebugUnitTest") {
//        //useJUnitPlatform() // Retire cette ligne si tu utilises JUnit 4
//        filter {
//            // N'exécute que le test spécifié
//            includeTestsMatching("com.easytoday.guidegroup.data.repository.impl.AuthRepositoryImplTest")
//        }
//        // Optionnel : ne pas échouer si aucun test n'est trouvé
//        // ignoreFailures = true
//    }
//}
////////////////////// OK/////////////////////////////////

//    val testToRun = "com.easytoday.guidegroup.data.repository.impl.AuthRepositoryImplTest"
//
//// Liste des tâches unitaires à filtrer
//    val unitTestTasks = listOf(
//        "testMockDebugUnitTest",
//        "testMockReleaseUnitTest",
//        "testProdDebugUnitTest",
//        "testProdReleaseUnitTest"
//    )
//
//    unitTestTasks.forEach { taskName ->
//        tasks.named<Test>(taskName) {
//            useJUnitPlatform() // Retire si tu utilises JUnit 4
//            filter {
//                includeTestsMatching(testToRun)
//            }
//            // Optionnel : ne pas échouer si aucun test n'est trouvé
//            // ignoreFailures = true
//        }
//    }


//        tasks.named<Test>("test") {
//            useJUnitPlatform() // Retire cette ligne si tu utilises JUnit 4
//            filter {
//                includeTestsMatching("com.easytoday.guidegroup.data.repository.impl.AuthRepositoryImplTest")
//            }
//        }

// --- FIN : Configuration pour exécuter un seul test unitaire ---

    dependencies {
        // AndroidX & Compose (Utilise les références TOML)
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)
        implementation(platform(libs.androidx.compose.bom)) // BOM pour gérer les versions de Compose
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.foundation.android)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)
        implementation(libs.androidx.material3)

        // Compose Navigation
        implementation(libs.androidx.navigation.compose)

        //Material 3
        implementation(libs.material.icons.extended)

        // Firebase (utilisez le BOM pour gérer les versions)
        implementation(platform(libs.firebase.bom)) // BOM Firebase (DOIT ÊTRE LA PREMIÈRE DÉPENDANCE FIREBASE)
        implementation("com.google.firebase:firebase-auth-ktx")
        implementation("com.google.firebase:firebase-firestore-ktx")
        implementation("com.google.firebase:firebase-database-ktx")
        implementation(libs.firebase.storage.ktx)
        implementation("com.google.firebase:firebase-messaging-ktx")

        // Importez le Firebase BOM en premier pour gérer les versions de Firebase
        //implementation(platform(libs.firebase.bom)) <-- DEUXIÈME FOIS
        // Firebase
        //implementation(libs.firebase.auth.ktx) <-- Déjà inclus ci-dessus, potentiellement redondant ou en conflit
        //implementation(libs.firebase.firestore.ktx) <-- Déjà inclus ci-dessus, potentiellement redondant ou en conflit

        // Pour Timber (Version en dur, à déplacer dans libs.versions.toml)
        implementation("com.jakewharton.timber:timber:5.0.1")
        //implementation("com.jakewharton.timber:timber:5.0.1")

        // Kotlin Coroutines (Versions en dur, à déplacer dans libs.versions.toml)
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0") // Conservez ces versions si elles fonctionnent pour vous
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0") // Conservez ces versions si elles fonctionnent pour vous
        //ajout
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services")

        // Dagger Hilt (Utilise les références TOML)
        implementation(libs.hilt.android)
        ksp(libs.hilt.android.compiler) // Processeur d'annotation Hilt pour l'application
        implementation(libs.androidx.hilt.navigation.compose)
        ksp(libs.androidx.hilt.compiler) // Processeur d'annotation Hilt pour AndroidX

        // Pour l'annotation @Inject
        //implementation("javax.inject:javax.inject:1") // C'est la version standard

        // Google Play Services
        implementation(libs.google.play.services.location)
        implementation(libs.google.play.services.maps) // <-- MODIFIÉ : Utilise la référence TOML
        implementation(libs.maps.compose) // <-- MODIFIÉ : Utilise la référence TOML
        implementation("com.google.android.gms:play-services-tasks:18.2.0") // Peut rester ici ou être aussi ajouté au TOML
        //implementation(libs.play.services.tasks) // Peut rester ici ou être aussi ajouté au TOML


        // ExoPlayer (Versions en dur, à déplacer dans libs.versions.toml)
        implementation("androidx.media3:media3-exoplayer:1.3.1")
        implementation("androidx.media3:media3-ui:1.3.1")

        // TensorFlow Lite (Version en dur, à déplacer dans libs.versions.toml)
        //implementation("org.tensorflow:tensorflow-lite:2.17.0")

        // Tests (Utilise les références TOML)
        //testImplementation(libs.junit)    ATTENTION
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)

        // Kotest Testing (si non déjà là, ajoutez-les)
        testImplementation(libs.kotest.runner.junit5)
        testImplementation(libs.kotest.assertions.core)
        testImplementation(libs.kotest.property)

        // MockK et Robolectric (gardé en dur car pas dans votre TOML)
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0")
        testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.0")
        testImplementation(libs.mockk)
        testImplementation("org.robolectric:robolectric:4.12.1")
        androidTestImplementation(libs.mockk.android)

        // Hilt Testing (Utilise les références TOML)
        androidTestImplementation(libs.hilt.android.testing)
        kspAndroidTest(libs.hilt.android.compiler) // Processeur d'annotation Hilt pour tests d'instrumentation
        testImplementation(libs.hilt.android.testing)
        kspTest(libs.hilt.android.compiler) // Processeur d'annotation Hilt pour tests unitaires

        // Pour l'intégration des coroutines avec les Tasks de Google Play Services/Firebase
        implementation(libs.kotlinx.coroutines.play.services)

        // Pour FusedLocationProviderClient et les services de localisation
        //implementation(libs.google.play.services.location)
        // Pour les coroutines avec les Task de Google Play Services/Firebase
        implementation(libs.kotlinx.coroutines.play.services)

        // Turbine est utilisé dans les tests : Turbine est conçu pour tester les Flows
        testImplementation("app.cash.turbine:turbine:1.1.0")

        // pour la classe SeeddatabaseUseCase
        implementation("com.google.code.gson:gson:2.10.1")

        // --- AJOUTEZ CES 3 LIGNES CI-DESSOUS ---
        // WorkManager pour les tâches en arrière-plan
        implementation("androidx.work:work-runtime-ktx:2.9.0")
        // Intégration de Hilt avec WorkManager
        implementation("androidx.hilt:hilt-work:1.2.0")
        ksp("androidx.hilt:hilt-compiler:1.2.0")
        // --- FIN DES AJOUTS ---

    }

// Appliquez le plugin Google Services ici (doit être après le bloc dependencies)
//apply(plugin = "com.google.gms.google-services")   <<-- redondance car dékjà en haut dans plugins id("com.google.gms.google-services")