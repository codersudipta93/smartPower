package com.example.parkingagent.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.AppAuthApi
import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationResponse
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleListReqBody
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class SharedViewModel @Inject constructor(
    private val client: AppAuthApi,
    private val sharedPreferenceManager: SharedPreferenceManager
) : ViewModel() {

    private val _heartBeatResponse = MutableLiveData<Result<DeviceActivationResponse>>()
    val heartBeatResponse: LiveData<Result<DeviceActivationResponse>> = _heartBeatResponse

    fun callHeartBeatApi(deviceId: String) {
        val requestBody = VehicleListReqBody(deviceId, 1)
        client.getDeviceHeartBeat(requestBody).enqueue(object : Callback<DeviceActivationResponse> {
            override fun onResponse(
                call: Call<DeviceActivationResponse>,
                response: Response<DeviceActivationResponse>
            ) {
                if (response.isSuccessful) {
                    _heartBeatResponse.postValue(Result.success(response.body()) as Result<DeviceActivationResponse>?)
                } else {
                    _heartBeatResponse.postValue(Result.failure(Exception(response.message())))
                }
            }

            override fun onFailure(call: Call<DeviceActivationResponse>, t: Throwable) {
                _heartBeatResponse.postValue(Result.failure(t))
            }
        })
    }
}
