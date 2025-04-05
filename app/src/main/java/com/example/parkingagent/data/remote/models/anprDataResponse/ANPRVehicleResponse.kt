package com.example.parkingagent.data.remote.models.anprDataResponse

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class ANPRVehicleResponse(

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("VehicleNo")
	val vehicleNo: String? = null,

	@field:SerializedName("VehicleType")
	val vehicleType: String? = null
) : Parcelable
