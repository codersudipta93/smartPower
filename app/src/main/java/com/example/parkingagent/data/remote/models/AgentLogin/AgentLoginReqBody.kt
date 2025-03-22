package com.example.parkingagent.data.remote.models.AgentLogin

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class AgentLoginReqBody(

	@field:SerializedName("UserName")
	val userName: String? = null,

	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("Password")
	val password: String? = null,

	@field:SerializedName("IMEI")
	val imeiNumber: String?=null

) : Parcelable
