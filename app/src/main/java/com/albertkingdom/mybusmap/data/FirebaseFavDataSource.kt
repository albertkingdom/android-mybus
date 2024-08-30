package com.albertkingdom.mybusmap.data

import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.model.FavoriteList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class FirebaseFavDataSource @Inject constructor(
    private val fireStore: FirebaseFirestore,
    private val auth: FirebaseAuth
): FavDataSource {

    override fun checkLogin(): Boolean {
        val currentUser = auth.currentUser
        return currentUser != null
    }

//    override fun getRef(): DocumentReference {
//        val currentUser = auth.currentUser ?: throw IllegalArgumentException("User not authenticated")
//        val email = currentUser.email ?: throw IllegalArgumentException("User email is null")
//        return fireStore.collection("favoriteRoute").("Doc")
//    }

    override suspend fun getFavRoute(): Result<List<Favorite>> {
        val currentUser = auth.currentUser
        val email = currentUser?.email?: return Result.failure(Exception("User email not found"))
        return try {
            val docRef = fireStore.collection("favoriteRoute").document(email).get().await()
            val favoriteList = docRef.toObject(FavoriteList::class.java)?.list ?: emptyList()

            Result.success(favoriteList)

        } catch (e: Exception) {
            Timber.d("Current data: null")
            Result.failure(e)
        }
    }

    override suspend fun saveFavRoute(routeName: String) {
        try {
            val currentUser = auth.currentUser ?: run {
                Timber.e("User not logged in.")
                return // Or handle this case appropriately
            }
            val userEmail = currentUser.email ?: return // Early return if email is null
            val docRef = fireStore.collection("favoriteRoutes").document(userEmail) // Pluralized collection name

            val favorite = Favorite(routeName, "") // Create Favorite object

            val snapshot = docRef.get().await() // Use await to get the document snapshot
            if (snapshot.exists()) {
                // Use arrayUnion directly for better efficiency
                docRef.update("list", FieldValue.arrayUnion(favorite)).await() // Use await
                Timber.d("Favorite route added successfully!")
            } else {
                // Initialize the list with the new favorite
                val favorites = listOf(favorite)
                val favoriteList = FavoriteList(favorites)
                docRef.set(favoriteList).await() // Use await
                Timber.d("Favorite list created successfully!")
            }
        } catch (e: Exception) {
            Timber.w(e, "Error adding/creating favorite route")
            // Handle the exception appropriately (e.g., show an error message)
        }
    }

    override suspend fun deleteFavRoute(routeName: String) {
        try {
            val currentUser = auth.currentUser
            val email = currentUser?.email
            val docRef = fireStore.collection("favoriteRoute").document(email!!)
            val favoriteToRemote = Favorite(name = routeName, stationID = "")
            docRef.update("list", FieldValue.arrayRemove(favoriteToRemote)).await()
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete favorite")
        }
    }
}