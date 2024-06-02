package com.albertkingdom.mybusmap.network

import android.util.Base64
import com.albertkingdom.mybusmap.BuildConfig
import com.albertkingdom.mybusmap.model.*
import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import retrofit2.Call
import retrofit2.http.*
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

interface BusApi {

    @GET("https://tdx.transportdata.tw/api/basic/v2/Bus/StopOfRoute/City/{cityName}/{routeName}")
    fun getStopOfRouteRx(@Header("authorization") auth: String,
//                       @Header("X-Date") xDate: String,
                       @Path("cityName") cityName: String,
                       @Path("routeName") routeName: String,
                       @Query("\$top") top: String = "30",
                       @Query("\$format") format: String = "JSON"
    ): Observable<List<Route>>


    @GET("https://ptx.transportdata.tw/MOTC/v2/Bus/Station/NearBy")
    fun getNearByStops(@Header("Authorization") auth: String,
                       @Header("X-Date") xDate: String,
                       @Query("\$spatialFilter") coordinate: String,
                       @Query("\$format") format: String = "JSON"

    ): Call<List<NearByStopsSource>>

    @GET("https://tdx.transportdata.tw/api/advanced/v2/Bus/Station/NearBy")
    fun getNearByStopsRx(@Header("authorization") auth: String,
                       @Query("\$spatialFilter") coordinate: String,
                       @Query("\$format") format: String = "JSON"

    ): Observable<List<NearByStopsSource>>

    @GET("https://tdx.transportdata.tw/api/advanced/v2/Bus/EstimatedTimeOfArrival/City/{cityName}/PassThrough/Station/{stationID}")
    fun getArrivalTime(@Header("authorization") auth: String,
                       @Path("cityName") cityName:String,
                       @Path("stationID") stationID: String,
                       @Query("\$top") top: String = "30",
                       @Query("\$format") format: String = "JSON"
    ): Observable<List<ArrivalTime>>

    @GET("https://tdx.transportdata.tw/api/advanced/V3/Map/GeoLocating/District/LocationX/{Lon}/LocationY/{Lnt}")
    fun getCityNameRx(
        @Path("Lon") lon: Double,
        @Path("Lnt") lnt: Double,
        @Query("\$format") format: String = "JSON",
        @Header("authorization") auth: String

    ): Observable<List<CityName>>

    @FormUrlEncoded
    @POST("https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token")
    fun getTokenRx(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientID: String = BuildConfig.TDX_CLIENT_ID,
        @Field("client_secret") clientSecret: String = BuildConfig.TDX_CLIENT_SECRET
    ): Single<AuthToken>

    @GET("https://tdx.transportdata.tw/api/basic/v2/Bus/EstimatedTimeOfArrival/City/{cityName}/{routeName}")
    fun getArrivalTimeForSpecificRouteNameRx(
        @Path("cityName") cityName: String,
        @Path("routeName") routeName: String,
        @Query("\$filter") filter: String,
        @Query("\$orderby") orderBy: String = "StopID",
        @Query("\$format") format: String = "JSON",
        @Header("authorization") auth: String
    ): Observable<List<Stop>>

    @GET("https://tdx.transportdata.tw/api/basic/v2/Bus/RealTimeNearStop/City/{cityName}/{routeName}")
    fun getRealTimeNearStopRx(
        @Path("cityName") cityName: String,
        @Path("routeName") routeName: String,
        @Query("\$filter") filter: String,
        @Query("\$format") format: String = "JSON",
        @Header("authorization") auth: String
    ): Observable<List<RealTimeNearStop>>
}

fun getHeader(): Map<String, String> {
    val APPID = "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"
    val AppKey =  "FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF"

    val xdate: String = getServerTime()
    val SignDate = "x-date: $xdate"

    val mac: Mac = Mac.getInstance("HmacSHA1")
    val secret_key = SecretKeySpec(AppKey.toByteArray(), "HmacSHA1")
    mac.init(secret_key)

    val hash: String = Base64.encodeToString(mac.doFinal(SignDate.toByteArray()), Base64.NO_WRAP)
    System.out.println("Signature :$hash")
    val sAuth =
        "hmac username=\"$APPID\", algorithm=\"hmac-sha1\", headers=\"x-date\", signature=\"${hash}\""
    println(sAuth)
    return mapOf("Authorization" to sAuth, "X-Date" to xdate)
}
fun getAuthString(): String {
    return getHeader()["Authorization"]!!
}
fun getServerTime(): String {
    val calendar: Calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
    return dateFormat.format(calendar.getTime())
}