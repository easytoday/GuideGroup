// app/src/main/java/com/easytoday/guidegroup/presentation/ui/SetGeofenceDialog.kt
package com.easytoday.guidegroup.presentation.screens.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import java.util.UUID

/**
 * Dialogue pour définir une nouvelle zone de géorepérage.
 * Permet au guide de spécifier le nom, le rayon et les types de transition.
 *
 * @param initialLocation La position initiale (centre) de la géorepérage.
 * @param onDismiss Action à effectuer lorsque le dialogue est fermé.
 * @param onConfirm Action à effectuer lorsque la zone de géorepérage est confirmée.
 * Prend en paramètres: id, nom, latitude, longitude, rayon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetGeofenceDialog(
    initialLocation: LatLng,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, Float) -> Unit // id, name, lat, lon, radius
) {
    var geofenceName by remember { mutableStateOf("") }
    var radiusText by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var radiusError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Définir une Zone de Géorepérage") },
        text = {
            Column {
                OutlinedTextField(
                    value = geofenceName,
                    onValueChange = {
                        geofenceName = it
                        nameError = if (it.isBlank()) "Le nom ne peut pas être vide" else null
                    },
                    label = { Text("Nom de la zone") },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = radiusText,
                    onValueChange = {
                        radiusText = it
                        radiusError = try {
                            val radius = it.toFloat()
                            if (radius <= 0) "Le rayon doit être positif"
                            else null
                        } catch (e: NumberFormatException) {
                            "Rayon invalide (nombre attendu)"
                        }
                    },
                    label = { Text("Rayon (mètres)") },
                    isError = radiusError != null,
                    supportingText = { radiusError?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Latitude: ${"%.4f".format(initialLocation.latitude)}")
                Text("Longitude: ${"%.4f".format(initialLocation.longitude)}")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val radius = radiusText.toFloatOrNull()
                    if (geofenceName.isNotBlank() && radius != null && radius > 0) {
                        onConfirm(
                            UUID.randomUUID().toString(), // Génère un ID unique pour la géorepérage
                            geofenceName,
                            initialLocation.latitude,
                            initialLocation.longitude,
                            radius
                        )
                    } else {
                        if (geofenceName.isBlank()) nameError = "Le nom ne peut pas être vide"
                        if (radius == null || radius <= 0) radiusError = "Rayon invalide ou nul"
                    }
                }
            ) {
                Text("Définir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}


