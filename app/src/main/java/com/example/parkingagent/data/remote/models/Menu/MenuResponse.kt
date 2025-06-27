package com.example.parkingagent.data.remote.models.Menu

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class MenuResponse(

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("Data")
	val data: List<MenuDataItem?>? = null,

	@field:SerializedName("slipHF")
	val slipHF: HFData ? = null,


	@field:SerializedName("AppVersion")
	val currentAppVersion: String ? = null,

	) : Parcelable

@Parcelize
data class MenuDataItem(

	@field:SerializedName("Status")
	val status: Boolean? = null,

	@field:SerializedName("msg")
	val msg: String? = null,

	@field:SerializedName("MenuName")
	val menuName: String? = null,

	@field:SerializedName("EntityId")
	val entityId: Int? = null,

	@field:SerializedName("ParentId")
	val parentId: Int? = null,

	@field:SerializedName("IOType")
	val iOType: String? = null,

	@field:SerializedName("DeviceId")
	val deviceId: String? = null,

	@field:SerializedName("icon")
	val icon: String? = null,

	@field:SerializedName("AppMenuId")
	val appMenuId: Int? = null
) : Parcelable


@Parcelize
data class HFData(

	@field:SerializedName("Header1")
	val Header1: String? = null,

	@field:SerializedName("Header2")
	val Header2: String? = null,

	@field:SerializedName("Footer1")
	val Footer1: String? = null,

	@field:SerializedName("Footer2")
	val Footer2: String? = null,

) : Parcelable
