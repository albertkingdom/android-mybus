package com.albertkingdom.mybusmap.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.model.db.FavoriteRealm
import com.albertkingdom.mybusmap.repository.FavoriteRepository
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.RealmResults
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FavFragmentViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
): ViewModel() {
    private val _listOfFavorite = MutableLiveData<List<Favorite>>()
    val listOfFavorite: LiveData<List<Favorite>>
        get() = _listOfFavorite
    var isLogin = MutableLiveData<Boolean>(false)
    private lateinit var favoriteRealms: RealmResults<FavoriteRealm> // store realm db query results
    private var firestoreListener: ListenerRegistration? = null // 儲存 Firestore Listener

    init {
        checkIfSignIn()
    }

    fun getFromRemote() {
        viewModelScope.launch {
            favoriteRepository.getFavoritesFromRemote().let {
                Timber.d(it.toString())
                _listOfFavorite.value = it
                syncAllToRealm(it)
            }
        }
    }
    // 將firebase數據同步到local
    private fun syncAllToRealm(favoriteList: List<Favorite>) {
        for (item in favoriteList) {
            item.name?.let { favoriteRepository.saveToLocal(routeName = it) }
        }
    }
    fun removeFromRemote(routeName: String) {
        viewModelScope.launch {
            favoriteRepository.deleteFavFromRemote(routeName)
            getFromRemote()
        }
    }

    fun getFromDB() {
        favoriteRepository.getFavoritesFromLocal().let { listOfStation ->
            _listOfFavorite.value = listOfStation
        }
    }
    fun removeFromDB(routeName: String) {
        favoriteRepository.deleteFromLocal(routeName)
    }
//    private fun getDBLiveChange() {
//        favoriteRealms = RealmManager.shared.queryAllFromDB()
//        val DBchangeListener =
//            OrderedRealmCollectionChangeListener { collection: RealmResults<FavoriteRealm>?, changeSet: OrderedCollectionChangeSet ->
//                // For deletions, notify the UI in reverse order if removing elements the UI
//                val deletions = changeSet.deletionRanges
//                for (i in deletions.indices.reversed()) {
//                    val range = deletions[i]
//                    Timber.d("${range.length} FavoriteRealm deleted at ${range.startIndex}")
//                }
//                val insertions = changeSet.insertionRanges
//                for (range in insertions) {
//                    Timber.d("${range.length} FavoriteRealm inserted at ${range.startIndex}")
//                }
//                val modifications = changeSet.changeRanges
//                for (range in modifications) {
//                    Timber.d("${range.length} FavoriteRealm modified at ${range.startIndex}")
//                }
//
//                val listOfStation =
//                    collection?.map { favoriteRealm -> Favorite(name = favoriteRealm.name) }
//                if (listOfStation != null) {
//                    _listOfFavorite.value = listOfStation
//                }
//            }
//        // Observe collection notifications.
//        favoriteRealms.addChangeListener(DBchangeListener)
//
//    }

//    private fun listenForFirestoreChanges() {
//        firestoreListener = favoriteRepository.getRef().addSnapshotListener { snapshot, e ->
//            if (e != null) {
//                Timber.e(e, "Listen failed.")
//                return@addSnapshotListener
//            }
//            if (snapshot != null && snapshot.exists()){
//                for (dc in snapshot.data) {
//
//                }
//            }
//            snapshot?.data?.forEach { dc ->
//                when (dc.`) {
//                    DocumentChange.Type.ADDED -> {
//                        val addedFavorite= dc.document.toObject(Favorite::class.java)
//                        _listOfFavorite.value = (_listOfFavorite.value ?: emptyList()) + addedFavorite
//                    }
//                    DocumentChange.Type.REMOVED -> {
//                        val removedFavorite = dc.document.toObject(Favorite::class.java)
//                        _listOfFavorite.value = (_listOfFavorite.value ?: emptyList()).filter { it != removedFavorite }
//                    }
//                    // No need to handle MODIFIED in this case, as it's not used
//                    else -> {}
//                }
//            }
//        }
//    }

    private fun checkIfSignIn() {
        favoriteRepository.checkIsLogin().let {
            isLogin.value = it
        }

        if (isLogin.value != true) {
//            listenForFirestoreChanges() // 如果已登入，開始監聽 Firestore 變動

//            getDBLiveChange()
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (::favoriteRealms.isInitialized) {
            favoriteRealms.removeAllChangeListeners()
        }
        firestoreListener?.remove() // 移除 Firestore Listener
    }
}