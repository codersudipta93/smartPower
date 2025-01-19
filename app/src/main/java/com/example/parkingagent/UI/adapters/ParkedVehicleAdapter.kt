package com.example.parkingagent.UI.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.paging.PagingDataAdapter
import com.example.parkingagent.databinding.ItemParkedVehicleBinding
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleItem

class ParkedVehicleAdapter : PagingDataAdapter<VehicleItem, ParkedVehicleAdapter.ParkedVehicleViewHolder>(VehicleDiffCallback()) {

    inner class ParkedVehicleViewHolder(private val binding: ItemParkedVehicleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(vehicleItem: VehicleItem?) {
            vehicleItem?.let { vehicle ->
                // Bind data to the views
                binding.idVehicleId.text = vehicle.vehicleNo
                binding.idSecondParams.text = "${vehicle.vehicleType} | ${vehicle.inTime}"

                // Set additional click listeners if needed
                binding.root.setOnClickListener {
                    // Handle click events
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkedVehicleViewHolder {
        val binding = ItemParkedVehicleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ParkedVehicleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ParkedVehicleViewHolder, position: Int) {
        val vehicleItem = getItem(position)
        holder.bind(vehicleItem)
    }

    // DiffUtil for comparing VehicleItem objects
    class VehicleDiffCallback : DiffUtil.ItemCallback<VehicleItem>() {
        override fun areItemsTheSame(oldItem: VehicleItem, newItem: VehicleItem): Boolean {
            return oldItem.vehicleNo == newItem.vehicleNo // Unique ID comparison
        }

        override fun areContentsTheSame(oldItem: VehicleItem, newItem: VehicleItem): Boolean {
            return oldItem == newItem // Data comparison
        }
    }
}
