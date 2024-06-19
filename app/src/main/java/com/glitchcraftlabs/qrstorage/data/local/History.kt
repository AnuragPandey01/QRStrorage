package com.glitchcraftlabs.qrstorage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class History(
    @PrimaryKey(autoGenerate = true)
    val id:Long? = null,

    val tag: String,

    val createdAt: Long,

    val data: String,

    val isGenerated: Boolean
)