package com.easytoday.guidegroup.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Données à partager
data class PoiToShare(
    val poiId: String,
    val poiName: String,
    val groupId: String
)

// Le Repository lui-même sert à partager des données entre les écrans ex: MapScreen et chatScreen
@Singleton
class SharedDataRepository @Inject constructor() {
    private val _poiToShare = MutableStateFlow<PoiToShare?>(null)
    val poiToShare: StateFlow<PoiToShare?> = _poiToShare

    fun setPoiToShare(poi: PoiToShare?) {
        _poiToShare.value = poi
    }

    // Fonction pour consommer le POI et le supprimer
    fun consumePoiToShare(): PoiToShare? {
        val poi = _poiToShare.value
        _poiToShare.value = null // Remet à zéro après consommation
        return poi
    }
}