// app/src/main/java/com/easytoday/guidegroup/domain/usecase/SendMessageUseCase.kt
package com.easytoday.guidegroup.domain.usecase


import com.easytoday.guidegroup.domain.model.Result
import kotlinx.coroutines.flow.Flow

import android.net.Uri
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.repository.MessageRepository
import com.easytoday.guidegroup.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject




/**
 * Cas d'utilisation pour gérer l'envoi de messages.
 * Interagit avec le MessageRepository pour effectuer l'opération d'envoi.
 */
class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository
) {
    /**
     * Exécute le cas d'utilisation pour envoyer un message.
     * @param groupId L'ID du groupe auquel le message est destiné.
     * @param message Le message à envoyer.
     * @return Un Flow de [Result] indiquant le succès ou l'échec de l'envoi.
     */
    suspend operator fun invoke(groupId: String, message: Message): Flow<Result<Unit>> {
        return messageRepository.sendMessage(groupId, message)
    }
}




/**
 * Cas d'utilisation pour l'envoi d'un message (texte ou média) dans un groupe.
 *
 * @param messageRepository Le référentiel de messages.
 * @param authRepository Le référentiel d'authentification pour obtenir l'utilisateur courant.
 * @param userRepository Le référentiel d'utilisateurs pour obtenir le nom de l'expéditeur.
 */

//class SendMessageUseCase @Inject constructor(
//    private val messageRepository: MessageRepository,
//    private val authRepository: AuthRepository,
//    private val userRepository: UserRepository
//) {
//    /**
//     * Exécute l'envoi d'un message.
//     *
//     * @param groupId L'ID du groupe cible.
//     * @param text Le contenu textuel du message (peut être null).
//     * @param mediaUri L'URI locale du média à envoyer (peut être null).
//     * @param mediaType Le type de média (peut être null).
//     * @return Un objet Result indiquant le succès ou l'échec.
//     */
//    suspend operator fun invoke(
//        groupId: String,
//        text: String?,
//        mediaUri: Uri?,
//        mediaType: Message.MediaType?
//    ): Result<Unit> {
//        val currentUserId = authRepository.getCurrentUserId()
//        if (currentUserId == null) {
//            return Result.failure(Exception("Utilisateur non authentifié."))
//        }
//
//        val senderUserResult = userRepository.getUser(currentUserId).collect { user ->
//            if (user == null) {
//                return Result.failure(Exception("Données utilisateur introuvables."))
//            }
//
//            val senderName = user.name
//            var mediaUrl: String? = null
//
//            // Si un média est présent, le télécharger d'abord
//            if (mediaUri != null && mediaType != null) {
//                val uploadResult = messageRepository.uploadMedia(mediaUri, mediaType)
//                uploadResult.onSuccess { url ->
//                    mediaUrl = url
//                }.onFailure { e ->
//                    return Result.failure(Exception("Échec du téléchargement du média: ${e.message}"))
//                }
//            }
//
//            // Créer l'objet Message
//            val message = Message(
//                id = UUID.randomUUID().toString(), // Génère un ID unique pour le message
//                groupId = groupId,
//                senderId = currentUserId,
//                senderName = senderName,
//                text = text,
//                mediaUrl = mediaUrl,
//                mediaType = mediaType,
//                timestamp = System.currentTimeMillis()
//            )
//
//            // Envoyer le message
//            return messageRepository.sendMessage(message)
//        }
//        return Result.failure(Exception("Erreur inattendue lors de l'envoi du message."))
//    }
//}


