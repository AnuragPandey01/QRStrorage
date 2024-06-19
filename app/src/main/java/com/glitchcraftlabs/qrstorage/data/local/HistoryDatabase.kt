package com.glitchcraftlabs.qrstorage.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [History::class], version = 1)
abstract class HistoryDatabase : RoomDatabase(){
    abstract fun historyDao(): HistoryDao
}