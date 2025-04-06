package com.example.parkingagent.data.remote.models.ParkingRate

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class ParkingRateReqBody(

	@field:SerializedName("EntityId")
	val entityId: Int? = null,

	@field:SerializedName("UserId")
	val userId: Int? = null,

	@field:SerializedName("Token")
	val token: String? = null
) : Parcelable
