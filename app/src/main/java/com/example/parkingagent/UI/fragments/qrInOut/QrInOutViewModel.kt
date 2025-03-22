package com.example.parkingagent.UI.fragments.qrInOut

import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.ParkingApis
import com.example.parkingagent.data.remote.models.CollectionInsert.CollectionInsertData
import com.example.parkingagent.data.remote.models.CollectionInsert.CollectionInsertReqBody
import com.example.parkingagent.data.remote.models.CollectionInsert.CollectionInsertResponse
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingReqBody
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject


@HiltViewModel
class QrInOutViewModel @Inject constructor(
    val client:ParkingApis,
    val sharedPreferenceManager: SharedPreferenceManager
) : ViewModel() {

    private val _mutualSharedflow= MutableSharedFlow<ParkingVehicleEvents>()
    val mutualSharedflow: SharedFlow<ParkingVehicleEvents> = _mutualSharedflow

    fun parkedVehicle(vehicleNumber:String,deviceId:String){
        val vehicleParkingReqqBody= VehicleParkingReqBody(sharedPreferenceManager.getUserId(),"1",vehicleNumber, "2",deviceId,"QR")
        val parkingVehicleCall=client.vehicleParking(sharedPreferenceManager.getAccessToken().toString(),vehicleParkingReqqBody)

        parkingVehicleCall.enqueue(object: Callback<VehicleParkingResponse> {
            override fun onResponse(
                call: Call<VehicleParkingResponse>,
                response: Response<VehicleParkingResponse>
            ) {
                viewModelScope.launch {
                    if (response.isSuccessful && response.body()?.status ==true){
                        _mutualSharedflow.emit(ParkingVehicleEvents.VehicleParkingSuccessful(response.body()!!))
                    }
                    else {
                        _mutualSharedflow.emit(ParkingVehicleEvents.VehicleParkingFailed(response.body()?.msg?:"Unknown Error"))
                    }
                }

            }

            override fun onFailure(call: Call<VehicleParkingResponse>, t: Throwable) {
                viewModelScope.launch {
                    _mutualSharedflow.emit(ParkingVehicleEvents.VehicleParkingFailed(t.message?:"Unknown Error"))
                }
            }

        })


    }

    fun collectionInsert(vehicleNumber:String,amount: Double){
        val reqBody= CollectionInsertReqBody(sharedPreferenceManager.getEntityId(),vehicleNumber,sharedPreferenceManager.getUserId(),amount)
        val collectionInsertCall=client.collectionInsert(sharedPreferenceManager.getAccessToken().toString(),reqBody)

        collectionInsertCall.enqueue(object: Callback<CollectionInsertResponse> {
            override fun onResponse(
                call: Call<CollectionInsertResponse>,
                response: Response<CollectionInsertResponse>
            ) {
                viewModelScope.launch {
                    if (response.isSuccessful && response.body()?.status ==true) {
                        _mutualSharedflow.emit(ParkingVehicleEvents.CollectionInsertSuccessful(response.body()!!.collectionInsertData!!))
                    }
                    else {
                        _mutualSharedflow.emit(ParkingVehicleEvents.VehicleParkingFailed(response.body()?.msg?:"Unknown Error"))
                    }
                }
            }
            override fun onFailure(call: Call<CollectionInsertResponse>, t: Throwable) {
                viewModelScope.launch {
                    _mutualSharedflow.emit(ParkingVehicleEvents.VehicleParkingFailed(t.message?:"Unknown Error"))
                }
            }

        })
    }



    sealed class ParkingVehicleEvents {
        class VehicleParkingSuccessful(val vehicleParkingResponse: VehicleParkingResponse):ParkingVehicleEvents()

        class VehicleParkingFailed(val message:String):ParkingVehicleEvents()

        class CollectionInsertSuccessful(val collectionInsertData: CollectionInsertData):ParkingVehicleEvents()
    }
}