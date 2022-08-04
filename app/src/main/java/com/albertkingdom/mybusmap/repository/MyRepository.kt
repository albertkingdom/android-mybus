package com.albertkingdom.mybusmap.repository

import com.albertkingdom.mybusmap.model.*
import com.albertkingdom.mybusmap.network.BusApi
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import javax.inject.Inject


class MyRepository @Inject constructor(private val api: BusApi) {
    fun getNearByStopsRx(authHeader: String, filter: String): Observable<List<NearByStopsSource>> {
        return api.getNearByStopsRx(auth = authHeader, coordinate = filter)
    }
    fun getArrivalTimeRx(authHeader: String, cityName: String, stationID: String): Observable<List<ArrivalTime>> {
        return api.getArrivalTime(auth = authHeader, cityName = cityName, stationID = stationID)
    }

    fun getStopOfRouteRx(auth: String, cityName: String, routeName: String): Observable<List<Route>> {
        return api.getStopOfRouteRx(auth = auth, cityName = cityName, routeName = routeName)
    }

    fun getCityNameRx(lon: Double, lnt: Double, auth: String): Observable<List<CityName>> {
        return api.getCityNameRx(lon = lon, lnt = lnt, auth = auth)
    }

    fun getTokenRx(): Observable<AuthToken> {
        return api.getTokenRx()
    }

    fun getArrivalTimeForSpecificRouteNameRx(auth: String, cityName: String, routeName: String): Observable<List<Stop>> {
        return api.getArrivalTimeForSpecificRouteNameRx(cityName = cityName, routeName = routeName, auth = auth, filter = "RouteName/Zh_tw eq '$routeName'")
    }

    fun getRealTimeNearStopRx(auth: String, cityName: String, routeName: String): Observable<List<RealTimeNearStop>> {
        return api.getRealTimeNearStopRx(cityName = cityName, routeName = routeName, auth = auth, filter = "RouteName/Zh_tw eq '$routeName'")
    }
}