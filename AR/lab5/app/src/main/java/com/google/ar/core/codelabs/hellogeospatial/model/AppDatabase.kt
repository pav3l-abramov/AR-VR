package com.google.ar.core.codelabs.hellogeospatial.model
/*
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AnchorEntity::class], version = 1, exportSchema = false)
abstract class AnchorDatabase : RoomDatabase() {

    abstract fun getAnchorDao(): AnchorDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: AnchorDatabase? = null

        fun getDatabase(context: Context): AnchorDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnchorDatabase::class.java,
                    "anchor_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}

 */