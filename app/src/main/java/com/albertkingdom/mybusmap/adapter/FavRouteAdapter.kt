package com.albertkingdom.mybusmap.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.albertkingdom.mybusmap.R
import com.albertkingdom.mybusmap.model.Favorite


//class FavRouteAdapter(context: Context, val layout: Int, data: List<Favorite>): ArrayAdapter<Favorite>(context, layout, data) {
//    var deleteFav: ((String) -> Unit)? = null
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        val view = View.inflate(parent.context, layout, null)
//        val item = getItem(position) ?: return view
//        val routeName = view.findViewById<TextView>(R.id.fav_route_name)
//        val favIcon = view.findViewById<ImageView>(R.id.heart)
//
//        routeName.text = item.name
//        favIcon.setOnClickListener {
//            // remove from sharedpreference
//            deleteFav?.let { it1 ->
//                if (item.name != null) {
//                    it1(item.name)
//                }
//
//            }
//        }
//        return view
//    }
//
//    companion object {
//        const val TAG = "FavRouteAdapter"
//    }
//}


class FavRouteAdapter(
    private val onItemClicked: (Favorite) -> Unit,
    private val onDeleteFav: ((Favorite) -> Unit)? = null
) : ListAdapter<Favorite, FavRouteAdapter.FavRouteViewHolder>(ItemDiffCallback()) {
    class FavRouteViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textView = itemView.findViewById<TextView>(R.id.fav_route_name)
        val favIcon = itemView.findViewById<ImageView>(R.id.heart)
    }

//    override fun getItemViewType(position: Int): Int {
//        return when (getItem(position)) {
//
//        }
//    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavRouteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fav_list, parent, false)
        return FavRouteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavRouteViewHolder, position: Int) {
        val item = getItem(position)
        holder.textView.text = item.name
        holder.itemView.setOnClickListener {
            onItemClicked(item)
        }
        holder.favIcon.setOnClickListener {
            onDeleteFav?.let { it1 -> it1(item) }
        }
    }
}
class ItemDiffCallback: DiffUtil.ItemCallback<Favorite>() {
    override fun areItemsTheSame(oldItem: Favorite, newItem: Favorite): Boolean {
        return oldItem.name == newItem.name && oldItem.stationID == newItem.stationID
    }
    override fun areContentsTheSame(oldItem: Favorite, newItem: Favorite): Boolean {
        return oldItem == newItem
    }
}