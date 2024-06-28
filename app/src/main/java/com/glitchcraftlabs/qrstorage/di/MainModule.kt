package com.glitchcraftlabs.qrstorage.di

import android.content.Context
import androidx.room.Room
import com.glitchcraftlabs.qrstorage.data.local.HistoryDao
import com.glitchcraftlabs.qrstorage.data.local.HistoryDatabase
import com.glitchcraftlabs.qrstorage.data.repository.AuthRepository
import com.glitchcraftlabs.qrstorage.data.repository.Repository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {

    @Singleton
    @Provides
    fun provideHistoryDao(@ApplicationContext context: Context): HistoryDao {
        return Room.databaseBuilder(context, HistoryDatabase::class.java, "history.db")
            .build()
            .historyDao()
    }

    @Singleton
    @Provides
    fun provideRepository(historyDao: HistoryDao, firebaseStorage: FirebaseStorage): Repository {
        return Repository(historyDao, firebaseStorage)
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
}