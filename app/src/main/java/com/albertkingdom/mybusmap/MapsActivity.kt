package com.albertkingdom.mybusmap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.albertkingdom.mybusmap.adapter.NearByStationAdapter
import com.albertkingdom.mybusmap.adapter.ViewPager2FragmentAdapter
import com.albertkingdom.mybusmap.databinding.ActivityMapsBinding
import com.albertkingdom.mybusmap.model.NearByStation
import com.albertkingdom.mybusmap.model.StationDetail
import com.albertkingdom.mybusmap.ui.MapsViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMyLocationButtonClickListener {
    private lateinit var mapViewModel: MapsViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var mapBinding: ActivityMapsBinding
    private lateinit var adapter: NearByStationAdapter
    private var locationPermissionGranted = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient // The entry point to the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    lateinit var nearByStationBottomSheetBehavior: BottomSheetBehavior<View>
    lateinit var arrivalTimeBottomSheetBehavior: BottomSheetBehavior<View>
    val highLightMarkersMap = mutableMapOf<Int, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mapBinding = ActivityMapsBinding.inflate(layoutInflater)
        //sheetBinding = ModalBottomSheetContentBinding.inflate(layoutInflater)
        setContentView(mapBinding.root)

        mapViewModel = ViewModelProvider(this).get(MapsViewModel::class.java)

        adapter = NearByStationAdapter()

        mapBinding.nearbyStationLayout.stationRecyclerView.adapter = adapter

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // setup view pager 2
        val pager2FragmentAdapter = ViewPager2FragmentAdapter(this)
        mapBinding.arrivalTimeLayout.viewPager2.adapter = pager2FragmentAdapter

        TabLayoutMediator(mapBinding.arrivalTimeLayout.tapLayout, mapBinding.arrivalTimeLayout.viewPager2) { tab, position ->
            tab.text = "${position + 1}"
        }.attach()

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
        mapViewModel.nearByStations.observe(this) { setOfStations ->
            Log.d(TAG, "nearByStations $setOfStations")
            addMark(setOfStations)
            adapter.currentLocation = mapViewModel.currentLocation
            adapter.onClickStationName = clickStationNameCallBack
            adapter.submitList(setOfStations)
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
            mapViewModel.getNearByStop()
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
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        //val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
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
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                //lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
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
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))

                            mapViewModel.currentLocation = LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude)
                            // After get device location, getNearByStop
                            mapViewModel.getNearByStop()
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
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

            highLightMarkersMap[i] = marker
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
            LatLng(coordinates[0].latitude,
               coordinates[0].longitude), DEFAULT_ZOOM.toFloat()))
    }




    fun addMark(source: List<NearByStation>) {
        clearAllMarker()
        for (station in source) {
            for ( (i, sub) in station.subStation.withIndex()) {
                val location = LatLng(
                    sub.stationPosition.PositionLat,
                    sub.stationPosition.PositionLon
                )
                val markerOptions = MarkerOptions().position(location).title(station.stationName)
                val marker = mMap.addMarker(markerOptions)

                highLightMarkersMap[i] = marker
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))


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
    fun closeArrivalTimeSheet() {
        arrivalTimeBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
    companion object {
        private val TAG = "MapsActivity"
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        clearAllMarker()
        //marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))

        Log.d(TAG, "on click marker.position ${marker.title}")
        // show arrival time

        val station = mapViewModel.onClickMarker(marker)
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
            val marker = mMap.addMarker(markerOptions)
            highLightMarkersMap[i] = marker
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
        Log.d(TAG, "onMyLocationButtonClick" )
        getDeviceLocation()
        return false
    }
}