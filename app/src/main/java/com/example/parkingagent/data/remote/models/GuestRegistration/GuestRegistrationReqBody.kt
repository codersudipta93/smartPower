package com.example.parkingagent.data.remote.models.GuestRegistration

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class GuestRegistrationReqBody(

	@field:SerializedName("EntityId")
	val entityId: String? = null,

	@field:SerializedName("ContactNo")
	val contactNo: String? = null,

	@field:SerializedName("CardNo")
	val cardNo: String? = null,

	@field:SerializedName("VehicleNo")
	val vehicleNo: String? = null,

	@field:SerializedName("GuestName")
	val guestName: String? = null,

	@field:SerializedName("Balance")
	val balance: String? = null,

	@field:SerializedName("isActive")
	val isActive: Boolean? = false,

	@field:SerializedName("VehicleTypeId")
	val VehicleTypeId: Int? = 1
) : Parcelable
