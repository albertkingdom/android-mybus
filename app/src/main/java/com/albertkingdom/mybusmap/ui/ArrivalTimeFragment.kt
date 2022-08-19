package com.albertkingdom.mybusmap.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.RouteOfStopActivity
import com.albertkingdom.mybusmap.adapter.ArrivalTimeAdapter
import com.albertkingdom.mybusmap.adapter.StopAdapter
import com.albertkingdom.mybusmap.databinding.ItemViewPager2FragmentBinding
import com.albertkingdom.mybusmap.model.ArrivalTime
import com.albertkingdom.mybusmap.model.Favorite
import com.albertkingdom.mybusmap.model.FavoriteList
import com.albertkingdom.mybusmap.model.Stop
import com.albertkingdom.mybusmap.util.Preference
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class ArrivalTimeFragment(private val listOfArrivalTime: List<ArrivalTime>?,
                          private val listOfStop: List<Stop>?) : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var viewModel: ArrivalTimeFragmentViewModel
    lateinit var binding: ItemViewPager2FragmentBinding
    var arrivalTimeAdapter: ArrivalTimeAdapter? = null
    var stopAdapter: StopAdapter? = null
    val db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    var isLogin = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.item_view_pager2_fragment, container, false)
        Log.d(TAG, "onCreateView")
        auth = FirebaseAuth.getInstance()
        checkIfSignIn()
        viewModel = ViewModelProvider(this).get(ArrivalTimeFragmentViewModel::class.java)

        if (isLogin) {
            viewModel.getFavoriteRouteFromRemote()
        }
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ItemViewPager2FragmentBinding.inflate(layoutInflater)

        recyclerView = view.findViewById(R.id.arrival_time_recyclerview)
        Log.d(TAG, "")
        if (listOfArrivalTime != null) {
            Log.d(TAG, "listOfArrivalTime $listOfArrivalTime")

            arrivalTimeAdapter = ArrivalTimeAdapter()
            arrivalTimeAdapter!!.onClickName = onClickRouteName
            arrivalTimeAdapter!!.onClickHeart = onClickHeart
            recyclerView.adapter = arrivalTimeAdapter

            val listOfFav = Preference(requireContext()).getFavRoute()
            
            if (isLogin) {
                viewModel.listOfFavorite.observe(viewLifecycleOwner) { list ->
                    Log.d(TAG, "listOfFavorite $list")
                    val listOfRouteName = list.map { it.name!! }
                    arrivalTimeAdapter!!.favRouteName = listOfRouteName
                    arrivalTimeAdapter!!.submitList(listOfArrivalTime)
                }
            } else {
                val listOfRouteName = listOfFav.map { it.name!! }
                arrivalTimeAdapter!!.favRouteName = listOfRouteName
                arrivalTimeAdapter!!.submitList(listOfArrivalTime)

            }



        }
        if (listOfStop != null) {
            Log.d(TAG, "listOfStop $listOfStop")
            stopAdapter = StopAdapter()
            //stopAdapter.onClickName = onClickRouteName
            recyclerView.adapter = stopAdapter
            stopAdapter!!.submitList(listOfStop)
        }
    }

    val onClickRouteName: (String) -> Unit = { routeName: String ->
        Log.d(TAG, "click route name $routeName")

        val intent = Intent(context,RouteOfStopActivity::class.java)
        intent.putExtra("click route name", routeName)
        startActivity(intent)
    }

    val onClickHeart: (String, Boolean) -> Unit = { routeName, isExisted ->
        if (!isExisted) {
            val favorite = Favorite(name = routeName, stationID = "")
            if (isLogin) {
                saveToRemote(favorite)
            } else {
                Preference(requireContext()).saveFavRoute(favorite)
            }
        } else {
            Preference(requireContext()).removeFavRoute(routeName)
        }

    }
    fun saveToRemote(favorite: Favorite) {
        val currentUser = auth.currentUser

            val userEmail = currentUser?.email!!

            val ref = db.collection("favoriteRoute").document(userEmail)
            ref.addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: ${snapshot.data}")
                    ref.update("list", FieldValue.arrayUnion(favorite))
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                } else {
                    Log.d(TAG, "Current data: null")
                    val favoriteList = FavoriteList(list = listOf(favorite))
                       ref.set(favoriteList)
                        .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                }
            }
    }
    fun checkIfSignIn() {
        val currentUser = auth.currentUser
        isLogin = currentUser != null
    }
    companion object {
        val TAG = "ArrivalTimeFragment"
    }
}