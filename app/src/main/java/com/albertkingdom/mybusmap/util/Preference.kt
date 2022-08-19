package com.albertkingdom.mybusmap.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.model.FavoriteList
import com.google.gson.Gson

class Preference(val context: Context) {
    companion object {
        const val PREF_NAME = "fav_station"
        const val KEY_NAME = "station"
        const val TAG = "Preference"
    }

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveFavRoute(routeName: Favorite) {
        val saved = getFavRoute()
        Log.d(TAG, "saved $saved")
        var newList: List<Favorite> = listOf(routeName)
        if (saved.isNotEmpty()) {
            newList = saved + routeName // combine saved and newly added
        }
        val favoriteList = FavoriteList(list = newList)
        Log.d(TAG, "newList $newList")
        Log.d(TAG, "favoriteList $favoriteList")
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(favoriteList)
        editor.putString(KEY_NAME, json).apply()
    }

    fun getFavRoute(): List<Favorite> {
        val station = sharedPref.getString(KEY_NAME, "")
        val stationList = Gson().fromJson(station, FavoriteList::class.java)

        Log.d(TAG, "stationList $stationList")
        if (stationList == null) {
            return emptyList()
        }
        return stationList.list!!
    }

    fun removeFavRoute(route: String): List<Favorite> {
        val saved = getFavRoute()
        val newList = saved.filter { favorite ->  favorite.name != route}
        val favoriteList = FavoriteList(list = newList)
        val editor = sharedPref.edit()
        val gson = Gson()
        val json = gson.toJson(favoriteList)
        editor.putString(KEY_NAME, json).apply()

        return getFavRoute()
    }
}