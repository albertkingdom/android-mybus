package com.albertkingdom.mybusmap

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.albertkingdom.mybusmap.adapter.ViewPager2FragmentAdapter
import com.albertkingdom.mybusmap.adapter.ViewPager2FragmentAdapterForActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.libraries.places.api.Places
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

abstract class BaseMapActivity: AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    lateinit var pager2FragmentAdapter: ViewPager2FragmentAdapterForActivity
    lateinit var mMap: GoogleMap
    var locationPermissionGranted = true
    var lastKnownLocation: Location? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient // The entry point to the Fused Location Provider.
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the SDK
        Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY)

        // Create a new PlacesClient instance
        val placesClient = Places.createClient(this)

    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        return false
    }

    fun setupTabLayoutViewPager(tabLayout: TabLayout, viewPager2: ViewPager2, tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy) {
        pager2FragmentAdapter = ViewPager2FragmentAdapterForActivity(this)
        viewPager2.adapter = pager2FragmentAdapter
        val tabMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabMediator.attach()
    }
    /* After get device location successfully
     */
    open fun getDeviceLocationCallBack() {}
    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {

                            moveCamera(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                            // After get device location,
                            getDeviceLocationCallBack()
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)

                        moveCamera(defaultLocation.latitude, defaultLocation.longitude)
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    fun moveCamera(lat: Double, lon: Double) {
        Log.d(TAG, "moveCamera")
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), DEFAULT_ZOOM.toFloat()))
    }
    companion object {
        val TAG = "RouteOfStopActivity"
        const val DEFAULT_ZOOM = 15
    }
}