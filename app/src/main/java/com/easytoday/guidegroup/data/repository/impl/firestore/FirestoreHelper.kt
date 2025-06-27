// app/src/main/java/com/easytoday/guidegroup/data/firestore/FirestoreHelper.kt
package com.easytoday.guidegroup.data.firestore

import com.easytoday.guidegroup.domain.model.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow // <<-- MODIFIÉ
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * FirestoreHelper est un point d'entré unique pour les opérations CRUD et ayhentification
 * Classe d'aide générique pour les opérations Firestore et d'authentification.
 * Fournit des méthodes pour interagir avec les collections et documents Firestore
 * et convertir les instantanés en flux Kotlin.
 */
class FirestoreHelper @Inject constructor(
    internal val db: FirebaseFirestore, // <<-- MODIFIÉ : Rendu internal pour les fonctions inline
    internal val auth: FirebaseAuth  // NOUVEAU changement d'architecture, (n'implique pas de changement dans le code de FirestoreHelper)
) { //cela implique de modifier tous les constructeurs des fichiers *RespositoryImpl
    /**
     * Récupère un document en temps réel comme un Flow de l'objet ou null.
     */
    internal inline fun <reified T : Any> getDocumentAsFlow(collectionPath: String, documentId: String): Flow<T?> = callbackFlow {
        val docRef = db.collection(collectionPath).document(documentId)
        val registration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Timber.e(e, "Listen failed for document $documentId")
                close(e) // Ferme le flow en cas d'erreur
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.toObject(T::class.java)
                Timber.d("Document $documentId data: $data")
                trySend(data).isSuccess // Envoie les données au flow
            } else {
                Timber.d("Document $documentId does not exist or is null.")
                trySend(null).isSuccess // Envoie null si le document n'existe pas
            }
        }
        awaitClose {
            Timber.d("Stopping snapshot listener for document $documentId")
            registration.remove() // Retire le listener quand le flow est annulé
        }
    }.flowOn(Dispatchers.IO) // Exécuter la logique de Firestore sur un dispatcher IO

    /**
     * Récupère un document en temps réel comme un Flow de Result<T?>.
     * Utile pour propager les états de chargement/erreur.
     */
    internal inline fun <reified T : Any> getDocumentAsFlowWithResult(collectionPath: String, documentId: String): Flow<Result<T?>> = callbackFlow {
        val docRef = db.collection(collectionPath).document(documentId)
        val registration = docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Timber.e(e, "Listen failed for document $documentId")
                trySend(Result.Error("Failed to fetch document: ${e.message}", e)).isSuccess
                close(e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    val item = snapshot.toObject(T::class.java)
                    trySend(Result.Success(item)).isSuccess
                } catch (e: Exception) {
                    Timber.e(e, "Error converting document to object: $e")
                    trySend(Result.Error("Error processing document data.", e)).isSuccess
                }
            } else {
                trySend(Result.Success(null)).isSuccess // Document not found, but it's not an error.
            }
        }
        awaitClose {
            Timber.d("Stopping snapshot listener for document $documentId")
            registration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Récupère une collection en temps réel comme un Flow de liste.
     */
    internal inline fun <reified T : Any> getCollectionAsFlow(collectionPath: String): Flow<List<T>> = callbackFlow {
        val collectionRef = db.collection(collectionPath)
        val registration = collectionRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Timber.e(e, "Listen failed for collection $collectionPath")
                close(e)
                return@addSnapshotListener
            }

            if (snapshot != null) { // snapshot peut être vide mais non null
                val data = snapshot.documents.mapNotNull { it.toObject(T::class.java) }
                Timber.d("Collection $collectionPath data: ${data.size} items")
                trySend(data).isSuccess
            } else {
                Timber.d("Collection $collectionPath is null.")
                trySend(emptyList()).isSuccess
            }
        }
        awaitClose {
            Timber.d("Stopping snapshot listener for collection $collectionPath")
            registration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Récupère une collection en temps réel avec une requête personnalisée comme un Flow de liste.
     */
    internal inline fun <reified T : Any> getCollectionAsFlow(collectionPath: String, crossinline queryBuilder: (CollectionReference) -> Query): Flow<List<T>> = callbackFlow {
        val query = queryBuilder(db.collection(collectionPath))
        val registration = query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Timber.e(e, "Listen failed for custom query on $collectionPath")
                close(e)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val items = snapshots.documents.mapNotNull { it.toObject(T::class.java) }
                Timber.d("Custom query on $collectionPath data: ${items.size} items")
                trySend(items).isSuccess
            } else {
                Timber.d("Custom query on $collectionPath is null.")
                trySend(emptyList()).isSuccess
            }
        }
        awaitClose {
            Timber.d("Stopping snapshot listener for custom query on $collectionPath")
            registration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Récupère une collection en temps réel en utilisant une Query Firestore directe.
     */
    internal inline fun <reified T : Any> getCollectionAsFlow(query: Query): Flow<List<T>> = callbackFlow {
        val registration = query.addSnapshotListener { snapshots, e ->
            if (e != null) {
                Timber.e(e, "Listen failed for direct query.")
                close(e)
                return@addSnapshotListener
            }

            if (snapshots != null) {
                val items = snapshots.documents.mapNotNull { it.toObject(T::class.java) }
                Timber.d("Direct query data: ${items.size} items")
                trySend(items).isSuccess
            } else {
                Timber.d("Direct query is null.")
                trySend(emptyList()).isSuccess
            }
        }
        awaitClose {
            Timber.d("Stopping snapshot listener for direct query")
            registration.remove()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun <T : Any> addDocument(collectionPath: String, data: T, documentId: String? = null) {
        try {
            val docRef: DocumentReference = if (documentId != null) {
                db.collection(collectionPath).document(documentId)
            } else {
                db.collection(collectionPath).document()
            }
            docRef.set(data).await()
            Timber.d("Document added to '$collectionPath' with ID: ${docRef.id}")
        } catch (e: Exception) {
            Timber.e(e, "Error adding document to $collectionPath")
            throw e
        }
    }

    suspend fun <T : Any> updateDocument(collectionPath: String, documentId: String, data: T) {
        try {
            db.collection(collectionPath).document(documentId).set(data).await()
            Timber.d("Document with ID '$documentId' updated in '$collectionPath'")
        } catch (e: Exception) {
            Timber.e(e, "Error updating document $documentId in $collectionPath")
            throw e
        }
    }

    suspend fun updateDocumentFields(collectionPath: String, documentId: String, updates: Map<String, Any>) {
        try {
            db.collection(collectionPath).document(documentId).update(updates).await()
            Timber.d("Document fields for ID '$documentId' updated in '$collectionPath'")
        } catch (e: Exception) {
            Timber.e(e, "Error updating document fields for $documentId in $collectionPath")
            throw e
        }
    }

    suspend fun deleteDocument(collectionPath: String, documentId: String) {
        try {
            db.collection(collectionPath).document(documentId).delete().await()
            Timber.d("Document with ID '$documentId' deleted from '$collectionPath'")
        } catch (e: Exception) {
            Timber.e(e, "Error deleting document $documentId from $collectionPath")
            throw e
        }
    }

    fun getCollection(collectionPath: String): CollectionReference {
        return db.collection(collectionPath)
    }
}

