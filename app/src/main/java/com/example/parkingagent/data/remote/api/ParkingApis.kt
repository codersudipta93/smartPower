package com.example.parkingagent.data.remote.api

import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleListReqBody
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleListResponse
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingReqBody
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ParkingApis {

@POST("Device/VehicleParking")
fun vehicleParking(@Body vehicleParkingReqBody: VehicleParkingReqBody): Call<VehicleParkingResponse>


@POST("Device/VehicleInTodayButNotOut")
suspend fun getVehicleList(@Body vehicleListReqBody: VehicleListReqBody): Response<VehicleListResponse>


}