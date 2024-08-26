package com.albertkingdom.mybusmap.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.repository.FavoriteRepository
import com.albertkingdom.mybusmap.repository.FavoriteRepositoryInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ArrivalTimeViewModel @Inject constructor(private val favoriteRepository: FavoriteRepositoryInterface):ViewModel() {


    private val _isLogin = MutableLiveData<Boolean>(false);
    val isLogin: LiveData<Boolean> = _isLogin;
    private val _listOfFavorite = MutableLiveData<List<Favorite>>()
    val listOfFavorite: LiveData<List<Favorite>> = _listOfFavorite

    init {
        checkIfSignIn();
    }

    fun checkIfSignIn() {
        favoriteRepository.checkIsLogin().let { isLogin ->
            _isLogin.setValue(isLogin)
        }
    }

    fun getFavoriteRouteFromRemote() {
        viewModelScope.launch {
            favoriteRepository.getFavoritesFromRemote().let {
                Timber.d(it.toString());
                _listOfFavorite.setValue(it);
            }
        }
    }
    fun saveToRemote(routeName: String) {
        viewModelScope.launch {
            favoriteRepository.saveFavToRemote(routeName);
        }
    }

    fun removeFromRemote(routeName: String) {
        viewModelScope.launch {
            favoriteRepository.deleteFavFromRemote(routeName);
        }
    }

    fun getFromDB() {
        favoriteRepository.getFavoritesFromLocal().let {
            _listOfFavorite.setValue(it);
        }
    }

    fun saveToDB(routeName: String) {
        Timber.d("save routeName $routeName")
        favoriteRepository.saveToLocal(routeName)
    }

    fun removeFromDB(routeName: String) {
        favoriteRepository.deleteFromLocal(routeName)
    }
}