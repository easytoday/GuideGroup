// Fichier : app/src/main/java/com/easytoday/guidegroup/presentation/viewmodel/ChatViewModel.kt

package com.easytoday.guidegroup.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.repository.MessageRepository
import com.easytoday.guidegroup.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.collectLatest // Assurez-vous que cet import est là
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject // Assurez-vous que cet import est là

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val messageRepository: MessageRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle // C'est lui qui est injecté !
) : ViewModel() {

    // L'ID du groupe pour lequel ce chat est ouvert (déjà existant, parfait !)
    private val _groupId = MutableStateFlow<String?>(null)
    val groupId: StateFlow<String?> = _groupId.asStateFlow()

    // Liste des messages pour le groupe actuel
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // État de l'envoi de message (texte ou média)
    private val _sendMessageState = MutableStateFlow<Result<Unit>>(Result.Loading)
    val sendMessageState: StateFlow<Result<Unit>> = _sendMessageState.asStateFlow()

    // L'utilisateur actuel, nécessaire pour l'ID de l'expéditeur et le nom d'affichage
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // État du téléchargement de média
    private val _uploadMediaState = MutableStateFlow<Result<String>>(Result.Loading)
    val uploadMediaState: StateFlow<Result<String>> = _uploadMediaState.asStateFlow()


    init {
        // --- MISE À JOUR : Récupération du groupId depuis SavedStateHandle ---
        val groupIdFromArgs = savedStateHandle.get<String>("groupId")
        _groupId.value = groupIdFromArgs // Met à jour le StateFlow avec l'ID obtenu

        // --- Votre appel existant à observeCurrentUser() ---
        observeCurrentUser()

        // --- Votre logique existante d'observation des messages, qui dépendra de _groupId ---
        _groupId.flatMapLatest { id ->
            if (id != null) {
                Timber.d("Observing messages for group ID: $id")
                // Assurez-vous que messageRepository.getMessagesForGroup existe
                messageRepository.getMessagesForGroup(id)
            } else {
                Timber.d("No group ID available to observe messages.")
                MutableStateFlow(emptyList()) // Retourne un flux vide si pas de groupId
            }
        }.onEach { messagesList ->
            _messages.value = messagesList
            Timber.d("Messages updated: ${messagesList.size} messages.")
        }.launchIn(viewModelScope)
    }

//    private fun observeCurrentUser() {
//        authRepository.getCurrentUser().onEach { user ->
//            // Assurez-vous que _currentUser est de type Result<User?> ou User?
//            // Si c'est Result<User?>, adaptez :
//            // if (user is Result.Success) _currentUser.value = user.data
//            // else if (user is Result.Error) _currentUser.value = null (ou gérez l'erreur)
//            // Sinon, si c'est directement User?, votre code est bon :
//            _currentUser.value = user
//            Timber.d("ChatViewModel: Current user observed: ${user?.email}")
//        }.launchIn(viewModelScope)
//    }

    private fun observeCurrentUser() {
        authRepository.getCurrentUser().onEach { resultUser -> // Renommer pour éviter la confusion
            // Ici, resultUser est de type Result<User?>
            when (resultUser) {
                is Result.Success -> {
                    _currentUser.value = resultUser.data // Extraire le User? de Result.Success
                    Timber.d("ChatViewModel: Current user observed: ${resultUser.data?.email}")
                }
                is Result.Error -> {
                    _currentUser.value = null // En cas d'erreur, l'utilisateur est null
                    Timber.e("ChatViewModel: Error observing current user: ${resultUser.message}", resultUser.exception)
                }
                Result.Loading -> {
                    // Optionnel: Gérer l'état de chargement si nécessaire, par exemple afficher un indicateur
                    Timber.d("ChatViewModel: Current user loading...")
                }
                is Result.Initial -> {
                    // Optionnel: Gérer l'état initial si nécessaire
                    Timber.d("ChatViewModel: Current user initial state.")
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Envoie un message texte au groupe actuel.
     * @param text Le contenu textuel du message.
     */
    fun sendMessage(text: String) {
        val currentGroupId = _groupId.value // Utilise l'ID du groupe du ViewModel
        val sender = _currentUser.value

        if (currentGroupId == null) {
            _sendMessageState.value = Result.Error("ID du groupe non disponible pour l'envoi de message.")
            Timber.e("sendMessage: groupId is null.")
            return
        }
        if (sender == null) {
            _sendMessageState.value = Result.Error("Utilisateur non connecté pour l'envoi de message.")
            Timber.e("sendMessage: sender is null.")
            return
        }
        if (text.isBlank()) {
            _sendMessageState.value = Result.Error("Le message ne peut pas être vide.")
            Timber.w("sendMessage: message text is blank.")
            return
        }

        val message = Message(
            senderId = sender.id,
            senderName = sender.username.ifEmpty { (sender.email ?: "").substringBefore("@") ?: "Inconnu" }, // Ligne 90 modif temporaire
            //senderName = sender.username.ifEmpty { sender.email?.substringBefore("@") ?: "Inconnu" },
            // timestamp sera rempli par Firestore (@ServerTimestamp)
        )

        viewModelScope.launch {
            sendMessageUseCase(currentGroupId, message).onEach { result ->
                _sendMessageState.value = result
                if (result is Result.Error) {
                    Timber.e("Error sending text message: ${result.message}", result.exception)
                } else if (result is Result.Success) {
                    Timber.d("Text message sent successfully.")
                    resetSendMessageState() // Réinitialiser l'état après succès
                }
            }.launchIn(viewModelScope)
        }
    }

    /**
     * Télécharge un média et envoie un message avec l'URL du média.
     * @param uri L'URI locale du fichier média.
     * @param mediaType Le type de média (IMAGE, AUDIO, VIDEO).
     */
    fun sendMediaMessage(uri: Uri, mediaType: Message.MediaType) {
        val currentGroupId = _groupId.value // Utilise l'ID du groupe du ViewModel
        val sender = _currentUser.value

        if (currentGroupId == null) {
            _sendMessageState.value = Result.Error("ID du groupe non disponible pour l'envoi de média.")
            Timber.e("sendMediaMessage: groupId is null.")
            return
        }
        if (sender == null) {
            _sendMessageState.value = Result.Error("Utilisateur non connecté pour l'envoi de média.")
            Timber.e("sendMediaMessage: sender is null.")
            return
        }

        viewModelScope.launch {
            // Collecte le Flow de uploadMedia
            messageRepository.uploadMedia(uri, mediaType, currentGroupId).collectLatest { uploadResult ->
                _uploadMediaState.value = uploadResult // Met à jour l'état de téléchargement

                when (uploadResult) {
                    is Result.Success -> {
                        val message = Message(
                            senderId = sender.id,
                            senderName = sender.username.ifEmpty { (sender.email ?: "").substringBefore("@") ?: "Inconnu" }, // Ligne 90 modif temporaire
                            //senderName = sender.username.ifEmpty { sender.email?.substringBefore("@") ?: "Inconnu" },
                            mediaUrl = uploadResult.data,
                            mediaType = mediaType
                        )
                        // Une fois le média téléchargé, envoyer le message
                        sendMessageUseCase(currentGroupId, message).onEach { sendResult ->
                            _sendMessageState.value = sendResult
                            if (sendResult is Result.Error) {
                                Timber.e("Error sending media message: ${sendResult.message}", sendResult.exception)
                            } else if (sendResult is Result.Success) {
                                Timber.d("Media message sent successfully.")
                                resetSendMessageState()
                            }
                        }.launchIn(viewModelScope)
                    }
                    is Result.Error -> {
                        _sendMessageState.value = Result.Error("Échec du téléchargement du média: ${uploadResult.message}", uploadResult.exception)
                    }
                    Result.Loading -> { /* L'état de chargement est déjà émis par _uploadMediaState.value = uploadResult */ }
                    else -> {} // Gérer Result.Initial si votre Flow l'émet
                }
            }
        }
    }

    /**
     * Réinitialise l'état d'envoi de message.
     */
    fun resetSendMessageState() {
        //_sendMessageState.value = Result.Initial() // Utilisez Result.Initial() ou Result.Success(Unit) si c'est ce que vous voulez
        _sendMessageState.value = Result.Initial // Utilisez Result.Initial() ou Result.Success(Unit) si c'est ce que vous voulez
    }

    /**
     * Réinitialise l'état de téléchargement de média.
     */
    fun resetUploadMediaState() {
        //_uploadMediaState.value = Result.Initial() // Utilisez Result.Initial()
        _uploadMediaState.value = Result.Initial // Utilisez Result.Initial()
    }
}

