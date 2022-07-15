package com.albertkingdom.mybusmap.repository

import com.albertkingdom.mybusmap.BuildConfig
import com.albertkingdom.mybusmap.model.ArrivalTime
import com.albertkingdom.mybusmap.model.AuthToken
import com.albertkingdom.mybusmap.model.CityName
import com.albertkingdom.mybusmap.model.NearByStopsSource
import com.albertkingdom.mybusmap.network.BusApi
import com.albertkingdom.mybusmap.network.RetrofitManager
import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import javax.inject.Inject


class MyRepository @Inject constructor(private val api: BusApi) {
    fun getNearByStops(authHeader: String, timeHeader: String, filter: String): Call<List<NearByStopsSource>> {
        return api.getNearByStops(authHeader, timeHeader, filter)
    }

    fun getArrivalTimeRx(authHeader: String, timeHeader: String, cityName: String, stationID: String): Observable<List<ArrivalTime>> {
        return api.getArrivalTime(auth = authHeader, xDate = timeHeader, cityName = cityName, stationID = stationID)
    }

    fun getCityNameRx(lon: Double, lnt: Double, auth: String): Observable<List<CityName>> {
        return api.getCityNameRx(lon = lon, lnt = lnt, auth = auth)
    }

    fun getTokenRx(): Observable<AuthToken> {
        return api.getTokenRx()
    }
}