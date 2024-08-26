package com.albertkingdom.mybusmap.di

import com.albertkingdom.mybusmap.data.FavDataSource
import com.albertkingdom.mybusmap.data.FirebaseFavDataSource
import com.albertkingdom.mybusmap.repository.FavoriteRepository
import com.albertkingdom.mybusmap.repository.FavoriteRepositoryInterface
import com.albertkingdom.mybusmap.util.RealmManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideRealm(): RealmManager = RealmManager()

    @Provides
    @Singleton
    fun provideFirebaseDataSource(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): FavDataSource {
        return FirebaseFavDataSource(firestore, firebaseAuth)
    }



    @Provides
    fun provideFavRepository(
        firebaseFavDataSource: FirebaseFavDataSource,
        realmManager: RealmManager
    ): FavoriteRepositoryInterface = FavoriteRepository(firebaseFavDataSource, realmManager)
}
