package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Result
import com.easytoday.guidegroup.domain.model.User
import com.easytoday.guidegroup.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper
) : AuthRepository {

    private val USERS_COLLECTION = "users"

    override suspend fun signUp(email: String, password: String, username: String, isGuide: Boolean): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val authResult = firestoreHelper.auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user!!
            val newUser = User(id = firebaseUser.uid, email = email, username = username, isGuide = isGuide)
            firestoreHelper.addDocument(USERS_COLLECTION, newUser, firebaseUser.uid)
            emit(Result.Success(newUser))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown sign-up error", e))
        }
    }

    override suspend fun signIn(email: String, password: String): Flow<Result<User>> = flow {
        emit(Result.Loading)
        try {
            val authResult = firestoreHelper.auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user!!
            val user = firestoreHelper.getDocumentAsFlow<User>(USERS_COLLECTION, firebaseUser.uid).firstOrNull()
            if (user != null) {
                emit(Result.Success(user))
            } else {
                firestoreHelper.auth.signOut()
                emit(Result.Error("User data not found in Firestore.", Exception("User data missing")))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown sign-in error", e))
        }
    }

    override suspend fun signOut(): Flow<Result<Unit>> = flow {
        emit(Result.Loading)
        try {
            firestoreHelper.auth.signOut()
            emit(Result.Success(Unit))
        } catch (e: Exception) {
            emit(Result.Error(e.message ?: "Unknown sign-out error", e))
        }
    }

    override fun getCurrentUser(): Flow<Result<User?>> {
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { auth ->
                trySend(auth.currentUser?.uid)
            }
            firestoreHelper.auth.addAuthStateListener(listener)
            awaitClose { firestoreHelper.auth.removeAuthStateListener(listener) }
        }.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(Result.Success(null))
            } else {
                firestoreHelper.getDocumentAsFlow<User>(USERS_COLLECTION, userId)
                    .map { user ->
                        // La correction est ici : on spécifie explicitement le type de retour général.
                        Result.Success(user) as Result<User?>
                    }
                    .catch { e ->
                        emit(Result.Error("Failed to fetch user data.", e))
                    }
            }
        }
    }

    override fun getCurrentUserId(): Flow<Result<String?>> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(Result.Success(auth.currentUser?.uid))
        }
        firestoreHelper.auth.addAuthStateListener(listener)
        awaitClose { firestoreHelper.auth.removeAuthStateListener(listener) }
    }
}