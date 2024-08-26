package com.albertkingdom.mybusmap.repository

import com.albertkingdom.mybusmap.model.Favorite


interface FavoriteRepositoryInterface {

    suspend fun getFavoritesFromRemote(): List<Favorite>
    suspend fun saveFavToRemote(routeName: String)

    suspend fun deleteFavFromRemote(routeName: String)

    fun getFavoritesFromLocal(): List<Favorite>

    fun saveToLocal(routeName: String)

    fun deleteFromLocal(routeName: String)

    fun checkIsLogin(): Boolean
}