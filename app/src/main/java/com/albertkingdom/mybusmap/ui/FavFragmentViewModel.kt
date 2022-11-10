package com.albertkingdom.mybusmap.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.model.FavoriteList
import com.albertkingdom.mybusmap.model.db.FavoriteRealm
import com.albertkingdom.mybusmap.util.RealmManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import io.realm.OrderedCollectionChangeSet
import io.realm.OrderedRealmCollectionChangeListener
import io.realm.RealmResults
import timber.log.Timber

class FavFragmentViewModel: ViewModel() {
    private val _listOfFavorite = MutableLiveData<List<Favorite>>()
    val listOfFavorite: LiveData<List<Favorite>>
        get() = _listOfFavorite
    val db = Firebase.firestore
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var isLogin = MutableLiveData<Boolean>(false)
    private lateinit var favoriteRealms: RealmResults<FavoriteRealm> // store realm db query results
    init {
        checkIfSignIn()
    }

    fun getFromRemote() {
        val currentUser = auth.currentUser
        val email = currentUser?.email
        val docRef = db.collection("favoriteRoute").document(email!!)

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Timber.w("Listen failed. $e")
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                Timber.d("Current data: ${snapshot.data}")
                val favoriteList = snapshot.toObject<FavoriteList>()
                if (favoriteList?.list != null) {
                    _listOfFavorite.value = favoriteList.list
                }
            } else {
                Timber.d("Current data: null")
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
                Timber.d("successfully delete")
            }
            .addOnFailureListener {
                Timber.d("failure delete")
            }
    }

    fun getFromDB() {
        val listOfStation = RealmManager.shared.queryAllFromDB().map { favoriteRealm -> Favorite(name= favoriteRealm.name) }
        _listOfFavorite.value = listOfStation
    }
    fun removeFromDB(routeName: String) {
        val favoriteRealmToDelete = FavoriteRealm(routeName, null)
        RealmManager.shared.removeFromDB(favoriteRealmToDelete)

    }
    private fun getDBLiveChange() {
        favoriteRealms = RealmManager.shared.queryAllFromDB()
        val DBchangeListener =
            OrderedRealmCollectionChangeListener { collection: RealmResults<FavoriteRealm>?, changeSet: OrderedCollectionChangeSet ->
                // For deletions, notify the UI in reverse order if removing elements the UI
                val deletions = changeSet.deletionRanges
                for (i in deletions.indices.reversed()) {
                    val range = deletions[i]
                    Timber.d("${range.length} FavoriteRealm deleted at ${range.startIndex}")
                }
                val insertions = changeSet.insertionRanges
                for (range in insertions) {
                    Timber.d("${range.length} FavoriteRealm inserted at ${range.startIndex}")
                }
                val modifications = changeSet.changeRanges
                for (range in modifications) {
                    Timber.d("${range.length} FavoriteRealm modified at ${range.startIndex}")
                }

                val listOfStation =
                    collection?.map { favoriteRealm -> Favorite(name = favoriteRealm.name) }
                if (listOfStation != null) {
                    _listOfFavorite.value = listOfStation
                }
            }
        // Observe collection notifications.
        favoriteRealms.addChangeListener(DBchangeListener)

    }
    fun checkIfSignIn() {
        val currentUser = auth.currentUser
        isLogin.value = currentUser != null

        if (isLogin.value != true) {
            getDBLiveChange()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::favoriteRealms.isInitialized) {
            favoriteRealms.removeAllChangeListeners()
        }
    }
}