package com.example.parkingagent.data.remote.api

import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationReqBody
import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationResponse
import com.example.parkingagent.data.remote.models.anprDataResponse.ANPRVehicleResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface LocalNetworkApis {
    @POST("Smartpower/Vehicle.aspx")
    fun getANPRVehicle(
        @Query("deviceId") deviceId: String,
    ): Call<ANPRVehicleResponse>
}
