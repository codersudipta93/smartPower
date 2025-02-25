package com.example.parkingagent.data.remote.models.VehicleParking

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class VehicleParkingCardResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("VehicleNo")
	val vehicleNo: String? = null,

	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("ChargableAmount")
	val chargableAmount: Double? = null,

	@field:SerializedName("OutTime")
	val outTime: String? = null,

	@field:SerializedName("Duration")
	val duration: String? = null,

	@field:SerializedName("CardNo")
	val cardNo: String? = null,

	@field:SerializedName("IOType")
	val iOType: Int? = null,

	@field:SerializedName("InTime")
	val inTime: String? = null,

	@field:SerializedName("UserId")
	val userId: Int? = null,

	@field:SerializedName("VehicleParkingId")
	val vehicleParkingId: Int? = null,

	@field:SerializedName("ParkingTime")
	val parkingTime: String? = null
) : Parcelable
