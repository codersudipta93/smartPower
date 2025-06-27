package com.example.parkingagent.data.remote.models.GuestRegistration

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class GuestRechargeReqBody(

	@field:SerializedName("EntityId")
	val entityId: String? = null,


	@field:SerializedName("CardNo")
	val CardNo: String? = null,


	@field:SerializedName("CardExpiryDate")
	val CardExpiryDate: String? = null,



	@field:SerializedName("rechargeAmount")
	val rechargeAmount: String? = null,

	@field:SerializedName("UserId")
	val UserId: String? = null,


) : Parcelable

