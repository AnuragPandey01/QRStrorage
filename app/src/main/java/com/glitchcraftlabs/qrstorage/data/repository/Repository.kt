package com.glitchcraftlabs.qrstorage.data.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.glitchcraftlabs.qrstorage.data.local.History
import com.glitchcraftlabs.qrstorage.util.QueryResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await


class Repository(
    private val firebaseStorage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) {

    private val storageRef = firebaseStorage.reference

    private val _history = MutableLiveData<QueryResult<List<History>>>(QueryResult.Loading())
    val history: LiveData<QueryResult<List<History>>> = _history

    private fun getUserNotesCollection() =
        firestore.collection("users").document(auth.uid!!).collection("qr_history")

    suspend fun insertHistory(
        tag: String,
        value: String,
        isGenerated: Boolean,
        isFile: Boolean
    ): LiveData<QueryResult<Nothing?>> {
        val insertLiveData = MutableLiveData<QueryResult<Nothing?>>(QueryResult.Loading())
        try {
            val document = getUserNotesCollection().document(tag)
            val snapshot = document.get().await()
            if (snapshot.exists()) {
                insertLiveData.postValue(QueryResult.Error("QR with same tag already exist"))
                return insertLiveData
            }
            val history = History(tag, System.currentTimeMillis(), value, isGenerated, isFile)
            document.set(history).await()
            insertLiveData.postValue(QueryResult.Success(null))
        } catch (e: Exception) {
            insertLiveData.postValue(QueryResult.Error(e.message))
        }
        return insertLiveData
    }

    suspend fun getAllHistory(sortOder: Query.Direction) {
        _history.postValue(QueryResult.Loading())
        try {
            _history.postValue(
                QueryResult.Success(
                    getUserNotesCollection().orderBy(
                        "createdAt",
                        sortOder
                    ).get().await().toObjects(History::class.java)
                )
            )
        } catch (e: Exception) {
            _history.postValue(QueryResult.Error(e.message))
        }
    }

    suspend fun getAllHistoryOrderByTag() {
        _history.postValue(QueryResult.Loading())
        try {
            _history.postValue(
                QueryResult.Success(
                    getUserNotesCollection().orderBy(
                        "tag",
                        Query.Direction.ASCENDING
                    ).get().await().toObjects(History::class.java)
                )
            )
        } catch (e: Exception) {
            _history.postValue(QueryResult.Error(e.message))
        }
    }

    suspend fun searchByTag(tag: String) {
        _history.postValue(QueryResult.Loading())
        try {
            _history.postValue(
                QueryResult.Success(
                    getUserNotesCollection().whereEqualTo("tag", tag).get().await()
                        .toObjects(History::class.java)
                )
            )
        } catch (e: Exception) {
            _history.postValue(QueryResult.Error("Error fetching data"))
        }
    }

    suspend fun getRecentWithLimit(limit: Int, isFile: Boolean) {
        _history.postValue(QueryResult.Loading())
        try {
            _history.postValue(QueryResult.Success(
                getUserNotesCollection().whereEqualTo("file", isFile)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .limit(limit.toLong()).get().await().toObjects(History::class.java)
            ) )
        } catch (e: Exception) {
            _history.postValue(QueryResult.Error(e.message))
        }
    }

    suspend fun updateTag(
        newTag: String,
        oldTag: String
    ): LiveData<QueryResult<Nothing?>> {
        val liveData = MutableLiveData<QueryResult<Nothing?>>(QueryResult.Loading())
        try {
            val document = getUserNotesCollection().document(oldTag)
            val snapshot = document.get().await()
            if (!snapshot.exists()) {
                liveData.postValue(QueryResult.Error("QR with tag $oldTag does not exist"))
                return liveData
            }
            val history = snapshot.toObject(History::class.java)!!
            getUserNotesCollection().document(newTag).set(history.copy(tag = newTag)).await()
            document.delete().await()
            liveData.postValue(QueryResult.Success(null))
        } catch (e: Exception) {
            Log.d("googgogo", "updateTag:${e.message} ")
            liveData.postValue(QueryResult.Error("check connection"))
        }
        return liveData
    }

    suspend fun uploadFile(
        tag: String,
        fileUri: Uri,
        user: FirebaseUser
    ): LiveData<QueryResult<Uri>> {
        val liveData = MutableLiveData<QueryResult<Uri>>(QueryResult.Loading())
        try {
            val userStorageRef = storageRef.child(user.uid)
            val fileRef = userStorageRef.child(tag)
            fileRef.putFile(fileUri).await()
            val downloadUrl = fileRef.downloadUrl.await()
            liveData.postValue(QueryResult.Success(downloadUrl))
        } catch (e: Exception) {
            liveData.postValue(QueryResult.Error("Error inserting data"))
        }
        return liveData
    }
}