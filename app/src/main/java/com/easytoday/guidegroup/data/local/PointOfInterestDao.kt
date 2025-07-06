package com.easytoday.guidegroup.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PointOfInterestDao {

    // "Upsert" est une opération qui fait un INSERT si l'objet est nouveau,
    // ou un UPDATE s'il existe déjà (basé sur la PrimaryKey). C'est parfait pour la synchronisation.
    @Upsert
    suspend fun upsertAll(pois: List<PointOfInterestEntity>)

    // Récupère tous les POI pour un groupe spécifique sous forme de Flow.
    // L'UI se mettra à jour automatiquement si les données changent dans la base.
    @Query("SELECT * FROM points_of_interest WHERE groupId = :groupId")
    fun getPoisForGroup(groupId: String): Flow<List<PointOfInterestEntity>>

    // Vide la table des POI pour un groupe donné avant une nouvelle synchronisation.
    @Query("DELETE FROM points_of_interest WHERE groupId = :groupId")
    suspend fun deleteAllForGroup(groupId: String)
}