package com.albertkingdom.mybusmap.model

data class NearByStation(
    val stationName: String,
    val subStation: MutableList<StationDetail>
)

data class StationDetail(
    val stationID: String,
    val stationPosition: StopPosition,
    val stationAddress: String?,
    val routeName: MutableList<String>
)