package com.albertkingdom.mybusmap.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import timber.log.Timber
import java.io.IOException
import java.util.Locale

object LocationUtils {
    fun getCityName(location: Location, context: Context, callback: (String?) -> Unit) {
        val geocoder = Geocoder(context, Locale("en"))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses?.isNotEmpty() == true) {
                    val city = addresses[0].adminArea.replace("City", "").replace(" ","")
                    callback(city)
                } else {
                    callback(null)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(null)
            }
        } else {

            geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val city = addresses[0].adminArea.replace("City", "").replace(" ","")
                        Timber.d("city name ${city}")

                        callback(city)
                    }

                    override fun onError(errorMessage: String?) {
                        super.onError(errorMessage)
                        callback(null)
                    }
                })
        }
    }
}