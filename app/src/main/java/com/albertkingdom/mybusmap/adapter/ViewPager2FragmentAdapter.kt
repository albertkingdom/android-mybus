package com.albertkingdom.mybusmap.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.albertkingdom.mybusmap.model.ArrivalTime
import com.albertkingdom.mybusmap.ui.ArrivalTimeFragment

class ViewPager2FragmentAdapter(fa: FragmentActivity): FragmentStateAdapter(fa) {
    var listOfData =  mapOf<String, List<ArrivalTime>>()

    override fun getItemCount(): Int {
        return listOfData.size
    }

    override fun createFragment(position: Int): Fragment {
        val keys = listOfData.keys.toList()
        val stationID = keys[position]
        val listOfArrivalTime = listOfData[stationID]!!
        return ArrivalTimeFragment(listOfArrivalTime)
    }
}