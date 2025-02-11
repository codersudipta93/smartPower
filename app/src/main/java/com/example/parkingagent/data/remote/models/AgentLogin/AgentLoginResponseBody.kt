package com.example.parkingagent.data.remote.models.AgentLogin

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class AgentLoginResponseBody(

	@field:SerializedName("Msg")
	val msg: String? = null,

	@field:SerializedName("UserName")
	val userName: String? = null,

	@field:SerializedName("UserId")
	val userId: Int? = null,

	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("FullName")
	val fullName: String? = null,

	@field:SerializedName("Password")
	val password: String? = null,

	@field:SerializedName("Status")
	val status: Boolean? = null
) : Parcelable
