package com.albertkingdom.mybusmap.model

import com.google.gson.annotations.SerializedName

data class Favorite(
    @SerializedName("name")
    val name: String,
    @SerializedName("stationID")
    val stationID: String
)

data class FavoriteList(
    @SerializedName("list")
    val list: List<Favorite>
)