package com.albertkingdom.mybusmap.model

import com.google.gson.annotations.SerializedName

data class Favorite(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("stationID")
    val stationID: String? = null
)

data class FavoriteList(
    @SerializedName("list")
    val list: List<Favorite>? = null
)