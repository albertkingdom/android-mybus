package com.albertkingdom.mybusmap.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.RouteOfStopActivity
import com.albertkingdom.mybusmap.adapter.ArrivalTimeAdapter
import com.albertkingdom.mybusmap.adapter.StopAdapter
import com.albertkingdom.mybusmap.databinding.ItemViewPager2FragmentBinding
import com.albertkingdom.mybusmap.model.ArrivalTime
import com.albertkingdom.mybusmap.model.Stop
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ArrivalTimeFragment(private val listOfArrivalTime: List<ArrivalTime>?,
                          private val listOfStop: List<Stop>?) : Fragment() {
    lateinit var recyclerView: RecyclerView
    private val viewModel: ArrivalTimeViewModel by viewModels()
    lateinit var binding: ItemViewPager2FragmentBinding
    var arrivalTimeAdapter: ArrivalTimeAdapter? = null
    var stopAdapter: StopAdapter? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.item_view_pager2_fragment, container, false)

        viewModel.isLogin.observe(viewLifecycleOwner) { isLogin ->
            if (isLogin) {
                viewModel.getFavoriteRouteFromRemote()
            } else {
                viewModel.getFromDB()
            }
        }
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ItemViewPager2FragmentBinding.inflate(layoutInflater)

        recyclerView = view.findViewById(R.id.arrival_time_recyclerview)

        if (listOfArrivalTime != null) {
            Timber.d("listOfArrivalTime $listOfArrivalTime")

            arrivalTimeAdapter = ArrivalTimeAdapter()
            arrivalTimeAdapter!!.onClickName = onClickRouteName
            arrivalTimeAdapter!!.onClickHeart = onClickHeart
            recyclerView.adapter = arrivalTimeAdapter

            viewModel.listOfFavorite.observe(viewLifecycleOwner) { list ->
                Timber.d("listOfFavorite $list")
                val listOfRouteName = list.map { it.name!! }
                arrivalTimeAdapter!!.favRouteName = listOfRouteName
                arrivalTimeAdapter!!.submitList(listOfArrivalTime)
            }
        }
        if (listOfStop != null) {
            Timber.d("listOfStop $listOfStop")
            stopAdapter = StopAdapter()
            //stopAdapter.onClickName = onClickRouteName
            recyclerView.adapter = stopAdapter
            stopAdapter!!.submitList(listOfStop)
        }
    }

    val onClickRouteName: (String) -> Unit = { routeName: String ->
        Timber.d("click route name $routeName")

        val intent = Intent(context,RouteOfStopActivity::class.java)
        intent.putExtra("click route name", routeName)
        startActivity(intent)
    }

    val onClickHeart: (String, Boolean) -> Unit = { routeName, isExisted ->
        if (!isExisted) {
            // save to db or firebase
           if (viewModel.isLogin.value == true) {
                viewModel.saveToRemote(routeName)
            } else {
               viewModel.saveToDB(routeName)
            }
        } else {
            // remove from db or firebase
            if (viewModel.isLogin.value == true) {
                viewModel.removeFromRemote(routeName)
            } else {
                viewModel.removeFromDB(routeName)
            }
        }

    }
}