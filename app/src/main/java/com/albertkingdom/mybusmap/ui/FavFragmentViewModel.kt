package com.albertkingdom.mybusmap.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.model.FavoriteList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class FavFragmentViewModel: ViewModel() {
    private val _listOfFavorite = MutableLiveData<List<Favorite>>()
    val listOfFavorite: LiveData<List<Favorite>>
        get() = _listOfFavorite
    val db = Firebase.firestore
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var isLogin = MutableLiveData<Boolean>(false)

    init {
        checkIfSignIn()
    }

    fun getFromRemote() {
        val currentUser = auth.currentUser
        val email = currentUser?.email
        val docRef = db.collection("favoriteRoute").document(email!!)

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: ${snapshot.data}")
                val favoriteList = snapshot.toObject<FavoriteList>()
                if (favoriteList?.list != null) {
                    _listOfFavorite.value = favoriteList.list
                }
            } else {
                Log.d(TAG, "Current data: null")
            }
        }

    }
    fun removeFromRemote(routeName: String) {
        val currentUser = auth.currentUser
        val email = currentUser?.email
        val docRef = db.collection("favoriteRoute").document(email!!)
        val favoriteToRemote = Favorite(name = routeName, stationID = "")
        docRef.update("list", FieldValue.arrayRemove(favoriteToRemote))
            .addOnSuccessListener {
                Log.d(TAG, "successfully delete")
            }
            .addOnFailureListener {
                Log.d(TAG, "failure delete")
            }
    }
    fun checkIfSignIn() {
        val currentUser = auth.currentUser
        isLogin.value = currentUser != null
    }

    fun setListOfFavorite(list: List<Favorite>) {
        _listOfFavorite.value = list
    }
    companion object {
        val TAG = "FavFragmentViewModel"
    }
}