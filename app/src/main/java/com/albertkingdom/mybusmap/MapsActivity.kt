package com.albertkingdom.mybusmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.albertkingdom.mybusmap.adapter.NearByStationAdapter
import com.albertkingdom.mybusmap.databinding.ActivityMapsBinding
import com.albertkingdom.mybusmap.model.NearByStation
import com.albertkingdom.mybusmap.ui.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapsActivity : BaseMapActivity(),GoogleMap.OnMyLocationButtonClickListener {
    private lateinit var mapViewModel: MapsViewModel
    private lateinit var mapBinding: ActivityMapsBinding
    private lateinit var nearByStationAdapter: NearByStationAdapter
    lateinit var nearByStationBottomSheetBehavior: BottomSheetBehavior<View>
    lateinit var arrivalTimeBottomSheetBehavior: BottomSheetBehavior<View>
    val highLightMarkersMap = mutableMapOf<Int, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapBinding = ActivityMapsBinding.inflate(layoutInflater)

        setContentView(mapBinding.root)

        mapViewModel = ViewModelProvider(this).get(MapsViewModel::class.java)

        nearByStationAdapter = NearByStationAdapter()

        mapBinding.nearbyStationLayout.stationRecyclerView.adapter = nearByStationAdapter

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupTabLayoutViewPager(
            tabLayout = mapBinding.arrivalTimeLayout.tapLayout,
            viewPager2 = mapBinding.arrivalTimeLayout.viewPager2,
            tabConfigurationStrategy = { tab, position ->
                tab.text = "${position + 1}"
            }
        )
        setupOnTabSelect()
        mapViewModel.nearByStations.observe(this) { setOfStations ->
            Log.d(TAG, "nearByStations $setOfStations")
            addMarker(setOfStations)
            nearByStationAdapter.currentLocation = mapViewModel.currentLocation
            nearByStationAdapter.onClickStationName = clickStationNameCallBack
            nearByStationAdapter.submitList(setOfStations)
        }

        mapViewModel.arrivalTimesLiveData.observe(this) { map ->
            Log.d(TAG, "arrivalTimesLiveData $map")
            mapBinding.arrivalTimeLayout.progressCircular.visibility = View.GONE
            pager2FragmentAdapter.listOfData = map
            pager2FragmentAdapter.notifyDataSetChanged()
        }

        configureBottomSheetBehavior()

        mapBinding.arrivalTimeLayout.closeArrivalTime.setOnClickListener {
            Log.d(TAG, "cancel arrival time")
            clearAllMarker()
            nearByStationBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            arrivalTimeBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        }

        mapBinding.nearbyStationLayout.refreshNearbyStation.setOnClickListener {
            Log.d(TAG, "refresh nearby station")
            mapViewModel.getNearByStopsRx()
        }

        mapViewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }

    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMyLocationButtonClickListener(this)

        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        getDeviceLocation()
    }
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }
    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } else {
                mMap.isMyLocationEnabled = false
                mMap.uiSettings.isMyLocationButtonEnabled = false
                //lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun getDeviceLocationCallBack() {
        super.getDeviceLocationCallBack()
        mapViewModel.currentLocation = LatLng(
            lastKnownLocation!!.latitude,
            lastKnownLocation!!.longitude
        )

        mapViewModel.getNearByStopsRx()
    }



    private val clickStationNameCallBack: (NearByStation) -> Unit = { station: NearByStation ->

        val stationIDs = station.subStation.map { it.stationID }
        mapViewModel.getArrivalTimeRx(stationIDs)
        mapBinding.arrivalTimeLayout.arrivalTimeTitle.text = station.stationName

        nearByStationBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        arrivalTimeBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        val coordinates = station.subStation.map { LatLng(it.stationPosition.PositionLat,it.stationPosition.PositionLon) }
        Log.d(TAG, "on click name coord $coordinates")
        // change marker color

        for ((i, item) in coordinates.withIndex()) {
            val markerOptions = MarkerOptions().position(item)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(station.stationName)
            val marker = mMap.addMarker(markerOptions)

            if (marker != null) {
                highLightMarkersMap[i] = marker
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(coordinates[0].latitude,
               coordinates[0].longitude), DEFAULT_ZOOM.toFloat()))
    }




    fun addMarker(source: List<NearByStation>) {
        clearAllMarker()
        for (station in source) {
            for ( (i, sub) in station.subStation.withIndex()) {
                val location = LatLng(
                    sub.stationPosition.PositionLat,
                    sub.stationPosition.PositionLon
                )
                val markerOptions = MarkerOptions().position(location).title(station.stationName)
                val marker = mMap.addMarker(markerOptions)

                if (marker != null) {
                    highLightMarkersMap[i] = marker
                }
            }
        }
    }

    fun configureBottomSheetBehavior() {
        val nearByStationBottomSheet = mapBinding.nearbyStationLayout.bottomSheetLayoutNearbyStation
        val arrivalTimeBottomSheet = mapBinding.arrivalTimeLayout.bottomSheetLayoutArrivalTime

        nearByStationBottomSheetBehavior = BottomSheetBehavior.from(nearByStationBottomSheet)
        arrivalTimeBottomSheetBehavior = BottomSheetBehavior.from(arrivalTimeBottomSheet)

        nearByStationBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        arrivalTimeBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

    }

    companion object {
        private const val TAG = "MapsActivity"
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        clearAllMarker()
        //marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        Log.d(TAG, "on click marker.position ${marker.title}")
        // show arrival time

        val station = mapViewModel.onClickMarkerRequestArrivalTime(marker)
        val coordinates = station!!.subStation.map {
            LatLng(
                it.stationPosition.PositionLat,
                it.stationPosition.PositionLon
            )
        }

        Log.d(TAG, "on click name coord $coordinates")
        // change marker color
        for ((i, item) in coordinates.withIndex()) {
            val markerOptions = MarkerOptions().position(item)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title(station.stationName)
            val newMarker = mMap.addMarker(markerOptions)
            if (newMarker != null) {
                highLightMarkersMap[i] = newMarker
            }
        }
        mapBinding.arrivalTimeLayout.arrivalTimeTitle.text = station.stationName
        nearByStationBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        arrivalTimeBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        return false
    }

    fun clearAllMarker() {
        highLightMarkersMap.forEach { _, marker ->  marker.remove()}
    }

    override fun onMyLocationButtonClick(): Boolean {
        getDeviceLocation()
        return false
    }

    fun setupOnTabSelect() {
        mapBinding.arrivalTimeLayout.tapLayout.addOnTabSelectedListener( object : TabLayout.OnTabSelectedListener {
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
    }
}