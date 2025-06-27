// app/src/main/java/com/easytoday/guidegroup/domain/model/Message.kt
package com.easytoday.guidegroup.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Entité de données représentant un message dans le chat de groupe.
 *
 * @param id L'identifiant unique du message.
 * @param groupId L'ID du groupe auquel le message appartient.
 * @param senderId L'ID de l'utilisateur qui a envoyé le message.
 * @param senderName Le nom de l'expéditeur.
 * @param text Le contenu textuel du message (peut être null si c'est un média).
 * @param mediaUrl L'URL du média associé au message (peut être null si c'est du texte).
 * @param mediaType Le type de média (image, audio, vidéo).
 * @param timestamp Le horodatage de l'envoi du message.
 */
data class Message(
    @DocumentId val id: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String? = null,
    val mediaUrl: String? = null,
    val mediaType: MediaType? = null,
    @ServerTimestamp val timestamp: Date? = null
) {
    enum class MediaType {
        TEXT, IMAGE, AUDIO, VIDEO
    }

    // Constructeur sans argument pour Firebase Firestore si nécessaire.
    // Les data classes Kotlin avec valeurs par défaut le gèrent souvent automatiquement.
    constructor() : this("", "", "", "", null, null, null, null)
}

