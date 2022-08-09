package com.albertkingdom.mybusmap.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.albertkingdom.mybusmap.model.ArrivalTime
import com.albertkingdom.mybusmap.model.Route
import com.albertkingdom.mybusmap.model.Stop
import com.albertkingdom.mybusmap.ui.ArrivalTimeFragment

/* bottom sheet tab 切換分頁 adapter */
class ViewPager2FragmentAdapterForActivity(fa: FragmentActivity): FragmentStateAdapter(fa) {
    var listOfData =  mapOf<String, List<ArrivalTime>>()
    var listOfRouteData = listOf<Route>()
    var listOfStopData = listOf<Stop>()

    override fun getItemCount(): Int {
        if (listOfData.isNotEmpty()) {
            return listOfData.size
        }
        if (listOfStopData.isNotEmpty()) {
            return 2 // 去/ 返
        }
        return 0
    }

    override fun createFragment(position: Int): Fragment {
        if (listOfData.isNotEmpty()) {
                val keys = listOfData.keys.toList()
                val stationID = keys[position]
                val listOfArrivalTime = listOfData[stationID]!!
                return ArrivalTimeFragment(listOfArrivalTime, null)
        } else {

           // val id = keys[position]
            val listOfStop = listOfStopData.filter { stop -> stop.Direction == position }
            return ArrivalTimeFragment(null, listOfStop)
        }

    }
}