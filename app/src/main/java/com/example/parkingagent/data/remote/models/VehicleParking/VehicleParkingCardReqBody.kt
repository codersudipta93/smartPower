package com.example.parkingagent.data.remote.models.VehicleParking

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class VehicleParkingCardReqBody(

	@field:SerializedName("CardNo")
	val cardNo: String? = null,

	@field:SerializedName("IOType")
	val iOType: Int? = null,

	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("UserId")
	val userId: Int? = null,

	@field:SerializedName("ParkingTime")
	val parkingTime: String? = null
) : Parcelable
