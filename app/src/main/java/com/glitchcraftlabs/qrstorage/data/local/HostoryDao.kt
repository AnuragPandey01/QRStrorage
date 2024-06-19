package com.glitchcraftlabs.qrstorage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {

    @Insert
    suspend fun insert(history: History) : Long

    @Query("SELECT * FROM History ORDER BY id DESC")
    suspend fun getAllHistoryNewFirst(): List<History>

    @Query("SELECT * FROM History ORDER BY id ASC")
    suspend fun getAllHistoryOldFirst(): List<History>


    @Query("SELECT * FROM History WHERE id = :id")
    suspend fun getHistoryByID(id: Long): History

}