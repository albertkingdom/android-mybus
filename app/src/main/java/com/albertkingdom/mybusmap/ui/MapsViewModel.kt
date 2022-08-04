package com.albertkingdom.mybusmap.ui

import android.util.Base64
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.albertkingdom.mybusmap.model.*
import com.albertkingdom.mybusmap.repository.MyRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor(private val repository: MyRepository): ViewModel() {

    val nearByStations = MutableLiveData<List<NearByStation>>()
    val arrivalTimesLiveData = MutableLiveData<Map<String, List<ArrivalTime>>>()
    var currentLocation: LatLng? = null
    var errorMessage = MutableLiveData<String>()
    var token: String = ""

    fun getHeaderHMAC(): Map<String, String> {

        val APPID = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"
        val AppKey =  "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"

        val xdate: String = getServerTime()
        val signDate = "x-date: $xdate"

        val mac: Mac = Mac.getInstance("HmacSHA1")
        val secretKey = SecretKeySpec(AppKey.toByteArray(), "HmacSHA1")
        mac.init(secretKey)

        val hash: String = Base64.encodeToString(mac.doFinal(signDate.toByteArray()), Base64.NO_WRAP)
        System.out.println("Signature :$hash")
        val sAuth =
            "hmac username=\"$APPID\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\"${hash}\""
        return mapOf("Authorization" to sAuth, "X-Date" to xdate)
    }

    fun getServerTime(): String {
        val calendar: Calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormat.format(calendar.time)
    }


    fun getNearByStopsRx() {
        Log.d(TAG, "getNearByStopsRx")
        currentLocation?.let {
            val filterString = "nearby(${it.latitude},${it.longitude},300)"
            repository.getTokenRx()
                .subscribeOn(Schedulers.io())
                .flatMap { response ->
                    // request for city name
                    val token = response.accessToken
                    Log.d(TAG, "token $token")
                    if (currentLocation == null) {

                    }

                    repository.getNearByStopsRx(
                        authHeader = "Bearer $token",
                        filter = filterString
                    )

                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ list ->
                    Log.d("on next", list.toString())
                    val stations = handleNearByStopsResponse(list)
                    nearByStations.value = stations

                }, {
                    Log.e("On error", it.localizedMessage)
                    errorMessage.value = it.localizedMessage
                })
        }
    }

    fun handleNearByStopsResponse(source: List<NearByStopsSource>): List<NearByStation> {
        val setOfStops = mutableListOf<NearByStation>()
        for (stop in source) {
            val routeNames = stop.Stops.map { Stop ->
                Stop.RouteName.Zh_tw
            } // 公車路線 e.g. [99,802]

            val index = setOfStops.indexOfFirst { nearByStation ->  nearByStation.stationName == stop.StationName.Zh_tw}

            if (index == -1) {
                // different stationName "裁示所"


                val subStation = mutableListOf<StationDetail>()
                subStation.add(StationDetail(stationPosition = stop.StationPosition, stationAddress = stop.StationAddress, routeName = routeNames.toMutableList(), stationID = stop.StationID))

                setOfStops.add(
                    NearByStation(stationName = stop.StationName.Zh_tw,
                        subStation = subStation)
                )

            } else {
                // same stationName "裁示所"
                val sameStationIDIndex = setOfStops[index].subStation.indexOfFirst { stationDetail ->  stationDetail.stationID == stop.StationID}
                if (sameStationIDIndex == -1) {
                    // 建立新的stationDetail
                    setOfStops[index].subStation.add(
                        StationDetail(stationPosition = stop.StationPosition, stationAddress = stop.StationAddress, routeName = routeNames.toMutableList(), stationID = stop.StationID)
                    )
                } else {
                   // 加入routeName到既有
                    setOfStops[index].subStation[sameStationIDIndex].routeName.addAll(routeNames)
                }
            }
        }

        Log.d(TAG, "handleNearByStopsResponse $setOfStops")

        return setOfStops
    }
    /* gettoken -> get cityname -> getArrivalTime */
    fun getArrivalTimeRx(stationIDs: List<String>) {

        val authHeader: String = getHeaderHMAC()["Authorization"]!!
        val timeHeader: String = getServerTime()

        val stationIDandArrivalTimes = mutableMapOf<String, List<ArrivalTime>>()

        repository.getTokenRx()
            .subscribeOn(Schedulers.io())
            .flatMap { response ->
                // request for city name
                token = response.accessToken

                if (currentLocation == null) {

                }

                repository.getCityNameRx(
                    lon = currentLocation!!.longitude,
                    lnt = currentLocation!!.latitude,
                    auth = "Bearer $token"
                )

            }.flatMap { list ->
                // request for arrival time
                val cityName = list[0].City
                Log.d(TAG, "cityname $cityName")

                val requests: List<Observable<List<ArrivalTime>>> = stationIDs.map { id ->
                    repository.getArrivalTimeRx(
                        authHeader = "Bearer $token",
                        cityName = cityName,
                        stationID = id
                    )
                }
                // zip multiple retrofit request into one
                Observable.zip(requests) { result ->
                    result
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                for (i in response.indices) {
                    Log.d("On next", "$i.. ${response[i].toString()}")
                    stationIDandArrivalTimes[stationIDs[i]] = response[i] as List<ArrivalTime>
                }
                handleArrivalTimeResponse(stationIDandArrivalTimes)

            }, {
                Log.e("On error", it.localizedMessage)
                errorMessage.value = it.localizedMessage
            })


    }
    fun handleArrivalTimeResponse(source: Map<String, List<ArrivalTime>>) {
        arrivalTimesLiveData.value = source
        Log.d("zip arrivalTimesLiveData", arrivalTimesLiveData.toString())
    }
    companion object {
        val TAG = "MapsViewModel"
    }

    fun onClickMarkerRequestArrivalTime(marker: Marker): NearByStation? {
        /* find the station where its substation position is equal to clicked marker position,
        return clicked marker station id
         */

        var stationIDs = listOf<String>()
        val targetNearbyStation = nearByStations.value?.find { nearByStation ->
            val result = nearByStation.subStation.find { stationDetail ->
                val position = LatLng(
                    stationDetail.stationPosition.PositionLat,
                    stationDetail.stationPosition.PositionLon
                )
                position == marker.position
            }
            result != null
        }
        if (targetNearbyStation != null) {
            stationIDs = targetNearbyStation.subStation.map { sub -> sub.stationID }
        }
        getArrivalTimeRx(stationIDs = stationIDs)
        return targetNearbyStation
    }


}