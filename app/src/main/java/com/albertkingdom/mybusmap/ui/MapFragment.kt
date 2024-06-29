package com.albertkingdom.mybusmap.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
import java.io.IOException
import java.util.Locale

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
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true || permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            // 權限已授予
            locationPermissionGranted = true
            startLocationUpdates()
        } else {
            // 權限被拒絕
        }
    }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getLocationPermission()
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
        updateLocationUI()
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 15000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(15000)
            .setMaxUpdateDelayMillis(60000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val latestLocation = locationResult.locations.lastOrNull()
                latestLocation?.let { location ->
                    updateMapLocation(location)
                    mapViewModel.currentLocation = LatLng(
                        location.latitude,
                        location.longitude
                    )
                    mapViewModel.getNearByStopsRx()
                    mapViewModel.getCityName(location, requireContext())
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
    private fun updateMapLocation(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, DEFAULT_ZOOM))
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
        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            startLocationUpdates()
        } else if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            startLocationUpdates()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
    private fun updateLocationUI() {
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
    }


    private val clickStationNameCallBack: (NearByStation) -> Unit = { station: NearByStation ->
        mapBinding.searchBar.visibility = View.INVISIBLE
        val stationIDs = station.subStation.map { it.stationID }
        mapViewModel.getArrivalTimeRx(stationIDs)
        mapBinding.arrivalTimeLayout.arrivalTimeTitle.text = station.stationName

        nearByStationBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
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
        private const val TAG = "MapsFragment"
        private const val DEFAULT_ZOOM = 15f
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

    override fun onDestroyView() {
        super.onDestroyView()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }
}