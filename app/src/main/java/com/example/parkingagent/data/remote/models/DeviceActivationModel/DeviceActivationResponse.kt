package com.example.parkingagent.data.remote.models.DeviceActivationModel

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class DeviceActivationResponse(

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("EntityId")
	val entityId: Int? = null,

	@field:SerializedName("DeviceMasterId")
	val deviceMasterId: Int? = null,

	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("IMEINo")
	val iMEINo: String? = null,

	@field:SerializedName("UserId")
	val userId: Int? = null,

	@field:SerializedName("IMEI")
	val iMEI: String? = null,

	@field:SerializedName("ActivationKey")
	val activationKey: String? = null
) : Parcelable
