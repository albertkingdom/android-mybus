package com.albertkingdom.mybusmap.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import java.io.IOException
import java.util.Locale

object LocationUtils {
    fun getCityName(location: Location, context: Context): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address> = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses.isNotEmpty()) {
                val city = addresses[0].adminArea.split(" ")[0]
                city
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}