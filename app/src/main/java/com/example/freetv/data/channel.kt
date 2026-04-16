package com.example.freetv.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val categoria: String = "General",
    val logoUrl: String,
    val streamUrl: String,
    val descripcion: String = "Sin descripción disponible para este canal.",
    val isFavorite: Boolean = false,
    val lastWatched: Long = 0
)
