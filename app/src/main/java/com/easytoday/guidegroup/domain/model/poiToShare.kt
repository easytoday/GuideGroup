package com.easytoday.guidegroup.domain.model

// CORRECTION : La data class a maintenant son propre fichier dans le bon package.
data class PoiToShare(
    val poiId: String,
    val poiName: String,
    val groupId: String
)