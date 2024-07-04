package com.glitchcraftlabs.qrstorage.di

import com.glitchcraftlabs.qrstorage.data.repository.AuthRepository
import com.glitchcraftlabs.qrstorage.data.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {

    @Singleton
    @Provides
    fun provideRepository(
        firebaseStorage: FirebaseStorage,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ): Repository {
        return Repository(firebaseStorage, auth, firestore)
    }

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepository(firebaseAuth)
    }

    @Provides fun provideStorage() = Firebase.storage

    @Provides fun provideFireStore() = Firebase.firestore
}