package com.easytoday.guidegroup.presentation.screens.main

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.presentation.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Écran "intelligent" pour le chat.
 */
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val sendMessageState by viewModel.sendMessageState.collectAsState()
    val uploadMediaState by viewModel.uploadMediaState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val groupId by viewModel.groupId.collectAsState()

    ChatScreenContent(
        groupId = groupId,
        messages = messages,
        currentUser = currentUser,
        sendMessageState = sendMessageState,
        uploadMediaState = uploadMediaState,
        onSendMessage = { text -> viewModel.sendMessage(text) },
        onPickImage = { uri -> viewModel.sendMediaMessage(uri, Message.MediaType.IMAGE) },
        onPickVideo = { uri -> viewModel.sendMediaMessage(uri, Message.MediaType.VIDEO) },
        onNavigateBack = { navController.popBackStack() },
        onResetSendState = { viewModel.resetSendMessageState() },
        onResetUploadState = { viewModel.resetUploadMediaState() }
    )
}

/**
 * Écran d'affichage "stupide" pour le chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    groupId: String?,
    messages: List<Message>,
    currentUser: User?,
    sendMessageState: Result<Unit>,
    uploadMediaState: Result<String>,
    onSendMessage: (String) -> Unit,
    onPickImage: (Uri) -> Unit,
    onPickVideo: (Uri) -> Unit,
    onNavigateBack: () -> Unit,
    onResetSendState: () -> Unit,
    onResetUploadState: () -> Unit
) {
    var messageInput by remember { mutableStateOf("") }
    var showMediaOptions by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val pickImageLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            showMediaOptions = false
            onPickImage(it)
        }
    }
    val pickVideoLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            showMediaOptions = false
            onPickVideo(it)
        }
    }

    LaunchedEffect(sendMessageState) {
        if (sendMessageState is Result.Error) {
            Toast.makeText(context, "Erreur: ${sendMessageState.message}", Toast.LENGTH_LONG).show()
            onResetSendState()
        }
    }
    LaunchedEffect(uploadMediaState) {
        if (uploadMediaState is Result.Error) {
            Toast.makeText(context, "Erreur upload: ${uploadMediaState.message}", Toast.LENGTH_LONG).show()
            onResetUploadState()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat du groupe ${groupId ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        },
        bottomBar = {
            Column {
                if (uploadMediaState is Result.Loading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                MessageInput(
                    messageInput = messageInput,
                    onMessageInputChange = { messageInput = it },
                    onSendMessage = {
                        if (messageInput.isNotBlank()) {
                            onSendMessage(messageInput)
                            messageInput = ""
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    },
                    onAttachMedia = { showMediaOptions = !showMediaOptions },
                    isSending = sendMessageState is Result.Loading || uploadMediaState is Result.Loading
                )
                if (showMediaOptions) {
                    MediaOptionsPanel(
                        onPickImage = { pickImageLauncher.launch("image/*") },
                        onPickVideo = { pickVideoLauncher.launch("video/*") }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 8.dp),
            state = listState
        ) {
            if (messages.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Aucun message. Commencez la conversation !")
                    }
                }
            } else {
                items(messages) { message ->
                    MessageBubble(message = message, isCurrentUser = message.senderId == currentUser?.id)
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    val backgroundColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = alignment
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            when (message.mediaType) {
                Message.MediaType.POI -> {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).clickable { /* TODO */ }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = "POI", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = message.text ?: "Point d'intérêt",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                else -> {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        if (!isCurrentUser) {
                            Text(
                                text = message.senderName,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        message.text?.let { Text(text = it) }
                        message.timestamp?.let {
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(it),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInput(
    messageInput: String,
    onMessageInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachMedia: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onAttachMedia) {
            Icon(Icons.Default.Add, contentDescription = "Joindre un fichier")
        }
        OutlinedTextField(
            value = messageInput,
            onValueChange = onMessageInputChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Votre message...") },
            shape = RoundedCornerShape(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilledIconButton(
            onClick = onSendMessage,
            enabled = messageInput.isNotBlank() && !isSending
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Envoyer")
            }
        }
    }
}

@Composable
fun MediaOptionsPanel(onPickImage: () -> Unit, onPickVideo: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(onClick = onPickImage) { Icon(Icons.Default.Image, contentDescription = "Image") }
        IconButton(onClick = onPickVideo) { Icon(Icons.Default.Videocam, contentDescription = "Vidéo") }
        IconButton(onClick = { /* TODO */ }, enabled = false) { Icon(Icons.Default.Mic, contentDescription = "Audio") }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    val fakeMessages = listOf(
        Message(id="1", senderId="user2", senderName="Bob", text="Salut tout le monde!", timestamp= Date()),
        Message(id="2", senderId="user1", senderName="Alice (Moi)", text="Hey! Bien arrivé?", timestamp= Date()),
    )
    val fakeUser = User(id="user1", username="Alice (Moi)")

    ChatScreenContent(
        groupId = "preview_group",
        messages = fakeMessages,
        currentUser = fakeUser,
        sendMessageState = Result.Initial,
        uploadMediaState = Result.Initial,
        onSendMessage = {},
        onPickImage = {},
        onPickVideo = {},
        onNavigateBack = {},
        onResetSendState = {},
        onResetUploadState = {}
    )
}