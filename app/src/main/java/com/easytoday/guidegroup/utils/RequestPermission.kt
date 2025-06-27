// app/src/main/java/com/easytoday/guidegroup/utils/PermissionUtils.kt
package com.easytoday.guidegroup.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity // Import nécessaire
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat // Import nécessaire pour shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat

/**
 * Gère la demande d'une permission spécifique et réagit à son résultat.
 *
 * @param permission La permission à demander (ex: Manifest.permission.ACCESS_FINE_LOCATION).
 * @param onPermissionGranted Lambda appelée si la permission est déjà accordée ou accordée par l'utilisateur.
 * @param onPermissionDenied Lambda appelée si la permission est refusée par l'utilisateur.
 * @param onRationaleNeeded (Optionnel) Lambda appelée si le système recommande d'expliquer pourquoi la permission est nécessaire.
 * Si fournie, la demande de permission sera relancée après cet appel.
 */
@Composable
fun RequestPermission(
    permission: String,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onRationaleNeeded: (() -> Unit)? = null // Optionnel
) {
    val context = LocalContext.current

    // Lanceur d'activité pour demander une permission unique
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

    LaunchedEffect(key1 = permission) {
        val permissionCheckResult = ContextCompat.checkSelfPermission(context, permission)
        when {
            permissionCheckResult == PackageManager.PERMISSION_GRANTED -> {
                // La permission est déjà accordée
                onPermissionGranted()
            }
            // <<-- CORRECTION DE LA LOGIQUE ICI -->>
            // Vérifie si l'explication est nécessaire ET si onRationaleNeeded est fourni
            onRationaleNeeded != null && (context as? ComponentActivity)?.let { activity ->
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            } == true -> {
                // La justification est nécessaire. Appelle la lambda pour l'expliquer.
                onRationaleNeeded()
                // Relance la demande de permission APRÈS que la justification ait été affichée
                requestPermissionLauncher.launch(permission)
            }
            // <<-- FIN DE CORRECTION -->>
            else -> {
                // Demande la permission
                requestPermissionLauncher.launch(permission)
            }
        }
    }
}

// Fonction utilitaire pour vérifier si la permission est déjà accordée
fun isPermissionGranted(context: Context, permission: String): Boolean {
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

// La fonction shouldShowRationale commentée n'est plus nécessaire ici car sa logique est intégrée.
// Vous pouvez la supprimer complètement du fichier.

