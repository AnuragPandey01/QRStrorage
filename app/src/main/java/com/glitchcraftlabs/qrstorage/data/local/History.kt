package com.glitchcraftlabs.qrstorage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity()
data class History(
    @PrimaryKey
    val tag: String,

    val createdAt: Long,

    val data: String,

    val isGenerated: Boolean,

    val isFile: Boolean
)