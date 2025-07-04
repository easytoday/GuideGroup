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
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
import com.easytoday.guidegroup.domain.usecase.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val messageRepository: MessageRepository,
    private val authRepository: AuthRepository,
    // CORRECTION : On a besoin du POI repository pour récupérer les infos du POI
    private val pointOfInterestRepository: PointOfInterestRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _groupId = MutableStateFlow(savedStateHandle.get<String>("groupId"))
    val groupId: StateFlow<String?> = _groupId.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // CORRECTION : Nouveau StateFlow pour exposer les POI récupérés aux messages
    private val _pointOfInterestDetails = MutableStateFlow<Map<String, com.easytoday.guidegroup.domain.model.PointOfInterest>>(emptyMap())
    val pointOfInterestDetails: StateFlow<Map<String, com.easytoday.guidegroup.domain.model.PointOfInterest>> = _pointOfInterestDetails.asStateFlow()

    private val _sendMessageState = MutableStateFlow<Result<Unit>>(Result.Initial)
    val sendMessageState: StateFlow<Result<Unit>> = _sendMessageState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _uploadMediaState = MutableStateFlow<Result<String>>(Result.Initial)
    val uploadMediaState: StateFlow<Result<String>> = _uploadMediaState.asStateFlow()

    init {
        observeCurrentUser()
        observeMessagesAndPois()
        handleSharedPoi()
    }

    private fun handleSharedPoi() {
        val poiId = savedStateHandle.get<String>("poiId")
        val poiNameEncoded = savedStateHandle.get<String>("poiName")

        if (poiId != null && poiNameEncoded != null) {
            val poiName = URLDecoder.decode(poiNameEncoded, StandardCharsets.UTF_8.toString())

            viewModelScope.launch {
                val user = _currentUser.first { it != null }
                sharePoiInChat(poiId, poiName, user!!)

                savedStateHandle.remove<String>("poiId")
                savedStateHandle.remove<String>("poiName")
            }
        }
    }

    fun setGroupId(id: String?) {
        if (id != null && _groupId.value != id) {
            _groupId.value = id
        }
    }

    private fun observeCurrentUser() {
        authRepository.getCurrentUser().onEach { result ->
            if (result is Result.Success) {
                _currentUser.value = result.data
            }
        }.launchIn(viewModelScope)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeMessagesAndPois() {
        _groupId.flatMapLatest { id ->
            if (id != null) {
                // On observe les messages et les POI en parallèle
                combine(
                    messageRepository.getMessagesForGroup(id),
                    pointOfInterestRepository.getGroupPointsOfInterest(id)
                ) { messages, pois ->
                    // On met à jour les deux états
                    _messages.value = messages
                    _pointOfInterestDetails.value = pois.associateBy { it.id }
                }
            } else {
                emptyFlow()
            }
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
                mediaType = Message.MediaType.TEXT,
                groupId = currentGroupId
            )
            sendMessageUseCase(currentGroupId, message).collect()
        }
    }

    fun sendMediaMessage(uri: Uri, mediaType: Message.MediaType) {
        val currentGroupId = _groupId.value ?: return
        val sender = _currentUser.value ?: return
        viewModelScope.launch {
            messageRepository.uploadMedia(uri, mediaType, currentGroupId).collect { uploadResult ->
                if (uploadResult is Result.Success) {
                    val message = Message(
                        senderId = sender.id,
                        senderName = sender.username,
                        mediaUrl = uploadResult.data,
                        mediaType = mediaType,
                        groupId = currentGroupId
                    )
                    sendMessageUseCase(currentGroupId, message).collect()
                }
            }
        }
    }

    private fun sharePoiInChat(poiId: String, poiName: String, sender: User) {
        val currentGroupId = _groupId.value ?: return
        viewModelScope.launch {
            val message = Message(
                senderId = sender.id,
                senderName = sender.username,
                text = "Point d'intérêt partagé : $poiName",
                mediaType = Message.MediaType.POI,
                poiId = poiId,
                groupId = currentGroupId
            )
            sendMessageUseCase(currentGroupId, message).collect()
        }
    }

    fun resetSendMessageState() {
        _sendMessageState.value = Result.Initial
    }
    fun resetUploadMediaState() {
        _uploadMediaState.value = Result.Initial
    }
}