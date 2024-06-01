package com.albertkingdom.mybusmap

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.albertkingdom.mybusmap.databinding.RouteStopActivityBinding
import com.albertkingdom.mybusmap.model.Route
import com.albertkingdom.mybusmap.ui.RouteViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RouteOfStopActivity : BaseMapActivity() {
    private lateinit var routeViewModel: RouteViewModel
    private lateinit var binding: RouteStopActivityBinding
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

        val routeName = intent.getStringExtra("click route name")

        if (routeName != null) {
            routeViewModel.routeName = routeName
            binding.stopListLayout.arrivalTimeTitle.text = routeName
        }
        binding.stopListLayout.closeArrivalTime.visibility = View.GONE

        binding.closeRouteActivity.setOnClickListener {
            finish()
        }

        setupTabLayoutViewPager(
            tabLayout = binding.stopListLayout.tapLayout,
            viewPager2 = binding.stopListLayout.viewPager2,
            tabConfigurationStrategy = { tab, position ->
                tab.text = when (position) {
                    0 -> "去程"
                    1 -> "返程"
                    else -> return@setupTabLayoutViewPager
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
       super.onMapReady(googleMap)
        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this)
        getDeviceLocation()
    }
    override fun getDeviceLocationCallBack() {
        routeViewModel.currentLocation = LatLng(
            lastKnownLocation!!.latitude,
            lastKnownLocation!!.longitude
        )
        routeViewModel.getCityName(lastKnownLocation!!, this)
        routeViewModel.getStopOfRoute()
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