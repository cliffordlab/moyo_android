package com.cliffordlab.amoss.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cliffordlab.amoss.R
import com.cliffordlab.amoss.models.VitalItems

class VitalsAdapter(
    var items: List<VitalItems>
) : RecyclerView.Adapter<VitalsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_vitals_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.date.text = items[position].createdAt
        holder.values.text = items[position].vitals
        holder.title.text = items[position].title
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.vitalsTitle)
        val date: TextView = view.findViewById(R.id.vitalsDate)
        val values: TextView = view.findViewById(R.id.vitalValues)
    }

}