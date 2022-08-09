package com.albertkingdom.mybusmap.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.albertkingdom.mybusmap.BaseMapActivity
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.adapter.NearByStationAdapter
import com.albertkingdom.mybusmap.adapter.ViewPager2FragmentAdapter
import com.albertkingdom.mybusmap.databinding.MapFragmentBinding
import com.albertkingdom.mybusmap.model.NearByStation
import com.google.android.gms.common.api.Status
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
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment: Fragment(), GoogleMap.OnMyLocationButtonClickListener,
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    lateinit var pager2FragmentAdapter: ViewPager2FragmentAdapter
    lateinit var mMap: GoogleMap
    var locationPermissionGranted = true
    var lastKnownLocation: Location? = null
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient // The entry point to the Fused Location Provider.
    private val defaultLocation = LatLng(25.0476133, 121.5152175)
    private lateinit var mapViewModel: MapsViewModel
    private lateinit var mapBinding: MapFragmentBinding
    private lateinit var nearByStationAdapter: NearByStationAdapter
    lateinit var nearByStationBottomSheetBehavior: BottomSheetBehavior<View>
    lateinit var arrivalTimeBottomSheetBehavior: BottomSheetBehavior<View>
    lateinit var autocompleteFragment: AutocompleteSupportFragment // search bar
    val highLightMarkersMap = mutableMapOf<Int, Marker>()
    private val markers = mutableListOf<Marker>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       super.onCreateView(inflater, container, savedInstanceState)
        mapBinding = MapFragmentBinding.inflate(layoutInflater, container, false)
        mapViewModel = ViewModelProvider(this).get(MapsViewModel::class.java)

        nearByStationAdapter = NearByStationAdapter()

        mapBinding.nearbyStationLayout.stationRecyclerView.adapter = nearByStationAdapter
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupTabLayoutViewPager(
            tabLayout = mapBinding.arrivalTimeLayout.tapLayout,
            viewPager2 = mapBinding.arrivalTimeLayout.viewPager2,
            tabConfigurationStrategy = { tab, position ->
                tab.text = "${position + 1}"
            }
        )
        setupOnSelectBottomSheetTab()
        mapViewModel.nearByStations.observe(viewLifecycleOwner) { setOfStations ->
            Log.d(TAG, "nearByStations $setOfStations")
            addMarker(setOfStations, isHighlight = false)
            nearByStationAdapter.currentLocation = mapViewModel.currentLocation
            nearByStationAdapter.onClickStationName = clickStationNameCallBack
            nearByStationAdapter.submitList(setOfStations)
        }

        mapViewModel.arrivalTimesLiveData.observe(viewLifecycleOwner) { map ->
            Log.d(TAG, "arrivalTimesLiveData $map")
            mapBinding.arrivalTimeLayout.progressCircular.visibility = View.GONE
            pager2FragmentAdapter.listOfData = map
            pager2FragmentAdapter.notifyDataSetChanged()
        }

        configureBottomSheetBehavior()

        mapBinding.arrivalTimeLayout.closeArrivalTime.setOnClickListener {
            Log.d(TAG, "cancel arrival time")
            mapBinding.searchBar.visibility = View.VISIBLE
            clearHighlightMarker()
            nearByStationBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            arrivalTimeBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        }

        mapBinding.nearbyStationLayout.refreshNearbyStation.setOnClickListener {
            Log.d(TAG, "refresh nearby station")
            mapViewModel.getNearByStopsRx()
        }

        mapViewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }

        setupSearchBar()



        return mapBinding.root
    }
    fun setupTabLayoutViewPager(tabLayout: TabLayout, viewPager2: ViewPager2, tabConfigurationStrategy: TabLayoutMediator.TabConfigurationStrategy) {
        pager2FragmentAdapter = ViewPager2FragmentAdapter(this)
        viewPager2.adapter = pager2FragmentAdapter
        viewPager2.isSaveEnabled = false
        val tabMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabMediator.attach()
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setPadding(0, 200, 0, 0)
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
    }
    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            Log.d(TAG,"lastKnownLocation is $lastKnownLocation" )
                            moveCamera(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude)
                            // After get device location,
                            getDeviceLocationCallBack()
                        }
                    } else {
                        Log.d(BaseMapActivity.TAG, "Current location is null. Using defaults.")
                        Log.e(BaseMapActivity.TAG, "Exception: %s", task.exception)

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
        Log.d(BaseMapActivity.TAG, "moveCamera")
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), BaseMapActivity.DEFAULT_ZOOM.toFloat()))
    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
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

    fun getDeviceLocationCallBack() {

        mapViewModel.currentLocation = LatLng(
            lastKnownLocation!!.latitude,
            lastKnownLocation!!.longitude
        )

        mapViewModel.getNearByStopsRx()
    }



    private val clickStationNameCallBack: (NearByStation) -> Unit = { station: NearByStation ->
        mapBinding.searchBar.visibility = View.INVISIBLE
        val stationIDs = station.subStation.map { it.stationID }
        mapViewModel.getArrivalTimeRx(stationIDs)
        mapBinding.arrivalTimeLayout.arrivalTimeTitle.text = station.stationName

        nearByStationBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        arrivalTimeBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        val coordinates = station.subStation.map { LatLng(it.stationPosition.PositionLat,it.stationPosition.PositionLon) }
        Log.d(TAG, "on click name coord $coordinates")
        // change marker color
        addMarker(source = listOf(station), isHighlight = true)
        moveCamera(coordinates[0].latitude, coordinates[0].longitude)
    }



    private fun addMarker(source: List<NearByStation>, isHighlight: Boolean) {
        if (!isHighlight) {
            clearAllMarker()
        }
        if (isHighlight) {
            clearHighlightMarker()
        }
        for (station in source) {
            for ( (i, sub) in station.subStation.withIndex()) {
                val location = LatLng(
                    sub.stationPosition.PositionLat,
                    sub.stationPosition.PositionLon
                )
                if (!isHighlight) {
                    val markerOptions =
                        MarkerOptions().position(location).title(station.stationName)
                    val marker = mMap.addMarker(markerOptions)
                    if (marker != null) {
                        markers.add(marker)
                    }
                }
                if(isHighlight) {
                    val markerOptions =
                        MarkerOptions().position(location).title(station.stationName)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    val marker = mMap.addMarker(markerOptions)
                    if (marker != null) {
                        highLightMarkersMap[i] = marker
                    }
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

    /**
     * click marker to highlight and show station arrival time
     */
    override fun onMarkerClick(marker: Marker): Boolean {
        clearHighlightMarker()

        Log.d(TAG, "on click marker.position ${marker.title}")
        // show arrival time

        val station = mapViewModel.onClickMarkerRequestArrivalTime(marker)

        addMarker(source = listOf(station!!), isHighlight = true)

        mapBinding.arrivalTimeLayout.arrivalTimeTitle.text = station.stationName
        nearByStationBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        arrivalTimeBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        return false
    }

    private fun clearAllMarker() {
        markers.forEach { marker ->  marker.remove() }
        markers.clear()
    }
    private fun clearHighlightMarker() {
        highLightMarkersMap.forEach { _, marker ->  marker.remove()}
        highLightMarkersMap.clear()
    }
    override fun onMyLocationButtonClick(): Boolean {
        getDeviceLocation()
        return false
    }

    fun setupOnSelectBottomSheetTab() {
        mapBinding.arrivalTimeLayout.tapLayout.addOnTabSelectedListener( object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position!!
                Log.d(TAG, "onTabSelected ${position}")
                // select tab and highlight 去/回marker --> modify logic
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

    private fun setupSearchBar() {
        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment
        autocompleteFragment.view?.setBackgroundColor(Color.WHITE)
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment.setCountries("TW")
        autocompleteFragment.setHint("搜尋地點")
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                Log.i(TAG, "Place: ${place.name}, ${place.id}, ${place.latLng}")
                // move camera
                place.latLng?.let {
                    moveCamera(it.latitude, it.longitude)
                    mapViewModel.currentLocation = it
                }
                // search nearby station
                mapViewModel.getNearByStopsRx()
                autocompleteFragment.setText(null)
            }

            override fun onError(status: Status) {
                Log.i(TAG, "An error occurred: $status")
            }
        })

    }
}