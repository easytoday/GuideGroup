package com.easytoday.guidegroup.domain.repository

import android.net.Uri
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.model.Result
import kotlinx.coroutines.flow.Flow

/**
 * Interface pour la gestion des messages et du média dans l'application.
 * Définit les opérations pour envoyer, récupérer des messages et télécharger du média.
 */
interface MessageRepository {
    /**
     * Envoie un nouveau message à un groupe spécifique.
     * @param groupId L'ID du groupe/de la conversation auquel le message appartient.
     * @param message Le message à envoyer.
     * @return Un Flow de [Result] indiquant le succès ou l'échec de l'envoi.
     */
    suspend fun sendMessage(groupId: String, message: Message): Flow<Result<Unit>>

    /**
     * Récupère un flux de messages pour un groupe donné.
     * Les messages sont triés par timestamp.
     * @param groupId L'ID du groupe.
     * @return Un Flow d'une liste de messages.
     */
    fun getMessagesForGroup(groupId: String): Flow<List<Message>>


    /**
     * Télécharge un fichier média (image, audio, vidéo) sur le stockage cloud.
     *
     * @param uri L'URI locale du fichier média.
     * @param type Le type de média (IMAGE, AUDIO, VIDEO).
     * @param groupId L'ID du groupe associé (peut être utilisé pour structurer le stockage si nécessaire).
     * @return Un Flow de [Result] indiquant le succès (avec l'URL de téléchargement) ou l'échec.
     */
    suspend fun uploadMedia(uri: Uri, type: Message.MediaType, groupId: String): Flow<Result<String>>
}


