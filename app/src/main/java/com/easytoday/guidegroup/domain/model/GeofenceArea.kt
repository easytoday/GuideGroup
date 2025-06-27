// app/src/main/java/com/easytoday/guidegroup/domain/model/GeofenceArea.kt
package com.easytoday.guidegroup.domain.model

//import com.google.firebase.firestore.DocumentId // <-- AJOUTEZ CETTE LIGNE
//import com.google.firebase.firestore.ServerTimestamp

/**
 * Entité de données représentant une zone de géorepérage.
 *
 * @param id L'identifiant unique de la zone de géorepérage.
 * @param groupId L'ID du groupe auquel cette zone est associée.
 * @param name Le nom de la zone de géorepérage (ex: "Zone de sécurité du parc").
 * @param latitude La latitude du centre de la zone.
 * @param longitude La longitude du centre de la zone.
 * @param radius Le rayon de la zone en mètres.
 * @param transitionTypes Les types de transition à surveiller (ex: entrée, sortie, séjour).
 * @param expirationDurationMillis La durée avant l'expiration de la géorepérage en millisecondes.
 * @param setByUserId L'ID de l'utilisateur (guide) qui a défini cette zone.
 * @param timestamp Le horodatage de la création de la zone.
 */
data class GeofenceArea(
    val id: String = "",
    val groupId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val radius: Float = 0f, // Rayon en mètres
    val transitionTypes: Int = 0, // Ex: Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
    val expirationDurationMillis: Long = 0L, // Durée d'expiration en ms
    val setByUserId: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    // Un constructeur sans argument est nécessaire pour la désérialisation de Firebase Firestore.
    constructor() : this("", "", "", 0.0, 0.0, 0f, 0, 0L, "", 0L)
}


