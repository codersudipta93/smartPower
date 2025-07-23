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

        val formattedRate = formatRatePerHr(rate.ratePerHr?.toString() ?: "")
        holder.tvRate.text = formattedRate
    }

    private fun formatRatePerHr(rawRate: String): String {
        return rawRate
            .split("|")
            .map { it.trim() }
            .filter { it.contains("Minutes-") && it.contains("Rate-") && it.contains("Total-") }
            .joinToString("\n") { entry ->
                val parts = entry.split(",").map { it.trim() }
                val minutes = parts.find { it.startsWith("Minutes-") }?.substringAfter("Minutes-") ?: "?"
                val rate = parts.find { it.startsWith("Rate-") }?.substringAfter("Rate-") ?: "?"
                val gst = parts.find { it.startsWith("GST-") }?.substringAfter("GST-") ?: "?"
                val total = parts.find { it.startsWith("Total-") }?.substringAfter("Total-") ?: "?"

                "$minutes Min → ₹$rate + ₹$gst GST = ₹$total"
            }
    }

    override fun getItemCount(): Int = items.size

    /** Call this to refresh the list in-place */
    fun updateData(newItems: List<ParkingRateDataItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}

