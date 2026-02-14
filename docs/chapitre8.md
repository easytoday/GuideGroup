# Annexe

Vous trouverez dans cet annexe les implémentations en kotlin mentionnées dans les chapitres.

## implémentation chapitre 2

### exemple de module Hilt

Voici l'implémentation en kotlin du module hilt pour fournir l'authentification avec Firebaseauth

Exemple de module Hilt pour fournir Firebaseauth :

``` {.kotlin caption="exemple de module Hilt pour fournir Firebaseauth" language="kotlin"}
// com.easytoday.guidegroup.di.appmodule.kt
@module
// le scope de la dépendance (ici, une seule instance pour toute l'app)
@installin(singletoncomponent::class) 
object appmodule {
    @provides
    @singleton // garantit une seule instance de Firebaseauth
    fun providefirebaseauth(): firebaseauth {
    return firebaseauth.getinstance()
    }

    @provides
    @singleton
    fun providefirebasefirestore(): firebasefirestore {
    return firebasefirestore.getinstance()
    }

    @provides
    @singleton
    fun providefirebasestorage(): firebasestorage {
    return firebasestorage.getinstance()
    }

    // fournit l'implémentation concrète des répertoires
    @provides
    @singleton
    fun provideauthrepository(impl: authrepositoryimpl): authrepository = impl

    @provides
    @singleton
    fun providemessagerepository(impl: messagerepositoryimpl): messagerepository = impl

    @provides
    @singleton
    fun providepointofinterestrepository(impl: pointofinterestrepositoryimpl): pointofinterestrepository = impl
}
```

## implémentation chapitre 4

### implémentation du modèle

Voic l'implémention en kotlin de la couche modèle : User.kt, Result.kt, AuthRepository.kt (interface d'authentification), AuthRepositoryImpl.kt (implementation de l'authentification), SignInUseCase.kt (use case d'authentification)

``` {.kotlin caption="User.kt (Modèle de Données)" language="kotlin"}
// com.easytoday.guidegroup.domain.model.User.kt
package com.easytoday.guidegroup.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val role: String = "participant" // "guide" ou "participant"
)
```

``` {.kotlin caption="Result.kt (Classe Scellée pour la Gestion des Résultats)" language="kotlin"}
// com.easytoday.guidegroup.domain.utils.Result.kt
package com.easytoday.guidegroup.domain.utils

sealed class Result<out T> {
    object Loading : Result<Nothing>()
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
}
```

``` {.kotlin caption="AuthRepository.kt (Interface du Référentiel d'Authentification)" language="kotlin"}
// com.easytoday.guidegroup.domain.repository.AuthRepository.kt
package com.easytoday.guidegroup.domain.repository

import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.utils.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun signIn(
    email: String, 
    password: String
    ): Flow<Result<User>>
    
    fun signUp(
    email: String, 
    password: String, 
    username: String, 
    role: String): Flow<Result<User>>
    fun getCurrentUser(): Flow<Result<User?>>
    fun signOut(): Flow<Result<Unit>>
}
```

::: center
``` {.kotlin caption="AuthRepositoryImpl.kt (Implémentation du Référentiel d'Authentification)" language="kotlin"}
// com.easytoday.guidegroup.data.repository.impl.AuthRepositoryImpl.kt
package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.utils.Result
import com.easytoday.guidegroup.data.firestore.FirestoreHelper // Classe utilitaire pour Firestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Pour la création du FirestoreHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestoreHelper: FirestoreHelper // Injecté via Hilt
) : AuthRepository {

    override fun signIn(
    email: 
    String, 
    password: String
    ): Flow<Result<User>> = flow {
    emit(Result.Loading) // Émettre un état de chargement

    try {
        val authResult = firebaseAuth.signInWithEmailAndPassword(
        email, 
        password
        ).await()
        
        val firebaseUser = authResult.user

        if (firebaseUser != null) {
        // Si l'authentification Firebase réussit, récupérer le profil utilisateur depuis Firestore
        val userProfile = firestoreHelper.getDocument(
        "users", 
        firebaseUser.uid, 
        User::class.java).firstOrNull()

        if (userProfile != null) {
            emit(Result.Success(userProfile))
        } else {
            // Si l'utilisateur n'a pas de profil Firestore après s'être connecté (cas rare après l'inscription)
            emit(Result.Error(Exception("Profil utilisateur introuvable.")))
        }
        } else {
        emit(Result.Error(Exception("Erreur d'authentification Firebase.")))
        }
    } catch (e: Exception) {
        emit(Result.Error(e)) // Émettre une erreur en cas d'exception
    }
    }

    override fun signUp(
    email: String, 
    password: String, 
    username: String, 
    role: String
    ): Flow<Result<User>> = flow {
    emit(Result.Loading)
    try {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(
        email, 
        password
        ).await()
        
        val firebaseUser = authResult.user

        if (firebaseUser != null) {
        // Créer un nouvel objet User pour Firestore
        val newUser = User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            username = username,
            role = role
        )
        // Enregistrer le profil utilisateur dans Firestore
        firestoreHelper.addDocument("users", newUser, firebaseUser.uid).await()
        emit(Result.Success(newUser))
        } else {
        emit(Result.Error(Exception("Échec de la création du compte Firebase.")))
        }
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
    }

    override fun getCurrentUser(): Flow<Result<User?>> = flow {
    emit(Result.Loading)
    try {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
        // Récupérer le profil utilisateur complet depuis Firestore
        val userProfile = firestoreHelper.getDocument(
        "users", 
        firebaseUser.uid, 
        User::class.java).firstOrNull()
        
        emit(Result.Success(userProfile))
        } else {
        emit(Result.Success(null)) // Aucun utilisateur connecté
        }
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
    }

    override fun signOut(): Flow<Result<Unit>> = flow {
    emit(Result.Loading)
    try {
        firebaseAuth.signOut()
        emit(Result.Success(Unit))
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
    }
}
```
:::

``` {.kotlin caption="SignInUseCase.kt (Use Case d'Authentification)" language="kotlin"}
// com.easytoday.guidegroup.domain.usecase.SignInUseCase.kt
package com.easytoday.guidegroup.domain.usecase

import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
// La convention 'operator fun invoke' permet d'appeler le Use Case comme une fonction
    operator fun invoke(email: String, password: String): Flow<Result<User>> {
    // Ici, on pourrait ajouter une logique métier spécifique à la connexion avant de déléguer au repository.
    // Ex: validation complexe d'e-mail/mot de passe au-delà de la simple vérification de format.
    // Pour cet exemple, nous déléguons directement.
    return authRepository.signIn(email, password)
    }
}
```

### implémentation du ViewModel

Voici l'implémentation en kotlin de la couche de présentation : AuthUiState.kt ( modèle d'état de l'Ui), LogicViewModel.kt (ViewModel),

``` {.kotlin caption="AuthUiState.kt (Modèle d'État UI)" language="kotlin"}
// com.easytoday.guidegroup.presentation.auth.AuthUiState.kt
package com.easytoday.guidegroup.presentation.auth

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignInSuccess: Boolean = false // Indique si la connexion a réussi
)
```

``` {.kotlin caption="LoginViewModel.kt (ViewModel)" language="kotlin"}
// com.easytoday.guidegroup.presentation.auth.LoginViewModel.kt
package com.easytoday.guidegroup.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.usecase.SignInUseCase
import com.easytoday.guidegroup.domain.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // Annotation Hilt pour injecter le ViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase // Hilt injecte le Use Case
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun signIn(email: String, password: String) {
    viewModelScope.launch {
        // Mettre à jour l'état de l'UI pour indiquer le chargement et effacer les erreurs précédentes
        _uiState.update { it.copy(isLoading = true, errorMessage = null, isSignInSuccess = false) }

        signInUseCase(email, password).collect { result ->
        when (result) {
            is Result.Loading -> {
            _uiState.update { it.copy(isLoading = true) }
            }
            is Result.Success -> {
            _uiState.update { it.copy(isLoading = false, isSignInSuccess = true) }
            // Pas besoin de naviguer ici. La Vue observera isSignInSuccess et naviguera.
            }
            is Result.Error -> {
            _uiState.update { it.copy(isLoading = false, errorMessage = result.exception.message) }
            }
        }
        }
    }
    }
}
```

### implémentation de la Vue

Voici l'implémentation en kotlin de la couche vue : LoginScreen.kt

``` {.kotlin caption="LoginScreen.kt (Composable Jetpack Compose)" language="kotlin"}
// com.easytoday.guidegroup.presentation.auth.LoginScreen.kt
package com.easytoday.guidegroup.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
// Pour injecter le ViewModel
import androidx.hilt.navigation.compose.hiltViewModel 
// Pour observer le StateFlow en toute sécurité
import androidx.lifecycle.compose.collectAsStateWithLifecycle 
// Pour la navigation
import androidx.navigation.NavController 

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    // Le ViewModel est fourni par Hilt
    viewModel: LoginViewModel = hiltViewModel() 
) {
    // Collecte l'état UI du ViewModel. La Vue se recompose quand cet état change.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // États locaux pour les champs de saisie (non gérés par le ViewModel)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Effet de bord pour la navigation après un succès de connexion
    // LaunchedEffect est un composable qui lance une coroutine et la gère
    LaunchedEffect(uiState.isSignInSuccess) {
    if (uiState.isSignInSuccess) {
        // Naviguer vers l'écran principal (ex: Home/MapScreen)
        navController.navigate("main_screen") {
        // Empêcher de revenir à l'écran de connexion via le bouton retour
        popUpTo("login_route") { inclusive = true }
        }
    }
    }

    Scaffold(
    topBar = {
        TopAppBar(title = { Text("Connexion à GuideGroup") })
    }
    ) { paddingValues ->
    Column(
        modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Mot de passe") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
        onClick = { viewModel.signIn(email, password) },
        enabled = !uiState.isLoading, // Désactiver le bouton pendant le chargement
        modifier = Modifier.fillMaxWidth()
        ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
            modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text("Se connecter")
        }
        }
        Spacer(Modifier.height(8.dp))

        // Afficher le message d'erreur si présent
        uiState.errorMessage?.let {
        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        // Bouton pour la navigation vers l'inscription (non implémenté ici pour la concision)
        TextButton(onClick = { navController.navigate("signup_route") }) {
        Text("Pas encore de compte ? S'inscrire")
        }
    }
    }
}
```

## implémentation chapitre 5

### implémentation du Modèle

Voici l'implémentation en kotlin de la couche domaine et données :Message.kt ( modèle de données), MessageRepository.kt (interface du référentiel de messages), MessageRepositoryImpl.kt (implémentation du référentiel de messages), SendMessageUseCase.kt (use case d'envoie de message)

``` {.kotlin caption="Message.kt (Modèle de Données)" language="kotlin"}
// com.easytoday.guidegroup.domain.model.Message.kt
package com.easytoday.guidegroup.domain.model

import java.util.Date

data class Message(
    val id: String = "",
    val groupId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String? = null,
    val mediaUrl: String? = null,
    // Pour gérer différents types de messages (texte, image, etc.)
    val mediaType: MediaType = MediaType.TEXT, 
    val timestamp: Date = Date()
) {
    enum class MediaType {
    // Enum pour distinguer les types de contenu
    TEXT, IMAGE, VIDEO, AUDIO 
    }
}
```

``` {.kotlin caption="MessageRepository.kt (Interface du Référentiel de Messages)" language="kotlin"}
// com.easytoday.guidegroup.domain.repository.MessageRepository.kt
package com.easytoday.guidegroup.domain.repository

import android.net.Uri
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.utils.Result
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun sendMessage(groupId: String, message: Message): Flow<Result<Unit>>
    fun getMessagesForGroup(groupId: String): Flow<List<Message>> // Pour observer les messages en temps réel
    fun uploadMedia(uri: Uri, type: Message.MediaType, groupId: String): Flow<Result<String>> // Pour les médias
}
```

``` {.kotlin caption="MessageRepositoryImpl.kt (Implémentation du Référentiel de Messages)" language="kotlin"}
// com.easytoday.guidegroup.data.repository.impl.MessageRepositoryImpl.kt
package com.easytoday.guidegroup.data.repository.impl

import android.net.Uri
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.repository.MessageRepository
import com.easytoday.guidegroup.domain.utils.Result
import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper,
    private val firebaseStorage: FirebaseStorage // Injecté pour la gestion des médias
) : MessageRepository {

    override fun sendMessage(groupId: String, message: Message): Flow<Result<Unit>> = flow {
    emit(Result.Loading) // Indique le début de l'opération
    try {
        // Stocke le message dans une sous-collection 'messages' du document du groupe.
        // L'ID du message est généré ou provient déjà de l'objet Message.
        firestoreHelper.addDocument("groups/$groupId/messages", message, message.id).await()
        emit(Result.Success(Unit)) // Opération réussie
    } catch (e: Exception) {
        emit(Result.Error(e)) // Gère les erreurs
    }
    }

    override fun getMessagesForGroup(groupId: String): Flow<List<Message>> {
    // Observe la collection de messages en temps réel pour un groupe spécifique.
    // Utilise FirestoreHelper pour récupérer les données et les mapper en objets Message.
    return firestoreHelper.getCollection("groups/$groupId/messages", Message::class.java)
        .map { messages -> messages.sortedBy { it.timestamp } } // Trie les messages par horodatage
    }

    override fun uploadMedia(uri: Uri, type: Message.MediaType, groupId: String): Flow<Result<String>> = flow {
    emit(Result.Loading)
    try {
        // Crée une référence dans Firebase Storage pour le média.
        val storageRef = firebaseStorage.reference.child("chat_media/<span class="math-inline">groupId/</span>{System.currentTimeMillis()}_${uri.lastPathSegment}")
        // Téléverse le fichier
        val uploadTask = storageRef.putFile(uri).await()
        // Récupère l'URL de téléchargement du fichier téléversé
        val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
        emit(Result.Success(downloadUrl)) // Retourne l'URL du média
    } catch (e: Exception) {
        emit(Result.Error(e))
    }
    }
}
```

``` {.kotlin caption="SendMessageUseCase.kt (Use Case d'Envoi de Message)" language="kotlin"}
// com.easytoday.guidegroup.domain.usecase.SendMessageUseCase.kt
package com.easytoday.guidegroup.domain.usecase

import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.repository.MessageRepository
import com.easytoday.guidegroup.domain.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val messageRepository: MessageRepository // Injecté via Hilt
) {
    operator fun invoke(groupId: String, message: Message): Flow<Result<Unit>> {
    // Ici, on pourrait ajouter de la logique métier avant d'envoyer le message:
    // ex: Filtrage de contenu, validation de la taille du message, enrichissement du message, etc.
    // Pour cet exemple, nous déléguons directement au repository.
    return messageRepository.sendMessage(groupId, message)
    }
}
```

### implémentation du ViewModel

Voici l'implémentation en kotlin de la couche présentation : ChatUiState.kt (modèle d'état UI), ChatViewModel (ViewModel)

``` {.kotlin caption="ChatUiState.kt (Modèle d'État UI)" language="kotlin"}
// com.easytoday.guidegroup.presentation.chat.ChatUiState.kt
package com.easytoday.guidegroup.presentation.chat

data class ChatUiState(
// Indique si les messages sont en cours de chargement
    val isLoadingMessages: Boolean = false,
    // Message d'erreur si l'envoi échoue 
    val sendMessageError: String? = null,  
    // Nom de l'utilisateur courant, pour afficher "Vous" 
    val currentUserName: String? = null     
)
```

``` {.kotlin caption="ChatViewModel.kt (ViewModel)" language="kotlin"}
// com.easytoday.guidegroup.presentation.chat.ChatViewModel.kt

package com.easytoday.guidegroup.presentation.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.Message
import com.easytoday.guidegroup.domain.repository.AuthRepository // Pour obtenir l'utilisateur courant
import com.easytoday.guidegroup.domain.repository.MessageRepository // Pour la lecture des messages
import com.easytoday.guidegroup.domain.usecase.SendMessageUseCase
import com.easytoday.guidegroup.domain.utils.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessageUseCase: SendMessageUseCase,
    private val messageRepository: MessageRepository, // Aussi utilisé pour le chargement en temps réel des messages
    private val authRepository: AuthRepository, // Pour obtenir les infos de l'expéditeur
    savedStateHandle: SavedStateHandle // Pour récupérer les arguments de navigation (groupId)
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow() // Expose l'état UI

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow() // Expose la liste des messages

    private val currentGroupId: String = savedStateHandle.get<String>("groupId") ?: ""
    private var currentUserId: String? = null
    private var currentUserName: String? = null

    init {
    // Initialise le chargement de l'utilisateur courant et des messages du groupe
    loadCurrentUser()
    if (currentGroupId.isNotEmpty()) {
        loadMessages(currentGroupId)
    }
    }

    private fun loadCurrentUser() {
    viewModelScope.launch {
        authRepository.getCurrentUser().collect { result ->
        if (result is Result.Success) {
            currentUserId = result.data?.id
            currentUserName = result.data?.username
            _uiState.update { it.copy(currentUserName = currentUserName) } // Met à jour l'état UI avec le nom de l'utilisateur
        }
        // Gérer les erreurs de chargement de l'utilisateur si nécessaire
        }
    }
    }

    private fun loadMessages(groupId: String) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoadingMessages = true) } // Indique que les messages sont en cours de chargement
        messageRepository.getMessagesForGroup(groupId)
        .onEach { _uiState.update { it.copy(isLoadingMessages = false) } } // Cesse le chargement une fois les données reçues
        .catch { e -> _uiState.update { it.copy(isLoadingMessages = false, sendMessageError = "Erreur de chargement des messages: ${e.message}") } } // Gère les erreurs
        .collect { messagesList ->
            _messages.value = messagesList // Met à jour la liste des messages, la Vue se recompose automatiquement
        }
    }
    }

    fun sendMessage(text: String) {
    // Vérifie si l'utilisateur est connecté et si le texte n'est pas vide
    val senderId = currentUserId ?: run {
        _uiState.update { it.copy(sendMessageError = "Erreur: Utilisateur non connecté pour envoyer un message.") }
        return
    }
    val senderName = currentUserName ?: "Inconnu" // Utilise "Inconnu" si le nom n'est pas disponible

    if (text.isBlank()) {
        _uiState.update { it.copy(sendMessageError = "Le message ne peut pas être vide.") }
        return
    }

    // Crée un objet Message avec les informations nécessaires
    val message = Message(
        id = java.util.UUID.randomUUID().toString(), // Génère un ID unique pour le message
        groupId = currentGroupId,
        senderId = senderId,
        senderName = senderName,
        text = text.trim(), // Supprime les espaces superflus
        mediaType = Message.MediaType.TEXT,
        timestamp = Date() // Horodatage actuel
    )

    viewModelScope.launch {
        sendMessageUseCase(currentGroupId, message) // Appelle le Use Case pour envoyer le message
        .collect { result ->
            when (result) {
            is Result.Loading -> _uiState.update { it.copy(sendMessageError = null) } // Efface les erreurs précédentes
            is Result.Success -> {
                // Le message a été envoyé avec succès.
                // La liste des messages affichée par la Vue sera automatiquement mise à jour
                // par le Flow de getMessagesForGroup qui réagit aux changements Firestore.
                // Donc, pas de mise à jour directe de _messages.value ici.
            }
            is Result.Error -> _uiState.update { it.copy(sendMessageError = "Échec de l'envoi: ${result.exception.message}") } // Affiche l'erreur
            }
        }
    }
    }
}
```

### implémentation de la vue

Voici l'implémentation en kotlin de la couche vue : ChatScreen.kt

``` {.kotlin caption="ChatScreen.kt (Composable Jetpack Compose)" language="kotlin"}
// com.easytoday.guidegroup.presentation.chat.ChatScreen.kt
package com.easytoday.guidegroup.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.easytoday.guidegroup.domain.model.Message // Import correct du modèle Message
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController, // Pour la navigation si nécessaire (ex: retour)
    viewModel: ChatViewModel = hiltViewModel() // Le ViewModel est injecté par Hilt
) {
    // Collecte l'état UI et la liste des messages du ViewModel.
    // La Vue se recompose automatiquement lorsque ces états changent.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    // État local pour le champ de saisie du message
    var messageInput by remember { mutableStateOf("") }

    Scaffold(
    topBar = {
        TopAppBar(title = { Text("Chat de Groupe") })
    },
    bottomBar = {
        // Barre de saisie de message et bouton d'envoi
        Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
        ) {
        OutlinedTextField(
            value = messageInput,
            onValueChange = { messageInput = it },
            label = { Text("Tapez votre message...") },
            modifier = Modifier.weight(1f), // Prend tout l'espace disponible
            singleLine = true // Empêche le champ de s'étendre sur plusieurs lignes
        )
        Spacer(Modifier.width(8.dp))
        IconButton(
            onClick = {
            // N'envoie que si le message n'est pas vide et si l'envoi n'est pas déjà en cours
            if (messageInput.isNotBlank() && !uiState.isLoadingMessages) {
                viewModel.sendMessage(messageInput)
                messageInput = "" // Efface le champ après envoi
            }
            },
            // Le bouton est désactivé si l'envoi est en cours ou si le champ est vide
            enabled = messageInput.isNotBlank() && !uiState.isLoadingMessages
        ) {
            Icon(Icons.Filled.Send, contentDescription = "Envoyer le message")
        }
        }
    }
    ) { paddingValues ->
    Column(
        modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
        // Affichage de l'indicateur de chargement des messages
        if (uiState.isLoadingMessages) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Liste déroulante des messages
        LazyColumn(
        modifier = Modifier
            .weight(1f) // Prend le reste de l'espace vertical
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        reverseLayout = true // Pour que les derniers messages soient en bas de la liste (chat classique)
        ) {
        // 'items' prend la liste des messages et génère un composable pour chaque message
        items(messages) { message ->
            // Détermine si le message a été envoyé par l'utilisateur courant
            val isCurrentUser = message.senderId == uiState.currentUserName // ATTENTION: C'est une simplification ici. Il faudrait comparer avec currentUserId réel
            MessageBubble(message = message, isCurrentUser = isCurrentUser)
        }
        }

        // Affichage de l'erreur d'envoi de message
        uiState.sendMessageError?.let { errorMessage ->
        Text(
            errorMessage,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(8.dp)
        )
        }
    }
    }
}

// Composant réutilisable pour afficher une bulle de message
@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    // Choisit la couleur de fond et l'alignement en fonction de l'expéditeur
    val backgroundColor = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val alignment = if (isCurrentUser) Alignment.End else Alignment.Start

    // Formatte l'horodatage
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { message.timestamp?.let { timeFormatter.format(it) } ?: "" }

    Row(
    modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
    horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
    Card(
        modifier = Modifier.widthIn(max = 300.dp), // Limite la largeur des bulles
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
        // Affiche le nom de l'expéditeur si ce n'est pas l'utilisateur courant
        if (!isCurrentUser) {
            Text(
            text = message.senderName,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        // Affiche le contenu texte du message
        message.text?.let {
            Text(text = it, style = MaterialTheme.typography.bodyLarge, color = textColor)
        }
        // TODO: Ajouter l'affichage des médias (image, vidéo) si mediaUrl est non nul

        // Affiche l'heure du message
        Text(
            text = formattedTime,
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.7f), // Un peu plus transparent
            modifier = Modifier.align(Alignment.End)
        )
        }
    }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChatScreen() {
    // Créez un faux NavController pour la prévisualisation
    val navController = rememberNavController()
    // Utilisez un ViewModel factice ou un mock pour la prévisualisation si nécessaire
    // ChatScreen(navController = navController, viewModel = MockChatViewModel())
    // Pour une prévisualisation simple, on peut appeler directement les composables internes.
    // Ou, si le viewModel n'a pas de dépendances complexes, hiltViewModel() peut fonctionner avec un faux graphe Hilt.
    MessageBubble(message = Message(senderName = "Moi", text = "Ceci est mon message.", senderId = "user1", groupId = "group1"), isCurrentUser = true)
    MessageBubble(message = Message(senderName = "Autre", text = "Bonjour à tous !", senderId = "user2", groupId = "group1"), isCurrentUser = false)
}
```

## implémentation chapitre 6

### implémentation du Modèle

Voici l'implémentation en kotlin de la couche domaine et données : PointOfInterest.kt (modèle de données), PointOfInterestRepository.kt (interface du référentiel de POI), PointOfInterestRepositoryImpl.kt (implémentation du référentiel de POI), AddPointOfInterestUseCase.kt (use case d'ajout de POI)

``` {.kotlin caption="PointOfInterest.kt` (Modèle de Données)" language="kotlin"}
// com.easytoday.guidegroup.domain.model.PointOfInterest.kt
package com.easytoday.guidegroup.domain.model

import java.util.Date

data class PointOfInterest(
    val id: String = "",
    val groupId: String = "",
    val name: String = "",
    val description: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    // User ID du guide qui l'a ajouté
    val addedBy: String = "", 
    val timestamp: Date = Date()
)
```

``` {.kotlin caption="PointOfInterestRepository.kt (Interface du Référentiel de POI)" language="kotlin"}
// com.easytoday.guidegroup.domain.repository.PointOfInterestRepository.kt
package com.easytoday.guidegroup.domain.repository

import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.utils.Result
import kotlinx.coroutines.flow.Flow

interface PointOfInterestRepository {
    fun addPointOfInterest(groupId: String, poi: PointOfInterest): Flow<Result<Unit>>
    fun getPointsOfInterestForGroup(groupId: String): Flow<List<PointOfInterest>> // Pour observer les POI en temps réel
}
```

``` {.kotlin caption="PointOfInterestRepositoryImpl.kt (Implémentation du Référentiel de POI)" language="kotlin"}
// com.easytoday.guidegroup.data.repository.impl.PointOfInterestRepositoryImpl.kt
package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
import com.easytoday.guidegroup.domain.utils.Result
import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class PointOfInterestRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper
) : PointOfInterestRepository {

    override fun addPointOfInterest(groupId: String, poi: PointOfInterest): Flow<Result<Unit>> = flow {
    emit(Result.Loading) // Indique le début de l'opération
    try {
        // Ajoute le POI dans une sous-collection 'pointsOfInterest' du document du groupe.
        // L'ID du POI est généré ou provient déjà de l'objet PointOfInterest.
        firestoreHelper.addDocument("groups/$groupId/pointsOfInterest", poi, poi.id).await()
        emit(Result.Success(Unit)) // Opération réussie
    } catch (e: Exception) {
        emit(Result.Error(e)) // Gère les erreurs
    }
    }

    override fun getPointsOfInterestForGroup(groupId: String): Flow<List<PointOfInterest>> {
    // Observe la collection de POI en temps réel pour un groupe spécifique.
    return firestoreHelper.getCollection("groups/$groupId/pointsOfInterest", PointOfInterest::class.java)
        .map { pois -> pois.sortedByDescending { it.timestamp } } // Trie par les plus récents en premier
    }
}
```

``` {.kotlin caption="AddPointOfInterestUseCase.kt (Use Case d'Ajout de POI)" language="kotlin"}
// com.easytoday.guidegroup.domain.usecase.AddPointOfInterestUseCase.kt
package com.easytoday.guidegroup.domain.usecase

import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
import com.easytoday.guidegroup.domain.utils.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AddPointOfInterestUseCase @Inject constructor(
    private val pointOfInterestRepository: PointOfInterestRepository
) {
    operator fun invoke(groupId: String, poi: PointOfInterest): Flow<Result<Unit>> {
    // Ici, on pourrait ajouter une logique métier spécifique avant d'ajouter le POI:
    // ex: validation des coordonnées, vérification des permissions avancées, etc.
    // Pour cet exemple, nous déléguons directement.
    return pointOfInterestRepository.addPointOfInterest(groupId, poi)
    }
}
```

### implémentation du ViewModel

Voici l'implémentation en kotlin de la couche de présentation : MapUiState.kt (modèle d'état UI pour la carte), MapViewModel.kt (pour ajout de POI)

``` {.kotlin caption="MapUiState.kt (Modèle d'État UI pour la Carte)" language="kotlin"}
// com.easytoday.guidegroup.presentation.map.MapUiState.kt
package com.easytoday.guidegroup.presentation.map

import com.google.android.gms.maps.model.LatLng // Dépendance de Google Maps
import com.easytoday.guidegroup.domain.model.PointOfInterest

data class MapUiState(
    val isLoadingMap: Boolean = false,
    val errorMessage: String? = null,
    val currentLocation: LatLng? = null, // La position actuelle de l'utilisateur
    val groupMembersLocations: Map<String, LatLng> = emptyMap(), // ID utilisateur -> LatLng
    val pointsOfInterest: List<PointOfInterest> = emptyList(),
    val showAddPoiDialog: Boolean = false, // Contrôle l'affichage du dialogue d'ajout de POI
    val tempPoiLocation: LatLng? = null, // Position temporaire du POI avant confirmation
    val isAddingPoi: Boolean = false, // Indique si l'opération d'ajout de POI est en cours
    val addPoiError: String? = null, // Erreur lors de l'ajout de POI
    val userRole: String? = null // Rôle de l'utilisateur actuel (guide/participant)
)
```

``` {.kotlin caption="MapViewModel.kt (Extrait pertinent pour l'ajout de POI)" language="kotlin"}
// com.easytoday.guidegroup.presentation.map.MapViewModel.kt
package com.easytoday.guidegroup.presentation.map

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
import com.easytoday.guidegroup.domain.usecase.AddPointOfInterestUseCase
import com.easytoday.guidegroup.domain.utils.Result
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
private val addPointOfInterestUseCase: AddPointOfInterestUseCase,
private val pointOfInterestRepository: PointOfInterestRepository, // Pour observer les POI existants
private val authRepository: AuthRepository, // Pour vérifier le rôle de l'utilisateur
savedStateHandle: SavedStateHandle
) : ViewModel() {

private val _uiState = MutableStateFlow(MapUiState())
val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

private val currentGroupId: String = savedStateHandle.get<String>("groupId") ?: ""
private var currentUserId: String? = null

init {
    // Initialisation: charger le rôle de l'utilisateur et les POI du groupe
    loadUserRole()
    if (currentGroupId.isNotEmpty()) {
    loadPointsOfInterest(currentGroupId)
    }
    // ... (autres initialisations pour la localisation des membres, etc.)
}

private fun loadUserRole() {
    viewModelScope.launch {
    authRepository.getCurrentUser().collect { result ->
        if (result is Result.Success) {
        _uiState.update { it.copy(userRole = result.data?.role, errorMessage = null) }
        currentUserId = result.data?.id
        } else if (result is Result.Error) {
        _uiState.update { it.copy(errorMessage = "Erreur de chargement du rôle: ${result.exception.message}") }
        }
    }
    }
}

private fun loadPointsOfInterest(groupId: String) {
    viewModelScope.launch {
    _uiState.update { it.copy(isLoadingPois = true, errorMessage = null) }
    pointOfInterestRepository.getPointsOfInterestForGroup(groupId)
        .onEach { _uiState.update { it.copy(isLoadingPois = false) } }
        .catch { e -> _uiState.update { it.copy(isLoadingPois = false, errorMessage = "Erreur de chargement des POI: ${e.message}") } }
        .collect { poisList ->
        _uiState.update { it.copy(pointsOfInterest = poisList) } // Met à jour les POI dans l'état UI
        }
    }
}

fun onMapLongClick(latLng: LatLng) {
    // Seuls les guides peuvent ajouter des POI
    if (_uiState.value.userRole == "guide") {
    _uiState.update { it.copy(showAddPoiDialog = true, tempPoiLocation = latLng) }
    } else {
    _uiState.update { it.copy(errorMessage = "Seuls les guides peuvent ajouter des points d'intérêt.") }
    }
}

fun dismissAddPoiDialog() {
    _uiState.update { it.copy(showAddPoiDialog = false, tempPoiLocation = null, addPoiError = null) }
}

fun addPointOfInterest(name: String, description: String) {
    // Pré-conditions : un emplacement temporaire doit être défini et l'utilisateur doit être un guide
    val location = _uiState.value.tempPoiLocation ?: run {
    _uiState.update { it.copy(addPoiError = "Emplacement du POI non défini.") }
    return
    }
    val userId = currentUserId ?: run {
    _uiState.update { it.copy(addPoiError = "Utilisateur non connecté.") }
    return
    }

    if (name.isBlank()) {
    _uiState.update { it.copy(addPoiError = "Le nom du POI ne peut pas être vide.") }
    return
    }

    val poi = PointOfInterest(
    id = java.util.UUID.randomUUID().toString(), // Génère un ID unique
    groupId = currentGroupId,
    name = name,
    description = description,
    latitude = location.latitude,
    longitude = location.longitude,
    addedBy = userId,
    timestamp = Date()
    )

    viewModelScope.launch {
    _uiState.update { it.copy(isAddingPoi = true, addPoiError = null) } // Indique que l'ajout est en cours
    addPointOfInterestUseCase(currentGroupId, poi)
        .collect { result ->
        when (result) {
            is Result.Loading -> { /* État de chargement géré par isAddingPoi */ }
            is Result.Success -> {
            _uiState.update { it.copy(isAddingPoi = false, showAddPoiDialog = false, tempPoiLocation = null) }
            // La liste des POI sera automatiquement mise à jour par le Flow de getPointsOfInterestForGroup
            }
            is Result.Error -> {
            _uiState.update { it.copy(isAddingPoi = false, addPoiError = "Échec de l'ajout du POI: ${result.exception.message}") }
            }
        }
        }
    }
}

}
```

### implémentation de la vue

Voici l'implémentation en kotlin de la couche vue : MapScreen.kt (pour ajout de POI)

``` {.kotlin caption="MapScreen.kt (Extrait pertinent pour l'ajout de POI)" language="kotlin"}
// com.easytoday.guidegroup.presentation.map.MapScreen.kt
package com.easytoday.guidegroup.presentation.map

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.* // Pour Google Maps dans Compose

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
// ... autres paramètres de navigation
viewModel: MapViewModel = hiltViewModel() // Le ViewModel est fourni par Hilt
) {
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// Position initiale de la caméra (peut être dynamisée par le ViewModel)
val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(LatLng(48.8566, 2.3522), 10f) // Paris par défaut
}

Scaffold(
    topBar = {
    TopAppBar(title = { Text("Carte de Groupe") })
    }
) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        // Gère le long-clic sur la carte pour ajouter un POI
        onMapLongClick = { latLng ->
        viewModel.onMapLongClick(latLng)
        }
    ) {
        // Affiche les marqueurs pour les points d'intérêt existants
        uiState.pointsOfInterest.forEach { poi ->
        Marker(
            state = MarkerState(position = LatLng(poi.latitude, poi.longitude)),
            title = poi.name,
            snippet = poi.description
        )
        }
        // TODO: Ajouter l'affichage des marqueurs de localisation des membres du groupe
    }

    // Dialogue pour ajouter un POI
    if (uiState.showAddPoiDialog) {
        AddPointOfInterestDialog(
        onDismiss = { viewModel.dismissAddPoiDialog() },
        onConfirm = { name, description ->
            viewModel.addPointOfInterest(name, description)
        },
        isLoading = uiState.isAddingPoi,
        errorMessage = uiState.addPoiError
        )
    }

    // Affichage des messages d'erreur généraux de la carte
    uiState.errorMessage?.let {
        Snackbar { Text(it) } // Utilise un Snackbar pour les messages transitoires
    }
    }
}

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPointOfInterestDialog(
onDismiss: () -> Unit,
onConfirm: (name: String, description: String) -> Unit,
isLoading: Boolean,
errorMessage: String?
) {
var name by remember { mutableStateOf("") }
var description by remember { mutableStateOf("") }

AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Ajouter un Point d'Intérêt") },
    text = {
    Column {
        OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Nom du POI") },
        modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Description") },
        modifier = Modifier.fillMaxWidth()
        )
        errorMessage?.let {
        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        if (isLoading) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp))
        }
    }
    },
    confirmButton = {
    Button(
        onClick = { onConfirm(name, description) },
        enabled = name.isNotBlank() && !isLoading
    ) {
        Text("Ajouter")
    }
    },
    dismissButton = {
    TextButton(onClick = onDismiss) {
        Text("Annuler")
    }
    }
)

}

// @Preview est omis ici car GoogleMap requiert un contexte d'appareil.
// Pour des prévisualisations, il faudrait mocker l'environnement ou utiliser des captures d'écran.
```
