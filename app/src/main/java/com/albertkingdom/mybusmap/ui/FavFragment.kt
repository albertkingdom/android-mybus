package com.albertkingdom.mybusmap.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.RouteOfStopActivity
import com.albertkingdom.mybusmap.adapter.FavRouteAdapter
import com.albertkingdom.mybusmap.databinding.FavFragmentBinding
import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.util.Preference


class FavFragment: Fragment(R.layout.fav_fragment) {
    private lateinit var listView: ListView
    private lateinit var binding: FavFragmentBinding
    lateinit var adapter: FavRouteAdapter
    private lateinit var viewModel: FavFragmentViewModel
    private val listOfFavorite = mutableListOf<Favorite>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(FavFragmentViewModel::class.java)

        val listOfStation = Preference(requireContext()).getFavRoute()
        listOfFavorite.clear()
        listOfFavorite.addAll(listOfStation)

        binding = FavFragmentBinding.inflate(inflater, container, false)
        listView = binding.listView

        adapter = FavRouteAdapter(requireContext(), R.layout.item_fav_list, listOfFavorite)
        adapter.deleteFav = onDeleteFav

        if (listOfStation.isNotEmpty()) {
            binding.emptyListPlaceholder.visibility = View.GONE
        }
        setupListView()
        return binding.root
    }

    private fun setupListView() {
        listView.adapter = adapter


        listView.setOnItemClickListener { adapterView, view, position, _ ->
            val routeName = (adapterView.getItemAtPosition(position) as Favorite).name
            val intent = Intent(requireActivity(), RouteOfStopActivity::class.java)
            intent.putExtra("click route name", routeName)
            startActivity(intent)
        }
    }

    private val onDeleteFav = { routeName: String ->
        val listOfStation = Preference(requireContext()).removeFavRoute(routeName)
        listOfFavorite.clear()
        listOfFavorite.addAll(listOfStation)
        adapter.notifyDataSetChanged()
    }
    companion object {
        const val TAG = "FavFragment"
    }

}