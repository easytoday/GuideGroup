package com.easytoday.guidegroup.domain.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Un simple repository singleton pour partager l'état du service de suivi
 * à travers l'application.
 */
@Singleton
class TrackingStateRepository @Inject constructor() {

    private val _isTracking = MutableStateFlow(false)
    val isTracking = _isTracking.asStateFlow()

    fun setTrackingState(isTracking: Boolean) {
        _isTracking.value = isTracking
    }
}