package com.example.parkingagent.utils

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.AppAuthApi
import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationResponse
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleListReqBody
import com.example.parkingagent.data.remote.models.Menu.MenuDataItem
import com.example.parkingagent.data.remote.models.Menu.MenuRequest
import com.example.parkingagent.data.remote.models.Menu.MenuResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

import android.content.SharedPreferences
import org.json.JSONObject

@HiltViewModel
class SharedViewModel @Inject constructor(
    private val client: AppAuthApi,
    private val sharedPreferenceManager: SharedPreferenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _heartBeatResponse = MutableLiveData<Result<DeviceActivationResponse>>()
    val heartBeatResponse: LiveData<Result<DeviceActivationResponse>> = _heartBeatResponse

    // New menu handling code
    // Keep only one menu data property
    private val _menuItems = MutableLiveData<List<MenuDataItem>>()
    val menuItems: LiveData<List<MenuDataItem>> = _menuItems

    // Simplified menu loading
    fun loadMenu() {
        val deviceId = Utils.getDeviceId(context)
        val entityId = sharedPreferenceManager.getEntityId().toString()

        client.getMenu(MenuRequest(entityId, deviceId)).enqueue(object : Callback<MenuResponse> {
            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                if (response.isSuccessful) {
                    Log.d("MenuResponse",response.body()?.slipHF?.Header1.toString())

                    val slipHF = JSONObject().apply {
                        put("Header1", response.body()?.slipHF?.Header1.toString())
                        put("Header2", response.body()?.slipHF?.Header2.toString())
                        put("Footer1", response.body()?.slipHF?.Footer1.toString())
                        put("Footer2", response.body()?.slipHF?.Footer2.toString())
                    }
                    sharedPreferenceManager.saveSlipHeaderFooter(slipHF.toString())
                    Log.d("Current App version", response.body()?.currentAppVersion.toString())
                    sharedPreferenceManager.setCurrentAppVersion(response.body()?.currentAppVersion.toString())
                    val menuResponse = response.body() ?: MenuResponse()
                    //Log.d("MenuResponse",menuResponse.data.toString())
                    _menuItems.postValue(menuResponse.data?.filterNotNull() ?: emptyList())
                } else {
                    _menuItems.postValue(emptyList())
                    Log.e("SharedViewModel", "Menu API error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                _menuItems.postValue(emptyList())
                Log.e("SharedViewModel", "Menu API failure", t)
            }
        })
    }




    sealed class MenuLoadingState {
        object Loading : MenuLoadingState()
        object Success : MenuLoadingState()
        class Error(val message: String) : MenuLoadingState()
    }

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
