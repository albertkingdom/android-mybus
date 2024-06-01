package com.albertkingdom.mybusmap.ui

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.albertkingdom.mybusmap.model.RealTimeNearStop
import com.albertkingdom.mybusmap.model.Route
import com.albertkingdom.mybusmap.model.Stop
import com.albertkingdom.mybusmap.repository.MyRepository
import com.albertkingdom.mybusmap.util.LocationUtils
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(val repository: MyRepository): ViewModel() {
    var currentLocation: LatLng? = null
    var routeName: String = ""
    var errorMessage = MutableLiveData<String>()
    var cityName: String = ""
    var token: String = ""
    val routeList = MutableLiveData<List<Route>>()
    val stopAndEstimateTime = MutableLiveData<List<Stop>>()
    val realTimeNearStop = MutableLiveData<List<RealTimeNearStop>>()
    private val disposables = CompositeDisposable()

    private fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    fun getStopOfRoute() {
        repository.getTokenRx()
            .subscribeOn(Schedulers.io())
            .flatMap { response ->
                // request for city name
                token = response.accessToken
                if (currentLocation == null) {
                    Observable.error<String>(Throwable("currentLocation is null"))
                }

                Log.d(TAG, "cityname $cityName")

                val requests = listOf(
                    repository.getStopOfRouteRx(auth = "Bearer $token", cityName = cityName, routeName = routeName),
                    repository.getArrivalTimeForSpecificRouteNameRx(auth = "Bearer $token", cityName = cityName, routeName = routeName),
                    repository.getRealTimeNearStopRx(auth = "Bearer $token", cityName = cityName, routeName = routeName)
                )
                Observable.zip(requests) { result ->
                    result
                }

            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                val stopOfRoute = response[0] as List<Route>
                val stopAndEstimateTimeResponse = response[1] as List<Stop>
                val realTimeNearStopResponse = response[2] as List<RealTimeNearStop>
                Log.d(TAG, "getStopOfRouteRx onNext ${stopOfRoute}")
                Log.d(TAG, "getArrivalTimeForSpecificRouteNameRx onNext $stopAndEstimateTimeResponse")
                Log.d(TAG, "getRealTimeNearStopRx onNext $realTimeNearStopResponse")

                handleRouteResponse(stopOfRoute)
                //stopAndEstimateTime.value = stopAndEstimateTimeResponse
                // 合併stopAndEstimateTimeResponse && realTimeNearStopResponse
                val result = combineStopEstimateTimeAndRealTimeNearStop(stopAndEstimateTimeResponse, realTimeNearStopResponse)
                stopAndEstimateTime.value = result

            }, {
                Log.e("On error", it.message.toString())
                errorMessage.value = it.message
            }).apply { addDisposable(this) }
    }
    fun getArrivalTimeForSpecificRouteNameRx() {
        repository.getArrivalTimeForSpecificRouteNameRx(auth = "Bearer $token", cityName = cityName, routeName = routeName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    Log.d(TAG, "getArrivalTimeForSpecificRouteNameRx onNext $list")
                    stopAndEstimateTime.value = list
                }, {
                    Log.e("On error", it.message.toString())
                    errorMessage.value = it.message
                }
            )
    }
    fun getCityName(location: Location, context: Context) {
        cityName = LocationUtils.getCityName(location, context).toString()
    }
    fun getRealTimeNearStopRx() {
        repository.getRealTimeNearStopRx(auth = "Bearer $token", cityName = cityName, routeName = routeName)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { list ->
                    Log.d(TAG, "getRealTimeNearStopRx onNext $list")
                    realTimeNearStop.value = list
                }, {
                    Log.e("On error", it.message.toString())
                    errorMessage.value = it.message
                }
            )
    }
    fun handleRouteResponse(source: List<Route>) {
        val filteredList = source.filter { route -> route.RouteName.Zh_tw == routeName }
        routeList.value = filteredList
    }
    fun combineStopEstimateTimeAndRealTimeNearStop(stopEstimateTime: List<Stop>, realTimeNearStops: List<RealTimeNearStop>): List<Stop> {
        val modifiedStops = stopEstimateTime.map { stop ->
            val matchedRealTimeNearStop = realTimeNearStops.find { realTimeNearStop ->
                realTimeNearStop.StopID == stop.StopID && realTimeNearStop.Direction == stop.Direction
            }
            if (matchedRealTimeNearStop != null){
                stop.PlateNumb = matchedRealTimeNearStop.PlateNumb
                stop.A2EventType = matchedRealTimeNearStop.A2EventType

            }
            stop
        }
        return modifiedStops
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
    companion object {
        val TAG = "RouteViewModel"
    }
}