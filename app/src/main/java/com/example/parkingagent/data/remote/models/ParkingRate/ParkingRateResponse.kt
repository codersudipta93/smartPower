package com.example.parkingagent.data.remote.models.ParkingRate

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class ParkingRateResponse(

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("Data")
	val data: List<ParkingRateDataItem?>? = null
) : Parcelable

@Parcelize
data class ParkingRateDataItem(

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("EntityId")
	val entityId: Int? = null,

	@field:SerializedName("UserId")
	val userId: Int? = null,

	@field:SerializedName("VehicleType")
	val vehicleType: String? = null,

	@field:SerializedName("RatePerHr")
	val ratePerHr: String? = null,

	@field:SerializedName("Token")
	val token: String? = null
) : Parcelable
