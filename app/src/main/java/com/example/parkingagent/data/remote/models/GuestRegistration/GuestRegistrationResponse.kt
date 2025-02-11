package com.example.parkingagent.data.remote.models.GuestRegistration

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class GuestRegistrationResponse(

	@field:SerializedName("EntityId")
	val entityId: Int? = null,

	@field:SerializedName("Msg")
	val msg: String? = null,

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("ContactNo")
	val contactNo: String? = null,

	@field:SerializedName("CardNo")
	val cardNo: String? = null,

	@field:SerializedName("VehicleNo")
	val vehicleNo: String? = null,

	@field:SerializedName("GuestId")
	val guestId: Int? = null,

	@field:SerializedName("GuestName")
	val guestName: String? = null,

	@field:SerializedName("Balance")
	val balance: Double? = null
) : Parcelable
