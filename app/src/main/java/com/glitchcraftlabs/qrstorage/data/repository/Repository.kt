package com.glitchcraftlabs.qrstorage.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.glitchcraftlabs.qrstorage.data.local.History
import com.glitchcraftlabs.qrstorage.data.local.HistoryDao
import com.glitchcraftlabs.qrstorage.util.QueryResult


class Repository(
    private val historyDao: HistoryDao
) {

    private val _history = MutableLiveData<QueryResult<List<History>>>(QueryResult.Loading())
    val history: LiveData<QueryResult<List<History>>> = _history

    private val _insertStatus = MutableLiveData<QueryResult<Long>>(QueryResult.Loading())
    val insertStatus: LiveData<QueryResult<Long>> = _insertStatus

    suspend fun insertHistory(
        tag: String,
        value : String,
        isGenerated: Boolean
    ): LiveData<QueryResult<Long>>  {
        val insertLiveData = MutableLiveData<QueryResult<Long>>(QueryResult.Loading())
        try{
            historyDao.insert(
                History(
                    tag = tag,
                    data = value,
                    isGenerated = isGenerated,
                    createdAt = System.currentTimeMillis()
                )
            ).also {
                insertLiveData.postValue(QueryResult.Success(it))
            }
        }catch (e: SQLiteConstraintException){
            insertLiveData.postValue(QueryResult.Error("QR with same tag already exist"))
        }catch (e: Exception){
            insertLiveData.postValue(QueryResult.Error("Error inserting data"))
        }
        return insertLiveData
    }

    suspend fun getAllHistory(newFirst: Boolean = true){
        _history.postValue(QueryResult.Loading())
        try{
            if (newFirst) {
                _history.postValue(QueryResult.Success(historyDao.getAllHistoryNewFirst()))
            } else {
                _history.postValue(QueryResult.Success(historyDao.getAllHistoryOldFirst()))
            }
        }catch (e: Exception){
            _history.postValue(QueryResult.Error("Error fetching data"))
        }
    }

    suspend fun getAllHistoryOrderByTag(){
        _history.postValue(QueryResult.Loading())
        try{
            _history.postValue(QueryResult.Success(historyDao.getHistoryOrderByTag()))
        }catch (e: Exception){
            _history.postValue(QueryResult.Error("Error fetching data"))
        }
    }

    suspend fun searchByTag(tag: String){
        _history.postValue(QueryResult.Loading())
        try{
            _history.postValue(QueryResult.Success(historyDao.getHistoryByTag(tag)))
        }catch (e: Exception){
            _history.postValue(QueryResult.Error("Error fetching data"))
        }
    }

    suspend fun getRecentWithLimit(limit: Int){
        _history.postValue(QueryResult.Loading())
        try{
            _history.postValue(QueryResult.Success(historyDao.getRecentWithLimit(limit)))
        }catch (e: Exception){
            _history.postValue(QueryResult.Error("Error fetching data"))
        }
    }

    suspend fun updateTag(
        newTag: String,
        oldTag: String
    ){
        try{
            historyDao.updateTag(newTag, oldTag)
        }catch (_: Exception){

        }
    }
}