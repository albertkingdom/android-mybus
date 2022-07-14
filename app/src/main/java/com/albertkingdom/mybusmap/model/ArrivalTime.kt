package com.albertkingdom.mybusmap.model

data class ArrivalTime(
    val Direction: Int, // [0:'去程',1:'返程',2:'迴圈',255:'未知']
    val EstimateTime: Int,
    val RouteName: RouteName,
    val StopStatus: Int //  [0:'正常',1:'尚未發車',2:'交管不停靠',3:'末班車已過',4:'今日未營運']
)
data class RouteName (
    val Zh_tw: String,
    val En: String
)
