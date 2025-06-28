package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import kotlinx.coroutines.flow.flow
import timber.log.Timber // Ou android.util.Log si vous n'utilisez pas Timber

import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage // Import de FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import android.net.Uri
import com.easytoday.guidegroup.domain.model.Result
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlinx.coroutines.flow.catch // Importez le catch pour les Flow

/**
 * Implémentation concrète de [MessageRepository] utilisant Firestore.
 * Gère l'envoi et la récupération de messages pour des conversations/groupes.
 */
class MessageRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    //private val firestore: FirebaseFirestore, // Injecter Firestore si nécessaire pour des requêtes plus complexes
    private val firebaseStorage: FirebaseStorage // Injecter FirebaseStorage
) : MessageRepository {

    // Chemin de la collection pour les messages.
    // Il est courant d'avoir des sous-collections de messages sous les groupes,
    // par exemple: "groups/{groupId}/messages"
    private val GROUPS_COLLECTION = "groups"
    private val MESSAGES_SUBCOLLECTION = "messages"
    private val MEDIA_STORAGE_PATH = "chat_media" // Chemin dans Firebase Storage

    /**
     * Envoie un nouveau message à une conversation/un groupe spécifique.
     * @param groupId L'ID du groupe/de la conversation auquel le message appartient.
     * @param message Le message à envoyer.
     * @return Un Flow de [Result] indiquant le succès ou l'échec de l'envoi.
     */
    override suspend fun sendMessage(groupId: String, message: Message): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            val messageRef = firestoreHelper.db.collection(GROUPS_COLLECTION)
                .document(groupId)
                .collection(MESSAGES_SUBCOLLECTION)
                .document() // Firestore générera un nouvel ID de document

            val messageWithId = message.copy(id = messageRef.id) // Copier le message avec l'ID généré

            messageRef.set(messageWithId).await() // Utiliser set pour ajouter le document
            Timber.d("Message envoyé avec succès au groupe $groupId: ${message.text ?: message.mediaUrl}")
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            Timber.e("Erreur lors de l'envoi du message au groupe $groupId: ${e.message}", e)
            emit(Result.Error("Échec de l'envoi du message.", e))
        }
    }

    /**
     * Récupère un flux de messages pour un groupe donné.
     * Les messages sont triés par timestamp.
     * @param groupId L'ID du groupe.
     * @return Un Flow d'une liste de messages.
     */
    override fun getMessagesForGroup(groupId: String): Flow<List<Message>> {
        // Correction ici : Spécifiez explicitement le type <Message> pour getCollectionAsFlow
        return firestoreHelper.getCollectionAsFlow<Message>(
            firestoreHelper.db.collection(GROUPS_COLLECTION)
                .document(groupId)
                .collection(MESSAGES_SUBCOLLECTION)
                .orderBy("timestamp") // Trie les messages par horodatage
        ).catch { e ->
            Timber.e("Erreur lors de la récupération des messages pour le groupe $groupId: ${e.message}", e)
            emit(emptyList())
        }
    }

    /**
     * Télécharge un fichier média (image, audio, vidéo) sur le stockage cloud.
     * Gère le téléchargement des fichiers multimédias vers Firebase Storage et
     * l'envoi des messages avec les URLs de ces médias.
     *
     * @param uri L'URI locale du fichier média.
     * @param type Le type de média (IMAGE, AUDIO, VIDEO).
     * @param groupId L'ID du groupe auquel le média est lié. <--- AJOUTÉ
     * @return Un Flow de [Result] indiquant le succès (avec l'URL de téléchargement) ou l'échec. <--- CHANGÉ
     */
    // MODIFICATION ICI : Signature de la fonction pour correspondre à l'interface
    override suspend fun uploadMedia(uri: Uri, type: Message.MediaType, groupId: String): Flow<Result<String>> = flow {
        emit(Result.Loading) // Commencez par émettre un état de chargement
        try {
            val fileExtension = when (type) {
                Message.MediaType.TEXT -> "txt"
                Message.MediaType.IMAGE -> "jpg"
                Message.MediaType.AUDIO -> "mp3"
                Message.MediaType.VIDEO -> "mp4"
                Message.MediaType.POI -> "txt"
            }
            val fileName = "${UUID.randomUUID()}.$fileExtension"
            val storageRef = firebaseStorage.reference
                // Il est bon d'organiser les médias par groupe dans le stockage
                .child(GROUPS_COLLECTION)
                .child(groupId) // Utilisez le groupId ici
                .child(MEDIA_STORAGE_PATH)
                .child(fileName)

            val uploadTask = storageRef.putFile(uri).await()
            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

            Timber.d("Média téléchargé avec succès: $downloadUrl")
            emit(Result.Success(downloadUrl)) // Émettez le succès
        } catch (e: StorageException) {
            val errorMessage = "Erreur de stockage Firebase: ${e.message}"
            Timber.e(e, errorMessage)
            emit(Result.Error(errorMessage, e)) // Émettez l'erreur
        } catch (e: Exception) {
            val errorMessage = "Erreur inattendue lors du téléchargement du média: ${e.message}"
            Timber.e(e, errorMessage)
            emit(Result.Error(errorMessage, e)) // Émettez l'erreur
        }
    }
}


