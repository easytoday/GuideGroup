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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val messageRepository: MessageRepository,
    private val authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _groupId = MutableStateFlow(savedStateHandle.get<String>("groupId"))
    val groupId: StateFlow<String?> = _groupId.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _sendMessageState = MutableStateFlow<Result<Unit>>(Result.Initial)
    val sendMessageState: StateFlow<Result<Unit>> = _sendMessageState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _uploadMediaState = MutableStateFlow<Result<String>>(Result.Initial)
    val uploadMediaState: StateFlow<Result<String>> = _uploadMediaState.asStateFlow()

    init {
        observeCurrentUser()
        observeMessages()
    }

    // CORRECTION : Permet à un autre écran de configurer l'ID du groupe pour ce ViewModel.
    fun setGroupId(id: String?) {
        if (id != null && _groupId.value != id) {
            _groupId.value = id
        }
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { result ->
                if (result is Result.Success) {
                    _currentUser.value = result.data
                }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeMessages() {
        _groupId.flatMapLatest { id ->
            if (id != null) {
                messageRepository.getMessagesForGroup(id)
            } else {
                emptyFlow()
            }
        }.onEach { messagesList ->
            _messages.value = messagesList
        }.launchIn(viewModelScope)
    }

    fun sendMessage(text: String) {
        val currentGroupId = _groupId.value ?: return
        val sender = _currentUser.value ?: return

        viewModelScope.launch {
            val message = Message(
                senderId = sender.id,
                senderName = sender.username,
                text = text,
                mediaType = Message.MediaType.TEXT
            )

            _sendMessageState.value = Result.Loading
            sendMessageUseCase(currentGroupId, message).collect { result ->
                _sendMessageState.value = result
                if (result !is Result.Loading) {
                    resetSendMessageState()
                }
            }
        }
    }

    fun sendMediaMessage(uri: Uri, mediaType: Message.MediaType) {
        val currentGroupId = _groupId.value ?: return
        val sender = _currentUser.value ?: return

        viewModelScope.launch {
            messageRepository.uploadMedia(uri, mediaType, currentGroupId).collect { uploadResult ->
                _uploadMediaState.value = uploadResult
                if (uploadResult is Result.Success) {
                    val message = Message(
                        senderId = sender.id,
                        senderName = sender.username,
                        mediaUrl = uploadResult.data,
                        mediaType = mediaType
                    )
                    sendMessageUseCase(currentGroupId, message).collect { sendResult ->
                        _sendMessageState.value = sendResult
                        if (sendResult !is Result.Loading) {
                            resetSendMessageState()
                            resetUploadMediaState()
                        }
                    }
                }
            }
        }
    }

    fun sharePoiInChat(poiId: String, poiName: String) {
        val currentGroupId = _groupId.value ?: return
        val sender = _currentUser.value ?: return

        viewModelScope.launch {
            val message = Message(
                senderId = sender.id,
                senderName = sender.username,
                text = "Point d'intérêt partagé : $poiName",
                mediaType = Message.MediaType.POI,
                poiId = poiId
            )

            _sendMessageState.value = Result.Loading
            sendMessageUseCase(currentGroupId, message).collect { result ->
                _sendMessageState.value = result
                if (result !is Result.Loading) {
                    resetSendMessageState()
                }
            }
        }
    }

    fun resetSendMessageState() {
        _sendMessageState.value = Result.Initial
    }

    fun resetUploadMediaState() {
        _uploadMediaState.value = Result.Initial
    }
}