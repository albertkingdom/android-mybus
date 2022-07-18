package com.albertkingdom.mybusmap

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.albertkingdom.mybusmap.adapter.ViewPager2FragmentAdapter
import com.albertkingdom.mybusmap.databinding.RouteStopActivityBinding
import com.albertkingdom.mybusmap.model.NearByStation
import com.albertkingdom.mybusmap.model.Route
import com.albertkingdom.mybusmap.ui.RouteViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RouteOfStopActivity: AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private lateinit var routeViewModel: RouteViewModel
    private lateinit var mMap: GoogleMap
    private var locationPermissionGranted = true
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient // The entry point to the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private lateinit var binding: RouteStopActivityBinding
    val highLightMarkersMap = mutableMapOf<Int, Marker>()
    lateinit var arrivalTimeBottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RouteStopActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate")
        routeViewModel = ViewModelProvider(this).get(RouteViewModel::class.java)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val routeName = intent.getStringExtra("click route name")

        if (routeName != null) {
            routeViewModel.routeName = routeName
            binding.stopListLayout.arrivalTimeTitle.text = routeName
        }
        binding.stopListLayout.closeArrivalTime.visibility = View.GONE

        binding.closeRouteActivity.setOnClickListener {
            Log.d(TAG, "click close button")
            finish()
        }

        val pager2FragmentAdapter = ViewPager2FragmentAdapter(this)
        binding.stopListLayout.viewPager2.adapter = pager2FragmentAdapter
        val tabMediator = TabLayoutMediator(binding.stopListLayout.tapLayout, binding.stopListLayout.viewPager2) { tab, position ->
            tab.text = when (position) {
                0 -> "去程"
                1 -> "返程"
                else -> return@TabLayoutMediator
            }

        }
        tabMediator.attach()
        binding.stopListLayout.tapLayout.addOnTabSelectedListener( object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position!!
                Log.d(TAG, "onTabSelected ${position}")
                highLightMarkersMap.forEach { i, marker ->
                    if (i==position) {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                        marker.showInfoWindow()
                    } else {
                        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        routeViewModel.routeList.observe(this) { list ->


            addMarker(list[0])
        }

        routeViewModel.stopAndEstimateTime.observe(this) { list ->
            Log.d(TAG, "stopAndEstimateTime $list")
            binding.stopListLayout.progressCircular.visibility = View.GONE
            pager2FragmentAdapter.listOfStopData = list

            pager2FragmentAdapter.notifyDataSetChanged()
        }

        configureBottomSheetBehavior()
    }
    companion object {
        val TAG = "RouteOfStopActivity"
        private const val DEFAULT_ZOOM = 15
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this)


        //getLocationPermission()
        //updateLocationUI()
        getDeviceLocation()
    }
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
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
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))

                            routeViewModel.currentLocation = LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude)
                            // After get device location, getNearByStop
                            routeViewModel.getStopOfRoute()
//                            routeViewModel.getArrivalTimeForSpecificRouteNameRx()
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap.moveCamera(
                            CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap.uiSettings.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    override fun onMarkerClick(p0: Marker): Boolean {
        return false
    }

    fun addMarker(source: Route) {
        //clearAllMarker()
        for (stop in source.Stops) {

                val location = LatLng(
                    stop.StopPosition.PositionLat,
                    stop.StopPosition.PositionLon
                )
                val markerOptions = MarkerOptions().position(location).title(stop.StopName.Zh_tw)
                mMap.addMarker(markerOptions)

            }
        }
    fun configureBottomSheetBehavior() {
        val arrivalTimeBottomSheet = binding.stopListLayout.bottomSheetLayoutArrivalTime

        arrivalTimeBottomSheetBehavior = BottomSheetBehavior.from(arrivalTimeBottomSheet)

        arrivalTimeBottomSheetBehavior.isHideable = false
    }
}