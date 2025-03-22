package com.example.parkingagent.data.remote.models.VehicleParking

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class VehicleParkingResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("VehicleNo")
	val vehicleNo: String? = null,

	@field:SerializedName("VehicleTypeId")
	val vehicleTypeId: Int? = null,

	@field:SerializedName("IOType")
	val iOType: Int? = null,

	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("ChargableAmount")
	val chargableAmount: String? = null,

	@field:SerializedName("VehicleParkingId")
	val vehicleParkingId: Int? = null,

	@field:SerializedName("InTime")
	val inTime: String? = null,

	@field:SerializedName("OutTime")
	val outTime: String? = null,

	@field:SerializedName("Duration")
	val duration: String? = null,
//
//	@field:SerializedName("Date")
//	val duration: String? = null

	@field:SerializedName("Location")
	val location: String? = null
) : Parcelable
