package com.glitchcraftlabs.qrstorage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface HistoryDao {

    @Insert
    suspend fun insert(history: History)

    @Query("SELECT * FROM History ORDER BY createdAt DESC")
    suspend fun getAllHistoryNewFirst(): List<History>

    @Query("SELECT * FROM History ORDER BY createdAt ASC")
    suspend fun getAllHistoryOldFirst(): List<History>

    @Query("SELECT * FROM History ORDER BY tag ASC")
    suspend fun getHistoryOrderByTag(): List<History>

    @Query("SELECT * FROM History WHERE tag LIKE '%' || :tag || '%'")
    suspend fun getHistoryByTag(tag: String): List<History>

    @Query("SELECT * FROM HISTORY WHERE NOT isFile ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getTextRecentWithLimit(limit: Int): List<History>

    @Query("SELECT * FROM HISTORY WHERE isFile ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getFileRecentWithLimit(limit: Int): List<History>

    @Query("UPDATE History SET tag = :newTag WHERE tag = :oldTag")
    suspend fun updateTag(newTag : String, oldTag:String)

}