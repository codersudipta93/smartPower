package com.example.parkingagent.data.remote.api

import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationReqBody
import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AppAuthApi {

    @POST("Device/DeviceActivation")
     fun activateDevice(@Body deviceActivationReqBody: DeviceActivationReqBody): Call<DeviceActivationResponse>

}