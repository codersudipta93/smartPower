package com.example.parkingagent.data.remote.api

import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleListReqBody
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleListResponse
import com.example.parkingagent.data.remote.models.GuestRegistration.GuestRegistrationReqBody
import com.example.parkingagent.data.remote.models.GuestRegistration.GuestRegistrationResponse
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingCardReqBody
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingCardResponse
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingReqBody
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ParkingApis {

@POST("Device/VehicleParking")
fun vehicleParking(@Body vehicleParkingReqBody: VehicleParkingReqBody): Call<VehicleParkingResponse>

@POST("Device/VehicleParkingCard")
fun vehicleParkingCard(@Body vehicleParkingReqBody: VehicleParkingCardReqBody): Call<VehicleParkingCardResponse>


@POST("Device/VehicleInTodayButNotOut")
suspend fun getVehicleList(@Body vehicleListReqBody: VehicleListReqBody): Response<VehicleListResponse>

@POST("Device/GuestRegistration")
fun guestRegistration(@Body guestRegistrationReqBody: GuestRegistrationReqBody): Call<GuestRegistrationResponse>



}