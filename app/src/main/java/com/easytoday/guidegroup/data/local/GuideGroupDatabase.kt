package com.easytoday.guidegroup.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PointOfInterestEntity::class],
    version = 1,
    exportSchema = false // On peut mettre à true plus tard si on veut gérer des migrations
)
abstract class GuideGroupDatabase : RoomDatabase() {
    abstract fun pointOfInterestDao(): PointOfInterestDao
}