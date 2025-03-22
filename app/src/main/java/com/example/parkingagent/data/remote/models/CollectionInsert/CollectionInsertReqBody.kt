package com.example.parkingagent.data.remote.models.CollectionInsert

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class CollectionInsertReqBody(

	@field:SerializedName("EntityId")
	val entityId: Int? = null,

	@field:SerializedName("VehicleNo")
	val vehicleNo: String? = null,

	@field:SerializedName("UserId")
	val userId: Int? = null,

	@field:SerializedName("Amount")
	val amount: Double? = null
) : Parcelable
