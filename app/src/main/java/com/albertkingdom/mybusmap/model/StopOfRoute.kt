package com.albertkingdom.mybusmap.model

data class Route(
    val RouteName: StopName,
    val Direction: Int, //: [0:'去程',1:'返程',2:'迴圈',255:'未知']
    val City: String,
    val Stops: List<Stop>
)

data class Stop (
    val StopID: String,
    val StopName: StopName,
    val StopPosition: StopPosition,
    val RouteName: StopName,
    val Direction: Int,
    val StopStatus: Int,
    val EstimateTime: Int,
    var PlateNumb: String?,
    var A2EventType: Int // : [0:'離站',1:'進站']
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

// 車牌 + 接近的站
data class RealTimeNearStop(
    val PlateNumb: String,
    val RouteName: RouteName,
    val Direction: Int,
    val StopID: String,
    val A2EventType: Int // : [0:'離站',1:'進站']
)