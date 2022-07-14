package com.albertkingdom.mybusmap

object Constant {
    val BASE_URL = "https://ptx.transportdata.tw/MOTC/v2/Bus/StopOfRoute/City/"
    const val BASE_URL_NEARBY_STOPS = "https://ptx.transportdata.tw/MOTC/v2/Bus/Station/NearBy?%24spatialFilter=nearby(25.0392167,%20121.445724,%20300)&%24format=JSON"

    // 預估到站時間
    //"https://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/City/Taipei/PassThrough/Station/1000782?%24top=30&%24format=JSON"
    var BASE_URL_ESTIMATED_ARRIVAL_TIME = "https://ptx.transportdata.tw/MOTC/v2/Bus/EstimatedTimeOfArrival/City/{cityName}/PassThrough/Station/{stationID}?%24top=30&%24format=JSON"
}