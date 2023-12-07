package com.google.ar.core.codelabs.hellogeospatial.model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnchorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(anchor: AnchorEntity)
    @Delete
     fun delete(anchor: AnchorEntity)

    @Query("SELECT  * from anchor_table")
    fun getAllAnchor(): Flow <List<AnchorEntity>>
    @Query("SELECT * FROM anchor_table ORDER BY anchorId DESC LIMIT 5")
    fun get5LastAnchor(): Flow <List<AnchorEntity>>
    @Query("SELECT COUNT(*) FROM anchor_table")
    fun getCount(): Int
    // selecting one note at a time
    @Query("SELECT * FROM anchor_table WHERE anchorId LIKE :id")
    fun getAnchor(id : Int) : AnchorEntity


}



