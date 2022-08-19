package com.albertkingdom.mybusmap.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        viewModel = ViewModelProvider(this).get(FavFragmentViewModel::class.java)

        binding = FavFragmentBinding.inflate(inflater, container, false)
        listView = binding.listView

        viewModel.isLogin.observe(viewLifecycleOwner) { isLogin ->
            if (isLogin) {
                viewModel.getFromRemote()
            } else {
                getFromLocal()
            }
        }
        viewModel.listOfFavorite.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                binding.emptyListPlaceholder.visibility = View.GONE
            }
            adapter = FavRouteAdapter(requireContext(), R.layout.item_fav_list, list)
            listView.adapter = adapter
            adapter.deleteFav = onDeleteFav
        }

        setupListView()
        return binding.root
    }

    private fun setupListView() {
        listView.setOnItemClickListener { adapterView, view, position, _ ->
            val routeName = (adapterView.getItemAtPosition(position) as Favorite).name
            val intent = Intent(requireActivity(), RouteOfStopActivity::class.java)
            intent.putExtra("click route name", routeName)
            startActivity(intent)
        }
    }
    fun getFromLocal() {
        val listOfStation = Preference(requireContext()).getFavRoute()
        viewModel.setListOfFavorite(listOfStation)
    }
    private val onDeleteFav = { routeName: String ->

        AlertDialog.Builder(requireContext())
            .setTitle("確認刪除")
            .setMessage("確認刪除 $routeName 路線?")
            .setPositiveButton("確認") { _, _ ->

                if (viewModel.isLogin.value == true) {
                    viewModel.removeFromRemote(routeName)
                } else {
                    val listOfStation = Preference(requireContext()).removeFavRoute(routeName)
                    viewModel.setListOfFavorite(listOfStation)
                }
            }
            .setNegativeButton("取消") { _, _ ->
            }
            .create()
            .show()
    }

    companion object {
        const val TAG = "FavFragment"
    }

}