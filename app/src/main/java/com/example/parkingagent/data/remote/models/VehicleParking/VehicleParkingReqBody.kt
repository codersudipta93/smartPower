package com.example.parkingagent.data.remote.models.VehicleParking

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class VehicleParkingReqBody(

	@field:SerializedName("UserId")
	val userId: Int? = null,

	@field:SerializedName("IOType")
	val iOType: String? = null,

	@field:SerializedName("VehicleNo")
	val vehicleNo: String? = null,

	@field:SerializedName("VehicleTypeId")
	val vehicleTypeId: String? = null,

	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("ParkingMode")
	val parkingMode: String? = null,

	@field:SerializedName("BookingNumber")
	val BookingNumber: String? = null,

//	@field:SerializedName("Token")
//	val Token: String? = null,
) : Parcelable
