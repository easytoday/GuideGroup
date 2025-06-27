package com.easytoday.guidegroup

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

import android.provider.Settings // Importez Settings pour rediriger vers les paramètres de l'app si besoin


import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts // Importez ceci

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.Surface


import androidx.core.content.ContextCompat // Importez ceci


import androidx.navigation.compose.rememberNavController

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import com.easytoday.guidegroup.presentation.navigation.AppNavigation
import com.easytoday.guidegroup.ui.theme.GuideGroupTheme
import dagger.hilt.android.AndroidEntryPoint

// Définissez vos routes de navigation ici, idéalement dans un objet séparé (ex: `AppRoutes.kt`)
// Pour l'instant, je les mets ici pour la clarté.
//object AppRoutes {
//    const val LOGIN = "login_route"
//    const val SIGN_UP = "signup_route"
//    const val HOME = "home_route"
//    const val CHAT_DETAIL = "chat_detail_route/{groupId}" // Route avec argument
//    const val GROUP_DETAIL = "group_detail_route/{groupId}" // Route avec argument
//    const val MAP_SCREEN = "map_screen_route/{groupId}" // Route avec argument
//}


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // 1. Créez un lanceur d'activité pour demander les permissions
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions() // Pour demander plusieurs permissions à la fois
    ) { permissions ->
        // Ce bloc est exécuté une fois que l'utilisateur a répondu à la demande de permission
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val backgroundLocationGranted = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: false

        if (fineLocationGranted && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || backgroundLocationGranted)) {
            // Toutes les permissions nécessaires sont accordées (ou non requises pour la version Android)
            // C'est ici que vous devriez déclencher l'initialisation de votre géorepérage
            // ou informer votre ViewModel/Repository que les permissions sont prêtes.
            // Par exemple:
            // geofenceViewModel.onLocationPermissionsGranted()
            // Ou si vous avez un service qui initie le geofencing:
            // startGeofencingService() // Appelez votre fonction pour démarrer le service/geofencing
            println("Toutes les permissions de localisation sont accordées. Vous pouvez initier le géorepérage.")

        } else if (Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT && fineLocationGranted && !backgroundLocationGranted) {
            // Sur Android 10 (Q) et plus : ACCESS_FINE_LOCATION est accordée, mais ACCESS_BACKGROUND_LOCATION non.
            // C'est un cas particulier où l'utilisateur doit souvent aller dans les paramètres de l'application
            // pour accorder la permission "Autoriser tout le temps" (Allow all the time).
            println("Permission de localisation en arrière-plan non accordée. Veuillez l'activer dans les paramètres de l'application.")
            // Vous pouvez optionnellement diriger l'utilisateur vers les paramètres de votre application:
            // openAppSettings()
        } else {
            // La permission de localisation précise a été refusée.
            // Informez l'utilisateur que la fonctionnalité de géorepérage ne sera pas disponible.
            println("Permission de localisation refusée. Le géorepérage ne fonctionnera pas.")
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 2. Appelez cette fonction pour vérifier et demander les permissions au démarrage de l'activité
        checkAndRequestLocationPermissions()

        setContent {
            GuideGroupTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //AppNavigation()
                    val navController = rememberNavController()
                    AppNavigation(navController = navController) // Votre fonction de navigation
                }
            }
        }
    }


    // 3. Ajoutez cette fonction dans votre classe MainActivity
    private fun checkAndRequestLocationPermissions() {
        // Vérifie si la permission de localisation précise est déjà accordée
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Vérifie si la permission de localisation en arrière-plan est déjà accordée (pour Android Q+)
        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // La permission ACCESS_BACKGROUND_LOCATION n'est pas requise avant Android Q
        }

        val permissionsToRequest = mutableListOf<String>()

        if (!fineLocationGranted) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // Demandez ACCESS_BACKGROUND_LOCATION seulement si nécessaire et pas encore accordée
        // et seulement si ACCESS_FINE_LOCATION est déjà accordée ou sur le point d'être demandée
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !backgroundLocationGranted) {
            permissionsToRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            // Si des permissions manquent, lancez la demande
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        } else {
            // Toutes les permissions nécessaires sont déjà accordées.
            // Vous pouvez directement initier le géorepérage ou les actions qui en dépendent.
            println("Toutes les permissions de localisation sont déjà accordées. Le géorepérage peut être initialisé.")
            // Par exemple:
            // geofenceViewModel.onLocationPermissionsGranted()
            // Ou si vous avez un service qui initie le geofencing:
            // startGeofencingService() // Appelez votre fonction pour démarrer le service/geofencing
        }
    }

    // Optionnel: fonction pour ouvrir les paramètres de l'application si l'utilisateur doit accorder manuellement
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        startActivity(intent)
    }


}


