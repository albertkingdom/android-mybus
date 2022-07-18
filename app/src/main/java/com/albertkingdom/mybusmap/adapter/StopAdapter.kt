package com.albertkingdom.mybusmap.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.databinding.ItemStopInfoBinding
import com.albertkingdom.mybusmap.model.Stop

class StopAdapter: ListAdapter<Stop, StopAdapter.StopInfoViewHolder>(DIFF_CALLBACK) {

    inner class StopInfoViewHolder(val binding: ItemStopInfoBinding): RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StopInfoViewHolder {
        val binding = ItemStopInfoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StopInfoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StopInfoViewHolder, position: Int) {
        val currentItem = getItem(position)
        //holder.binding.timeOfArrival.text = holder.binding.timeOfArrival.resources.getString(R.string.arrival_time_min,currentItem.EstimateTime/60)
        holder.binding.stopName.apply {
            text = currentItem.StopName.Zh_tw

        }

        holder.binding.timeOfArrival.text = when (currentItem.StopStatus) {
            0 ->  holder.binding.timeOfArrival.resources.getString(R.string.arrival_time_min,currentItem.EstimateTime/60)
            1 ->  "未發車"
            2 ->  "不停靠"
            3 ->  "末班已過"
            4 ->  "未營運"
            else -> return
        }
        holder.binding.plateNumber.apply {
            visibility = if (currentItem.PlateNumb != null) View.VISIBLE else View.INVISIBLE
            text = currentItem.PlateNumb

            if (currentItem.EstimateTime / 60 > 0 && currentItem.PlateNumb != null) {
                Log.d(TAG, "current ${currentItem} ")
                //val height = holder.itemView.height
                //Log.d(TAG, "height $height")
                //val layoutParams = this.layoutParams as LinearLayout.LayoutParams
                //layoutParams.setMargins(0, -25, 0, 0)
                //this.layoutParams = layoutParams
            }
        }

        holder.itemView.setOnClickListener {
            //onClickName?.let { it1 -> it1(currentItem.RouteName.Zh_tw) }
        }
    }

    companion object {
        val TAG = "StopAdapter"
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Stop>() {
            override fun areItemsTheSame(oldItem: Stop, newItem: Stop): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: Stop,
                newItem: Stop
            ): Boolean {
                return oldItem.StopName.Zh_tw == newItem.StopName.Zh_tw
            }
        }
    }
}