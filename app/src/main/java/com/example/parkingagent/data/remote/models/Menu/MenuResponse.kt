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
	val data: List<DataItem?>? = null
) : Parcelable

@Parcelize
data class DataItem(

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
