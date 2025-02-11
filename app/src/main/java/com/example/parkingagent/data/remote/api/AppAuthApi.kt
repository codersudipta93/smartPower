package com.example.parkingagent.data.remote.api

import com.example.parkingagent.data.remote.models.AgentLogin.AgentLoginReqBody
import com.example.parkingagent.data.remote.models.AgentLogin.AgentLoginResponseBody
import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationReqBody
import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationResponse
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleListReqBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AppAuthApi {

    @POST("Device/DeviceActivation")
     fun activateDevice(@Body deviceActivationReqBody: DeviceActivationReqBody): Call<DeviceActivationResponse>

     @POST("Device/GetDeviceHeartBeat")
     fun getDeviceHeartBeat(@Body vehicleListReqBody: VehicleListReqBody):Call<DeviceActivationResponse>

     @POST("Device/AgentLogin")
     fun agentLogin(@Body agentLoginReqBody: AgentLoginReqBody):Call<AgentLoginResponseBody>

}