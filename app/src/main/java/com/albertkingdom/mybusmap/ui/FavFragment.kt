package com.albertkingdom.mybusmap.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.RouteOfStopActivity
import com.albertkingdom.mybusmap.adapter.FavRouteAdapter
import com.albertkingdom.mybusmap.databinding.FavFragmentBinding
import com.albertkingdom.mybusmap.model.Favorite
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class FavFragment: Fragment(R.layout.fav_fragment) {
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: FavFragmentBinding
    lateinit var adapter: FavRouteAdapter
    private val viewModel: FavFragmentViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FavFragmentBinding.bind(view)
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = FavRouteAdapter(onClick, onDeleteFav)
        recyclerView.adapter = adapter
        viewModel.isLogin.observe(viewLifecycleOwner) { isLogin ->
            if (isLogin) {
                viewModel.getFromRemote()
            } else {
                viewModel.getFromDB()
            }
        }
        viewModel.listOfFavorite.observe(viewLifecycleOwner) { list ->
            Timber.d(list.toString())
            if (list.isNotEmpty()) {
                binding.emptyListPlaceholder.visibility = View.GONE
            } else {
                binding.emptyListPlaceholder.visibility = View.VISIBLE
            }
            adapter.submitList(list)
//            adapter.deleteFav = onDeleteFav
        }

//        setupListView()
    }
    private val onClick = { favorite: Favorite ->
        println(favorite)
        val routeName = favorite.name
        val intent = Intent(requireActivity(), RouteOfStopActivity::class.java)
        intent.putExtra("click route name", routeName)
        startActivity(intent)
    }
//    private fun setupListView() {
//        recyclerView.setOnItemClickListener { adapterView, view, position, _ ->
//            val routeName = (adapterView.getItemAtPosition(position) as Favorite).name
//            val intent = Intent(requireActivity(), RouteOfStopActivity::class.java)
//            intent.putExtra("click route name", routeName)
//            startActivity(intent)
//        }
//    }

    private val onDeleteFav = { favorite: Favorite ->
        val routeName = favorite.name
        if (routeName != null) {
            showDeleteConfirmationDialog(routeName) {
                when (viewModel.isLogin.value) {
                    true -> viewModel.removeFromRemote(routeName)
                    false -> viewModel.removeFromDB(routeName)
                    else -> {} // Handle nullcase if necessary
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(routeName: String, onConfirm: () -> Unit ) {
        AlertDialog.Builder(requireContext())
            .setTitle("確認刪除")
            .setMessage("確認刪除 $routeName 路線?")
            .setPositiveButton("確認") { _, _ ->
               onConfirm()
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