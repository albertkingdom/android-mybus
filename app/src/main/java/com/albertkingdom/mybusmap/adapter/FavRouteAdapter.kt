package com.albertkingdom.mybusmap.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.model.Favorite


class FavRouteAdapter(context: Context, val layout: Int, data: List<Favorite>): ArrayAdapter<Favorite>(context, layout, data) {
    var deleteFav: ((String) -> Unit)? = null
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = View.inflate(parent.context, layout, null)
        val item = getItem(position) ?: return view
        val routeName = view.findViewById<TextView>(R.id.fav_route_name)
        val favIcon = view.findViewById<ImageView>(R.id.heart)

        routeName.text = item.name
        favIcon.setOnClickListener {
            // remove from sharedpreference
            deleteFav?.let { it1 ->
                if (item.name != null) {
                    it1(item.name)
                }

            }
        }
        return view
    }

    companion object {
        const val TAG = "FavRouteAdapter"
    }
}