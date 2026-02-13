// app/src/main/java/com/easytoday/guidegroup/domain/model/User.kt
package com.easytoday.guidegroup.domain.model

import com.google.firebase.firestore.DocumentId 
import com.google.firebase.firestore.PropertyName

/**
 * Entité de données représentant un utilisateur dans l'application Group Guide.
 *
 * @param id L'identifiant unique de l'utilisateur (UID Firebase).
 * @param email L'adresse e-mail de l'utilisateur.
 * @param name Le nom d'affichage de l'utilisateur.
 * @param isGuide Un booléen indiquant si l'utilisateur est un guide.
 */
// app/src/main/java/com/easytoday/guidegroup/domain/model/User.kt


data class User(
    @DocumentId val id: String = "",
    val email: String = "",
    val username: String = "",
    val profileImageUrl: String = "",
    val lastKnownLat: Double = 0.0,
    val lastKnownLon: Double = 0.0,
    // CORRECTION : On indique à Firestore que ce champ Kotlin
    // correspond au champ "guide" dans la base de données.
    @get:PropertyName("guide") @set:PropertyName("guide")
    var isGuide: Boolean = false
)




