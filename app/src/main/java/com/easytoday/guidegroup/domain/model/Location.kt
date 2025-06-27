// app/src/main/java/com/easytoday/guidegroup/domain/model/Location.kt
package com.easytoday.guidegroup.domain.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Location(
    // @DocumentId val id: String = "", // Vous pouvez ajouter ceci si chaque mise à jour de localisation est un nouveau document
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @ServerTimestamp val timestamp: Date? = null // <-- MODIFIÉ ici
)

