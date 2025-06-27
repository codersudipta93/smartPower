package com.example.parkingagent.data.remote.api

import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationReqBody
import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationResponse
import com.example.parkingagent.data.remote.models.anprDataResponse.ANPRVehicleResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LocalNetworkApis {

    @POST("Smartpower/")
    fun getANPRVehicle(): Call<ANPRVehicleResponse>
}