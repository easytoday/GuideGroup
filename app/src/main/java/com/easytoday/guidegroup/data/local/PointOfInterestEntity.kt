package com.easytoday.guidegroup.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "points_of_interest")
data class PointOfInterestEntity(
    @PrimaryKey
    val id: String,
    val groupId: String,
    // NOUVEAU : Ajout du champ pour l'ID du créateur
    val creatorId: String,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val type: String
)