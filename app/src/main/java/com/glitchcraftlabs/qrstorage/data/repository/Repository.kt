package com.glitchcraftlabs.qrstorage.data.repository

import com.glitchcraftlabs.qrstorage.data.local.History
import com.glitchcraftlabs.qrstorage.data.local.HistoryDao


class Repository(
    private val historyDao: HistoryDao
) {

    suspend fun insertHistory(
        tag: String,
        value : String,
        isGenerated: Boolean
    ): Long  {
        return historyDao.insert(
            History(
                tag = tag,
                data = value,
                isGenerated = isGenerated,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun getAllHistory(newFirst: Boolean = true): List<History> {
        return if(newFirst) {
            historyDao.getAllHistoryNewFirst()
        } else {
            historyDao.getAllHistoryOldFirst()
        }
    }

    suspend fun getHistoryByID(id: Long): History {
        return historyDao.getHistoryByID(id)
    }
}