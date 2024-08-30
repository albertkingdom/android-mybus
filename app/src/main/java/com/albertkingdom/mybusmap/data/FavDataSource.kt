package com.albertkingdom.mybusmap.data

import com.albertkingdom.mybusmap.model.Favorite
import com.google.firebase.firestore.DocumentReference

interface FavDataSource {
    suspend fun getFavRoute(): Result<List<Favorite>>

    suspend fun saveFavRoute(routeName: String)

    suspend fun deleteFavRoute(routeName: String)

    fun checkLogin(): Boolean

//    fun getRef(): DocumentReference
}