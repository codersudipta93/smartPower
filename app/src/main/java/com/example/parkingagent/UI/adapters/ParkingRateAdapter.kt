package com.example.parkingagent.UI.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.parkingagent.R
import com.example.parkingagent.data.remote.models.ParkingRate.ParkingRateDataItem

class ParkingRateAdapter(
    private var items: List<ParkingRateDataItem>
) : RecyclerView.Adapter<ParkingRateAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvVehicleType: TextView = view.findViewById(R.id.tvVehicleType)
        val tvRate: TextView = view.findViewById(R.id.tvRate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_parking_rate, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rate = items[position]
        holder.tvVehicleType.text = rate.vehicleType ?: "—"
        holder.tvRate.text = String.format("\u20B9%.2f/hr", rate.ratePerHr ?: 0.0)
    }

    override fun getItemCount(): Int = items.size

    /** Call this to refresh the list in-place */
    fun updateData(newItems: List<ParkingRateDataItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

