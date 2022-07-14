package com.albertkingdom.mybusmap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.databinding.ItemArrivalTimeBinding
import com.albertkingdom.mybusmap.model.ArrivalTime


class ArrivalTimeAdapter: ListAdapter<ArrivalTime,ArrivalTimeAdapter.ArrivalTimeViewHolder>(
    DIFF_CALLBACK) {
    inner class ArrivalTimeViewHolder(val binding:ItemArrivalTimeBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArrivalTimeViewHolder {
        val binding = ItemArrivalTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArrivalTimeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArrivalTimeViewHolder, position: Int) {
        val currentItem = getItem(position)
        //holder.binding.timeOfArrival.text = holder.binding.timeOfArrival.resources.getString(R.string.arrival_time_min,currentItem.EstimateTime/60)
        holder.binding.routeName.text = currentItem.RouteName.Zh_tw

        holder.binding.timeOfArrival.text = when (currentItem.StopStatus) {
            0 ->  holder.binding.timeOfArrival.resources.getString(R.string.arrival_time_min,currentItem.EstimateTime/60)
            1 ->  "未發車"
            2 ->  "不停靠"
            3 ->  "末班已過"
            4 ->  "未營運"
            else -> return
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ArrivalTime>() {
            override fun areItemsTheSame(oldItem: ArrivalTime, newItem: ArrivalTime): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: ArrivalTime,
                newItem: ArrivalTime
            ): Boolean {
                return oldItem.RouteName == newItem.RouteName
            }
        }
    }
}