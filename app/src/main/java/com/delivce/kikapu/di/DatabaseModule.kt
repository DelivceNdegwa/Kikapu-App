package com.delivce.kikapu.di

import android.content.Context
import androidx.room.Room
import com.delivce.kikapu.data.local.KikapuDatabase
import com.delivce.kikapu.data.local.dao.ItemDao
import com.delivce.kikapu.data.local.dao.TripDao
import com.delivce.kikapu.data.repository.ItemRepositoryImpl
import com.delivce.kikapu.data.repository.TripRepositoryImpl
import com.delivce.kikapu.domain.repository.ItemRepository
import com.delivce.kikapu.domain.repository.TripRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideKikapuDatabase(@ApplicationContext context: Context): KikapuDatabase =
        Room.databaseBuilder(context, KikapuDatabase::class.java, "kikapu_db")
            // Pre-release app, no shipped users yet — simplest safe path across schema bumps.
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideTripDao(database: KikapuDatabase): TripDao = database.tripDao()

    @Provides
    @Singleton
    fun provideItemDao(database: KikapuDatabase): ItemDao = database.itemDao()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class TripRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTripRepository(impl: TripRepositoryImpl): TripRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository
}
