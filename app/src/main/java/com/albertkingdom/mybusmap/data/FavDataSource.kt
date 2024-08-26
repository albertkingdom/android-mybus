package com.albertkingdom.mybusmap.data

import com.albertkingdom.mybusmap.model.Favorite

interface FavDataSource {
    suspend fun getFavRoute(): Result<List<Favorite>>

    suspend fun saveFavRoute(routeName: String)

    suspend fun deleteFavRoute(routeName: String)

    fun checkLogin(): Boolean
}