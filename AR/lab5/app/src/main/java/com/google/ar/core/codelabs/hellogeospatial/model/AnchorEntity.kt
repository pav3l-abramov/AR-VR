package com.google.ar.core.codelabs.hellogeospatial.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anchor_table")
data class AnchorEntity(
    @PrimaryKey(autoGenerate = true)
    var anchorId :Int?=null,
    @ColumnInfo(name = "latitude")
    var latitude: Double,
    @ColumnInfo(name = "longitude")
    var longitude: Double,
    @ColumnInfo(name = "altitude")
    var altitude: Double,
    @ColumnInfo(name = "Qx")
    var qx: Float,
    @ColumnInfo(name = "Qy")
    var qy: Float,
    @ColumnInfo(name = "Qz")
    var qz: Float,
    @ColumnInfo(name = "Qw")
    var qw: Float

)

