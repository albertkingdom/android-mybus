package com.albertkingdom.mybusmap.repository

import com.albertkingdom.mybusmap.data.FavDataSource
import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.model.db.FavoriteRealm
import com.albertkingdom.mybusmap.util.RealmManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import timber.log.Timber
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    private val firebaseDataSource: FavDataSource,
    private val realmManager: RealmManager
): FavoriteRepositoryInterface {

    override suspend fun getFavoritesFromRemote(): List<Favorite> {
        // ... Firestore logic to fetch favorites ...
        val result = firebaseDataSource.getFavRoute()

        return if (result.isSuccess) {
            return result.getOrNull()!!
        } else {
            return emptyList()
        }
    }
    override suspend fun saveFavToRemote(routeName: String) {
        firebaseDataSource.saveFavRoute(routeName)
    }

    override suspend fun deleteFavFromRemote(routeName: String) {
        Timber.d("deleteFavFromRemote")
        firebaseDataSource.deleteFavRoute(routeName)
    }

    override fun getFavoritesFromLocal(): List<Favorite> {
        return realmManager.queryAllFromDB().map { favoriteRealm -> Favorite(name= favoriteRealm.name) }
    }

    override fun saveToLocal(routeName: String) {
        realmManager.saveToDB(routeName)
    }

    override fun deleteFromLocal(routeName: String) {
        val favoriteRealmToDelete = FavoriteRealm(routeName, null)
        realmManager.removeFromDB(favoriteRealmToDelete)
    }

    override fun checkIsLogin(): Boolean {
        return firebaseDataSource.checkLogin()
    }

//    override fun getRef(): DocumentReference {
//        return firebaseDataSource.getRef()
//    }

}