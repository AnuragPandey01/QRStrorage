package com.glitchcraftlabs.qrstorage.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.glitchcraftlabs.qrstorage.util.QueryResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class AuthRepository(
    private val firebaseAuth: FirebaseAuth
) {

    fun signUp(email: String, password: String): LiveData<QueryResult<FirebaseUser>> {
        val signUpLiveData = MutableLiveData<QueryResult<FirebaseUser>>(QueryResult.Loading())
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                signUpLiveData.postValue(QueryResult.Success(it.user!!))
            }.addOnFailureListener {
                signUpLiveData.postValue(QueryResult.Error(it.message))
            }
        return signUpLiveData
    }

    fun signIn(email: String, password: String): LiveData<QueryResult<FirebaseUser>> {
        val signInLiveData = MutableLiveData<QueryResult<FirebaseUser>>(QueryResult.Loading())
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                signInLiveData.postValue(QueryResult.Success(it.user!!))
            }.addOnFailureListener {
                signInLiveData.postValue(QueryResult.Error(it.message))
            }
        return signInLiveData
    }

    fun authWithGoogle(idToken: String): LiveData<QueryResult<FirebaseUser>> {
        val authWithGoogleLiveData = MutableLiveData<QueryResult<FirebaseUser>>(QueryResult.Loading())
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                authWithGoogleLiveData.postValue(QueryResult.Success(it.user!!))
            }.addOnFailureListener {
                authWithGoogleLiveData.postValue(QueryResult.Error(it.message))
            }
        return authWithGoogleLiveData
    }

    fun logout(){
        firebaseAuth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
}