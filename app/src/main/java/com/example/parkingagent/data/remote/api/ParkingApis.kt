package com.example.parkingagent.data.remote.api

import com.example.parkingagent.data.remote.models.CollectionInsert.CollectionInsertReqBody
import com.example.parkingagent.data.remote.models.CollectionInsert.CollectionInsertResponse
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleListReqBody
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleListResponse
import com.example.parkingagent.data.remote.models.GuestRegistration.GuestRegistrationReqBody
import com.example.parkingagent.data.remote.models.GuestRegistration.GuestRechargeReqBody
import com.example.parkingagent.data.remote.models.GuestRegistration.GuestRegistrationResponse
import com.example.parkingagent.data.remote.models.ParkingRate.ParkingRateReqBody
import com.example.parkingagent.data.remote.models.ParkingRate.ParkingRateResponse
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingCardReqBody
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingCardResponse
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingReqBody
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingResponse
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleSearchParkingReqBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ParkingApis {

@POST("Device/VehicleParking")
fun vehicleParking(@Header("Authorization") token:String,@Body vehicleParkingReqBody: VehicleParkingReqBody): Call<VehicleParkingResponse>


    @POST("Device/VehicleParkingOutUsingVehcileNo")
fun vehicleSearchAndParking(@Header("Authorization") token:String,@Body searchVehicleReqBody:VehicleSearchParkingReqBody): Call<VehicleParkingResponse>


@POST("Device/VehicleParkingCard")
fun vehicleParkingCard(@Header("Authorization") token:String,@Body vehicleParkingReqBody: VehicleParkingCardReqBody): Call<VehicleParkingCardResponse>


@POST("Device/VehicleInTodayButNotOut")
suspend fun getVehicleList(@Body vehicleListReqBody: VehicleListReqBody): Response<VehicleListResponse>

@POST("Device/GuestRegistration")
fun guestRegistration(@Header("Authorization") token:String,@Body guestRegistrationReqBody: GuestRegistrationReqBody): Call<GuestRegistrationResponse>

@POST("Device/GuestCardRecharge")
fun guestRecharge(@Header("Authorization") token:String,@Body GuestRechargeReqBody: GuestRechargeReqBody): Call<GuestRegistrationResponse>

@POST("Device/CollectionInsert")
fun collectionInsert(@Header("Authorization") token:String,@Body collectionReqBody: CollectionInsertReqBody): Call<CollectionInsertResponse>

@POST("Device/GetRateByEntity")
fun getRateByEntity(@Body parkingRateResponse: ParkingRateReqBody): Call<ParkingRateResponse>


}