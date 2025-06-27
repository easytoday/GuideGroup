package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.Location
import com.easytoday.guidegroup.domain.repository.LocationRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import java.util.Date


// Implémentation factice de LocationRepository pour l'environnement de test (mock).
// Simule le stockage et la récupération des localisations des utilisateurs.
class FakeLocationRepositoryImpl @Inject constructor() : LocationRepository {

    // Simule une base de données de localisations en mémoire.
    // La clé est l'ID de l'utilisateur, la valeur est l'objet Location.
    private val fakeLocationsDb = MutableStateFlow<MutableMap<String, Location>>(mutableMapOf())

    // Initialise avec quelques localisations factices pour les tests.
    init {
        val loc1 = Location(
            userId = "user1_id",
            latitude = 48.8566, // Paris centre
            longitude = 2.3522,
            timestamp = Date(System.currentTimeMillis() - 10000)
        )
        val loc2 = Location(
            userId = "user2_id",
            latitude = 48.8600, // Près du Louvre
            longitude = 2.3370,
            timestamp = Date(System.currentTimeMillis() - 5000)
        )
        val loc3 = Location(
            userId = "user3_id",
            latitude = 48.8738, // Montmartre
            longitude = 2.2950,
            timestamp = Date(System.currentTimeMillis() - 20000)
        )
        fakeLocationsDb.value[loc1.userId] = loc1
        fakeLocationsDb.value[loc2.userId] = loc2
        fakeLocationsDb.value[loc3.userId] = loc3
    }

    /**
     * Récupère la localisation d'un utilisateur spécifique en tant que Flow.
     * Simule la récupération de la DB en mémoire.
     * @param userId L'ID de l'utilisateur.
     * @return Un Flow de [Location] ou null si non trouvée.
     */
    override fun getUserLocation(userId: String): Flow<Location?> {
        return fakeLocationsDb.map { db ->
            delay(300) // Simule un délai
            db[userId]
        }
    }

    /**
     * Met à jour la localisation d'un utilisateur. Si la localisation n'existe pas, elle est créée.
     * Simule l'ajout/mise à jour dans la DB en mémoire.
     * @param location L'objet Location à mettre à jour/ajouter.
     */
    override suspend fun updateLocation(location: Location) {
        delay(400) // Simule un délai
        fakeLocationsDb.value[location.userId] = location
        // Pour s'assurer que le MutableStateFlow émet une nouvelle valeur.
        // Sinon, les observateurs ne verraient pas les changements si seule la map interne était modifiée.
        fakeLocationsDb.value = fakeLocationsDb.value.toMutableMap()
    }

    /**
     * Récupère les localisations des membres d'un groupe spécifique.
     * Simule la récupération des localisations pour une liste d'IDs.
     *
     * @param groupId L'ID du groupe (non utilisé directement dans ce mock simple, car les locations sont par userId).
     * @param memberIds La liste des IDs des membres du groupe.
     * @return Un Flow d'une liste de [Location] pour les membres spécifiés.
     */
    override fun getMemberLocations(groupId: String, memberIds: List<String>): Flow<List<Location>> {
        return fakeLocationsDb.map { db ->
            delay(500) // Simule un délai réseau
            // Filtre les localisations pour ne conserver que celles des membres spécifiés.
            memberIds.mapNotNull { memberId -> db[memberId] }
        }
    }
}

