package com.finite.livelocationtest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.finite.livelocationtest.R
import com.finite.livelocationtest.model.LocationModel

class LocationAdapter(val c: Context, val dataList: ArrayList<LocationModel>): RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    class LocationViewHolder(view: View):RecyclerView.ViewHolder(view) {
        var latitude : TextView = view.findViewById(R.id.latitude)
        var longitude : TextView = view.findViewById(R.id.longitude)
        var timestamp : TextView = view.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v  = inflater.inflate(R.layout.single_item,parent,false)
        return LocationViewHolder(v)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val newList = dataList[position]
        holder.latitude.text = newList.lat
        holder.longitude.text = newList.lng
        holder.timestamp.text = newList.tms
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}