package com.example.parkingagent.data.remote.models.DeviceActivationModel

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
data class DeviceActivationReqBody(

	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("UserId")
	val userId: String? = null,

	@field:SerializedName("IMEI")
	val iMEI: String? = null,

	@field:SerializedName("ActivationKey")
	val activationKey: String? = null
) : Parcelable
