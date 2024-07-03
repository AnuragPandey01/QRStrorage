package com.glitchcraftlabs.qrstorage.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.glitchcraftlabs.qrstorage.data.local.History
import com.glitchcraftlabs.qrstorage.data.local.HistoryDao
import com.glitchcraftlabs.qrstorage.util.QueryResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await


class Repository(
    private val historyDao: HistoryDao,
    private val firebaseStorage: FirebaseStorage
) {

    private val storageRef = firebaseStorage.reference

    private val _history = MutableLiveData<QueryResult<List<History>>>(QueryResult.Loading())
    val history: LiveData<QueryResult<List<History>>> = _history

    suspend fun insertHistory(
        tag: String,
        value : String,
        isGenerated: Boolean,
        isFile: Boolean
    ): LiveData<QueryResult<Nothing?>>  {
        val insertLiveData = MutableLiveData<QueryResult<Nothing?>>(QueryResult.Loading())
        try{
            historyDao.insert(
                History(
                    tag = tag,
                    data = value,
                    isGenerated = isGenerated,
                    createdAt = System.currentTimeMillis(),
                    isFile = isFile
                )
            ).also {
                insertLiveData.postValue(QueryResult.Success(null))
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

    suspend fun getRecentWithLimit(limit: Int, isFile: Boolean){
        _history.postValue(QueryResult.Loading())
        try{
            if (isFile) _history.postValue(QueryResult.Success(historyDao.getFileRecentWithLimit(limit)))
            else _history.postValue(QueryResult.Success(historyDao.getTextRecentWithLimit(limit)))
        }catch (e: Exception){
            _history.postValue(QueryResult.Error("Error fetching data"))
        }
    }

    suspend fun updateTag(
        newTag: String,
        oldTag: String
    ): LiveData<QueryResult<Nothing?>>{
        val liveData = MutableLiveData<QueryResult<Nothing?>>(QueryResult.Loading())
        try{
            historyDao.updateTag(newTag, oldTag)
            liveData.postValue(QueryResult.Success(null))
        }catch (e: SQLiteConstraintException){
            liveData.postValue(QueryResult.Error("QR with same tag already exist"))
        }
        return liveData
    }

    suspend fun uploadFile(
        tag:String,
        fileUri: Uri,
        user: FirebaseUser
    ): LiveData<QueryResult<Uri>>{
        val liveData = MutableLiveData<QueryResult<Uri>>(QueryResult.Loading())
        try{
            val userStorageRef = storageRef.child(user.uid)
            val fileRef = userStorageRef.child(tag)
            fileRef.putFile(fileUri).await()
            val downloadUrl = fileRef.downloadUrl.await()
            liveData.postValue(QueryResult.Success(downloadUrl))
        }catch (e: Exception){
            liveData.postValue(QueryResult.Error("Error inserting data"))
        }
        return liveData
    }
}