package com.albertkingdom.mybusmap.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.util.Preference

class FavFragmentViewModel: ViewModel() {
    val favoriteRoute = MutableLiveData<List<Favorite>>()

    init {

    }
    fun getFavoriteRoute() {

    }

}