package com.example.parkingagent.data.remote.models.DeviceheartBeat

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class DeviceHeartBeatResponse(

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("DeviceId")
	val deviceId: String? = null
) : Parcelable
