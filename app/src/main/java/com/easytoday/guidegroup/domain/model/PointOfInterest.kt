// app/src/main/java/com/easytoday/guidegroup/domain/model/PointOfInterest.kt
package com.easytoday.guidegroup.domain.model

import java.util.Date
import com.google.firebase.firestore.DocumentId 
import com.google.firebase.firestore.ServerTimestamp // ajouté createdAt: Date? et l'annotation


data class PointOfInterest(
    @DocumentId val id: String = "", // ici
    val groupId: String = "",
    // NOUVEAU : Ajout du champ pour l'ID du créateur
    val creatorId: String = "",
    val name: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val type: String = "", // e.g., "restaurant", "landmark", "emergency"
    @ServerTimestamp val createdAt: Date? = null // (optionnel )
)

