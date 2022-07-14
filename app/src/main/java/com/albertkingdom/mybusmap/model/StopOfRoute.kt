package com.albertkingdom.mybusmap.model

data class StopOfRoute(
    val Stops: List<Stop>
)

data class Stop (
    val StopName: StopName,
    val RouteName: StopName
    )

data class StopName (
    val Zh_tw: String,
    val En: String
)

data class StopPosition(
   val PositionLon: Double,
   val PositionLat: Double
)

// 附近公車站
data class NearByStopsSource (
    val Stops: List<Stop>,
    val StationName: StationName,
    val StationPosition: StopPosition,
    val StationAddress: String,
    val StationID: String
)

data class StationName (
    val Zh_tw: String
    )

