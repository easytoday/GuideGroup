package com.easytoday.guidegroup.data.repository.impl

import com.easytoday.guidegroup.domain.model.PointOfInterest
import com.easytoday.guidegroup.domain.repository.PointOfInterestRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Implémentation factice de PointOfInterestRepository pour l'environnement de test (mock).
// Simule les opérations CRUD sur les points d'intérêt.
class FakePointOfInterestRepositoryImpl @Inject constructor() : PointOfInterestRepository {

    // Simule une base de données de points d'intérêt en mémoire
    private val fakePoisDb = MutableStateFlow<MutableMap<String, PointOfInterest>>(mutableMapOf())

    // Initialise avec quelques POI factices pour les tests
    init {
        val poi1 = PointOfInterest(
            id = "poi1_id",
            groupId = "group1_id", // Associé à un groupe factice
            name = "Tour Eiffel",
            description = "Un monument emblématique de Paris.",
            latitude = 48.8584,
            longitude = 2.2945
        )
        val poi2 = PointOfInterest(
            id = "poi2_id",
            groupId = "group1_id",
            name = "Musée du Louvre",
            description = "Célèbre musée d'art.",
            latitude = 48.8606,
            longitude = 2.3376
        )
        val poi3 = PointOfInterest(
            id = "poi3_id",
            groupId = "group2_id",
            name = "Cathédrale Notre-Dame",
            description = "Monument historique en rénovation.",
            latitude = 48.8529,
            longitude = 2.3499
        )
        fakePoisDb.value[poi1.id] = poi1
        fakePoisDb.value[poi2.id] = poi2
        fakePoisDb.value[poi3.id] = poi3
    }

    // Récupère un flux de points d'intérêt pour un groupe spécifique.
    override fun getGroupPointsOfInterest(groupId: String): Flow<List<PointOfInterest>> {
        return fakePoisDb.map { poisMap ->
            delay(500) // Simule un délai réseau
            // Filtre les POI en fonction du groupId
            poisMap.values.filter { it.groupId == groupId }
        }
    }

    // Ajoute un nouveau point d'intérêt.
    override suspend fun addPointOfInterest(poi: PointOfInterest): String {
        delay(300) // Simule un délai
        val newId = poi.id.ifEmpty { "fakePoiId_${System.currentTimeMillis()}" }
        val poiWithId = poi.copy(id = newId)
        fakePoisDb.value[poiWithId.id] = poiWithId
        return newId
    }

    // Met à jour un point d'intérêt existant.
    override suspend fun updatePointOfInterest(poi: PointOfInterest) {
        delay(300) // Simule un délai
        if (fakePoisDb.value.containsKey(poi.id)) {
            fakePoisDb.value[poi.id] = poi
        } else {
            // Dans un mock, lancer une exception ou ne rien faire.
            // rien pour simuler une opération sans erreur spécifique.
        }
    }

    // Supprime un point d'intérêt.
    override suspend fun deletePointOfInterest(poiId: String) {
        delay(300) // Simule un délai
        if (fakePoisDb.value.containsKey(poiId)) {
            fakePoisDb.value.remove(poiId)
        } else {
            // Similaire à update, pas d'erreur spécifique pour le mock.
        }
    }
}

