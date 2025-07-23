package com.example.parkingagent.data.remote.models.CollectionInsert

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class CollectionInsertResponse(

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("Data")
	val collectionInsertData: CollectionInsertData? = null
) : Parcelable

@Parcelize
data class CollectionInsertData(

	@field:SerializedName("EntityId")
	val entityId: Int? = null,

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("VehicleNo")
	val vehicleNo: String? = null,

	@field:SerializedName("VehicleTypeId")
	val vehicleTypeId: Int? = null,

	@field:SerializedName("IsCollected")
	val isCollected: Boolean? = null,

	@field:SerializedName("UserId")
	val userId: Int? = null,

	@field:SerializedName("CollectionId")
	val collectionId: Int? = null,

	@field:SerializedName("Amount")
	val amount: Double? = null,

	@field:SerializedName("IsGST")
	val isGST: String? = null,

	@field:SerializedName("BreakUpAmount")
	val breakUpAmount: Double? = null,

	@field:SerializedName("GSTAmount")
	val gstAmount: Double? = null,

	@field:SerializedName("Token")
	val token: String? = null
) : Parcelable
