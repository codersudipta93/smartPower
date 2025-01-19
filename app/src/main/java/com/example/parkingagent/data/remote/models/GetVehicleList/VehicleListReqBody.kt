package com.example.parkingagent.data.remote.models.GetVehicleList

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class VehicleListReqBody(

	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("pageNo")
	val pageNo: Int? = 1
) : Parcelable
