package com.easytoday.guidegroup.domain.repository

// CORRECTION : On importe la classe qui sera déplacée
import com.easytoday.guidegroup.domain.model.PoiToShare
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

// La data class a été déplacée dans son propre fichier.

@Singleton
class SharedDataRepository @Inject constructor() {
    private val _poiToShare = MutableStateFlow<PoiToShare?>(null)
    val poiToShare: StateFlow<PoiToShare?> = _poiToShare

    fun setPoiToShare(poi: PoiToShare?) {
        _poiToShare.value = poi
    }

    fun consumePoiToShare(): PoiToShare? {
        val poi = _poiToShare.value
        _poiToShare.value = null
        return poi
    }
}