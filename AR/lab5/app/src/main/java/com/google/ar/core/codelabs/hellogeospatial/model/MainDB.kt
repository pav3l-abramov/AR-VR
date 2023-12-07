package com.google.ar.core.codelabs.hellogeospatial.model

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.google.ar.core.codelabs.hellogeospatial.HelloGeoRenderer

@Database(entities = [AnchorEntity::class], version = 3)
abstract class MainDB : RoomDatabase() {
    abstract fun getDao(): AnchorDao
    companion object {
        fun getDb(context: HelloGeoRenderer): MainDB {
            return Room.databaseBuilder(
                context.activity,
                MainDB::class.java,
                "anchor_table.db"
            ).build()

        }
    }
}