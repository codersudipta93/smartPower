package com.example.parkingagent.data.remote.models.Menu

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class MenuRequest(

	@field:SerializedName("EntityId")
	val entityId: String? = null,

	@field:SerializedName("DeviceId")
	val deviceId: String? = null
) : Parcelable
