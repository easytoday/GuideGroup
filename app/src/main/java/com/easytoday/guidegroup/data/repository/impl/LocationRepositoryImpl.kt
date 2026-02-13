package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.data.firestore.FirestoreHelper
import com.easytoday.guidegroup.domain.model.Location
import com.easytoday.guidegroup.domain.repository.LocationRepository
//import com.google.firebase.firestore.FirebaseFirestore //on passe par FirestoreHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf // import pour flowOf
import timber.log.Timber
import javax.inject.Inject

/**
 * Implémentation concrète de [LocationRepository] utilisant Firestore.
 * Gère le stockage et la récupération des localisations des utilisateurs.
 */
class LocationRepositoryImpl @Inject constructor(
    private val firestoreHelper: FirestoreHelper
    //private val firestore: FirebaseFirestore // Injecter Firestore pour les requêtes complexes
) : LocationRepository {

    private val LOCATIONS_COLLECTION = "locations"

    /**
     * Récupère la localisation d'un utilisateur spécifique en tant que Flow.
     * @param userId L'ID de l'utilisateur.
     * @return Un Flow de [Location] ou null si non trouvée.
     */
    override fun getUserLocation(userId: String): Flow<Location?> {
        // MODIFICATION : type générique <Location> explicite
        return firestoreHelper.getDocumentAsFlow<Location>(LOCATIONS_COLLECTION, userId)
            .catch { e ->
                Timber.e("Error getting user location for $userId: ${e.message}", e)
                emit(null)
            }
    }

    /**
     * Met à jour la localisation d'un utilisateur. Si la localisation n'existe pas, elle est créée.
     * @param location L'objet Location à mettre à jour/ajouter.
     */
    override suspend fun updateLocation(location: Location) {
        try {
            // firestoreHelper.addDocument n'a pas besoin de spécification générique si le type du paramètre est clair
            firestoreHelper.addDocument(LOCATIONS_COLLECTION, location, location.userId)
            Timber.d("Location updated for user ${location.userId}")
        } catch (e: Exception) {
            Timber.e("Error updating location for user ${location.userId}: ${e.message}", e)
            // L'erreur est loggée, pas propagée directement ici selon le comportement actuel.
        }
    }

    /**
     * Récupère les localisations des membres d'un groupe spécifique.
     *
     * @param groupId L'ID du groupe.
     * @param memberIds La liste des IDs des membres du groupe.
     * @return Un Flow d'une liste de [Location] pour les membres spécifiés.
     */
    override fun getMemberLocations(groupId: String, memberIds: List<String>): Flow<List<Location>> {
        // Si memberIds est vide, nous n'avons pas de membres à suivre.
        if (memberIds.isEmpty()) {
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }

        // MODIFICATION : type générique <Location> explicite
        return firestoreHelper.getCollectionAsFlow<Location>(
            firestoreHelper.db.collection(LOCATIONS_COLLECTION).whereIn("userId", memberIds)
        ).map { locations ->
            locations.filter { it.userId in memberIds } // Double vérification
        }.catch { e ->
            Timber.e("Error getting member locations for group $groupId: ${e.message}", e)
            emit(emptyList())
        }
    }
}

