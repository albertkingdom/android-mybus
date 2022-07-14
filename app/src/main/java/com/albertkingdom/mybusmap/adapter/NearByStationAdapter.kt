package com.albertkingdom.mybusmap.adapter

import android.content.res.Resources
import android.content.res.loader.ResourcesLoader
import android.location.GnssAntennaInfo
import android.provider.Settings.Global.getString
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.TypedArrayUtils.getString
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.databinding.ItemNearbyStationBinding
import com.albertkingdom.mybusmap.model.NearByStation
import com.albertkingdom.mybusmap.model.StationDetail
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

class NearByStationAdapter: ListAdapter<NearByStation, NearByStationAdapter.NearByStationViewHolder>(
    DIFF_CALLBACK) {
    var currentLocation: LatLng? = null
    var onClickStationName: ((station: NearByStation) -> Unit)? = null

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<NearByStation>() {
            override fun areItemsTheSame(oldItem: NearByStation, newItem: NearByStation): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: NearByStation,
                newItem: NearByStation
            ): Boolean {
//                return oldItem.subStation[0] == newItem.subStation[0]
                return oldItem.subStation == newItem.subStation
            }
        }
    }
    class NearByStationViewHolder(val binding: ItemNearbyStationBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind() {

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearByStationViewHolder {
        val binding = ItemNearbyStationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NearByStationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NearByStationViewHolder, position: Int) {
        val currentItem = getItem(position)
//        val location = LatLng(currentItem.subStation[0].stationPosition.PositionLat, currentItem.subStation[0].stationPosition.PositionLon)
        val location = LatLng(currentItem.subStation[0].stationPosition.PositionLat, currentItem.subStation[0].stationPosition.PositionLon) //有一組以上的location,取第一個coordinate
        Log.d("adapter", "onBindViewHolder" )
        holder.binding.stationStationName.text = currentItem.stationName
        holder.binding.distance.text = holder.binding.distance.resources.getString(R.string.distance, calculateDistance(to = location).toInt())
        holder.binding.count.text = holder.binding.count.resources.getString(R.string.stop_count, currentItem.subStation.size)
        holder.itemView.setOnClickListener {
            if (onClickStationName != null) {
//                val stationIDs = mutableListOf<String>()
//                for (sub in currentItem.subStation) {
//                    stationIDs.add(sub.stationID)
//                }
                onClickStationName!!(currentItem)
            }
        }
    }

    fun calculateDistance(from: LatLng? = currentLocation, to: LatLng): Double {
        if (from != null) {
            return SphericalUtil.computeDistanceBetween(from, to)
        }
        return 0.0
    }
}