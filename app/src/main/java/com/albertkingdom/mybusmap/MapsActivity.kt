package com.albertkingdom.mybusmap

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.albertkingdom.mybusmap.databinding.ActivityMapsBinding
import com.albertkingdom.mybusmap.ui.FavFragment
import com.albertkingdom.mybusmap.ui.MapFragment
import com.albertkingdom.mybusmap.ui.UserFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapsActivity : BaseMapActivity() {
    private lateinit var mapBinding: ActivityMapsBinding
    private val favFragment by lazy { FavFragment() }
    private val mapFragment by lazy { MapFragment() }
    private val userFragment by lazy { UserFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mapBinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(mapBinding.root)
        setupCurrentFragment(mapFragment)

        // select bottom nav view to switch fragment
        mapBinding.bottomNavView.setOnItemSelectedListener { menu ->
            when(menu.itemId) {
                R.id.map -> setupCurrentFragment(mapFragment)
                R.id.fav_list -> setupCurrentFragment(favFragment)
                R.id.user -> setupCurrentFragment(userFragment)
            }
            true
        }
    }
    private fun setupCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
    }
}