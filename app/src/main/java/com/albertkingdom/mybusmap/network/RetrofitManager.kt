package com.albertkingdom.mybusmap.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager {
    //val BASE_URL = "https://tdx.transportdata.tw/api/basic/v2/Bus/StopOfRoute/City/"
    val BASE_URL =   "https://ptx.transportdata.tw/MOTC/v2/Bus/StopOfRoute/City/"
    val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("RetrofitManager",message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
        .baseUrl(BASE_URL)
        .client(client)
        .build()

    val retrofitService: BusApi by lazy {
        retrofit.create(BusApi::class.java)
    }
}