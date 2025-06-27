// Dans app/src/main/java/com/easytoday/guidegroup/data/repository/impl/FakeMessageRepositoryImpl.kt

package com.easytoday.guidegroup.data.repository.impl

import android.net.Uri
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.repository.MessageRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.util.UUID
import java.util.Date // Assurez-vous que cet import est là

// Implémentation factice de MessageRepository pour l'environnement de test (mock).
// Simule l'envoi, la réception et le téléchargement de médias pour les messages.
class FakeMessageRepositoryImpl @Inject constructor() : MessageRepository {

    // Simule une base de données de messages en mémoire, organisée par groupId
    private val fakeMessagesDb = MutableStateFlow<MutableMap<String, MutableList<Message>>>(mutableMapOf())

    // Initialise avec quelques messages factices pour les tests
    init {
        // Ajoutons quelques messages pour un groupe factice
        val groupId1 = "group1_id"
        fakeMessagesDb.value[groupId1] = mutableListOf(
            Message(
                id = "msg1_id",
                groupId = groupId1, // <-- AJOUTÉ: Il est bon de spécifier le groupId ici aussi
                senderId = "user1_id",
                senderName = "Utilisateur 1",
                text = "Salut tout le monde !",
                mediaUrl = null, // Texte, donc pas de média URL
                mediaType = Message.MediaType.TEXT, // <-- MODIFIÉ: utilisez mediaType
                timestamp = Date(System.currentTimeMillis() - 20000)
            ),
            Message(
                id = "msg2_id",
                groupId = groupId1, // <-- AJOUTÉ
                senderId = "user2_id",
                senderName = "Utilisateur 2",
                text = "Bien le bonjour !",
                mediaUrl = null,
                mediaType = Message.MediaType.TEXT, // <-- MODIFIÉ
                timestamp = Date(System.currentTimeMillis() - 10000)
            ),
            Message(
                id = "msg3_id",
                groupId = groupId1, // <-- AJOUTÉ
                senderId = "user1_id",
                senderName = "Utilisateur 1",
                text = null, // Média, donc pas de texte
                mediaUrl = "https://mock.com/image_test.jpg", // URL factice
                mediaType = Message.MediaType.IMAGE, // <-- MODIFIÉ
                timestamp = Date(System.currentTimeMillis() - 5000)
            )
        )
    }

    /**
     * Envoie un nouveau message à une conversation/un groupe spécifique.
     * Simule l'ajout du message à la base de données factice.
     */
    override suspend fun sendMessage(groupId: String, message: Message): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        delay(300) // Simule un délai réseau

        try {
            if (!fakeMessagesDb.value.containsKey(groupId)) {
                fakeMessagesDb.value[groupId] = mutableListOf()
            }

            val newMessageId = message.id.ifEmpty { "fakeMsgId_${UUID.randomUUID()}" }
            val messageWithId = message.copy(
                id = newMessageId,
                // Si message.timestamp est déjà une Date non nulle, utilisez-la.
                // Sinon (si c'est null ou non défini), créez une nouvelle Date.
                timestamp = message.timestamp ?: Date()
            )

            fakeMessagesDb.value[groupId]?.add(messageWithId)
            fakeMessagesDb.value = fakeMessagesDb.value.toMutableMap()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error("Échec de l'envoi du message factice: ${e.localizedMessage}", e))
        }
    }

    /**
     * Récupère un flux de messages pour un groupe donné.
     * Simule la récupération de messages de la base de données factice.
     */
    override fun getMessagesForGroup(groupId: String): Flow<List<Message>> {
        return fakeMessagesDb.map { allMessages ->
            delay(200)
            allMessages[groupId]?.sortedBy { it.timestamp?.time ?: 0L } ?: emptyList()
        }
    }

    /**
     * Télécharge un fichier média sur le stockage cloud (simulé).
     * Retourne une URL factice.
     */
    override suspend fun uploadMedia(uri: Uri, type: Message.MediaType, groupId: String): Flow<Result<String>> = flow {
        emit(Result.Loading)
        delay(1000)

        try {
            val fakeUrl = "https://mock.com/${groupId}/${UUID.randomUUID()}.${type.name.lowercase()}"
            emit(Result.Success(fakeUrl))
        } catch (e: Exception) {
            emit(Result.Error("Échec du téléchargement média factice: ${e.localizedMessage}", e))
        }
    }
}

