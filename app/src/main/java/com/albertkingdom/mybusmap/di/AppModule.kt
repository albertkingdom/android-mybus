package com.albertkingdom.mybusmap.di

import android.util.Log
import com.albertkingdom.mybusmap.network.BusApi
import com.albertkingdom.mybusmap.network.RetrofitManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApi(): BusApi {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("loggingInterceptor",message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .baseUrl(RetrofitManager.BASE_URL)
            .client(client)
            .build()
        return retrofit.create(BusApi::class.java)
    }

}