package com.santhosh.nearby.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.santhosh.nearby.R
import com.santhosh.nearby.ui.main.model.database.PlaceData
import kotlinx.android.synthetic.main.place_holder.view.*


class PlaceAdapter(val adapterCallback: AdapterCallback) : PagedListAdapter<PlaceData, PlaceAdapter.PlaceHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_holder, parent, false)
        return PlaceHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        val placeData = getItem(position)!!
        holder.index = position
        holder.itemView.place_name.text = placeData.name
        holder.itemView.place_address.text = placeData.address
    }

    inner class PlaceHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var index: Int = 0

        init {
            itemView.place_delete.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            adapterCallback.onDelete(getItem(index)!!)
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PlaceData>() {
            override fun areItemsTheSame(oldData: PlaceData, newData: PlaceData): Boolean =
                    oldData.id == newData.id

            override fun areContentsTheSame(oldData: PlaceData, newData: PlaceData): Boolean =
                    oldData == newData
        }
    }
}