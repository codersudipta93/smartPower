package com.example.parkingagent.data.remote.models.VehicleParking

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class VehicleSearchParkingReqBody(
	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("UserId")
	val userId: Int? = null,

	@field:SerializedName("VehicleNo")
	val vehicleNo: String? = null,

) : Parcelable
