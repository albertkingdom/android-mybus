package com.albertkingdom.mybusmap.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.adapter.ArrivalTimeAdapter
import com.albertkingdom.mybusmap.databinding.ItemViewPager2FragmentBinding
import com.albertkingdom.mybusmap.model.ArrivalTime
import com.albertkingdom.mybusmap.model.RouteName

class ArrivalTimeFragment(private val listOfArrivalTime: List<ArrivalTime>) : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var binding: ItemViewPager2FragmentBinding
    lateinit var adapter: ArrivalTimeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.item_view_pager2_fragment, container, false)
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ItemViewPager2FragmentBinding.inflate(layoutInflater)
//        recyclerView = binding.arrivalTimeRecyclerview
        recyclerView = view.findViewById(R.id.arrival_time_recyclerview)

        adapter = ArrivalTimeAdapter()
        recyclerView.adapter = adapter
        adapter.submitList(listOfArrivalTime)
    }
}