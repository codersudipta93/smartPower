package com.example.parkingagent.UI.fragments.cardInOut

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.ParkingApis
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingCardReqBody
import com.example.parkingagent.data.remote.models.VehicleParking.VehicleParkingCardResponse
import com.example.parkingagent.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class CardInOutViewModel @Inject constructor(
    val sessionManager: SharedPreferenceManager,
    val client: ParkingApis,
    @ApplicationContext private val context: Context
):ViewModel(){
    private val _mutualSharedflow= MutableSharedFlow<VehicleCardEvents>()
     val mutualSharedflow: SharedFlow<VehicleCardEvents> =_mutualSharedflow


    fun parkVehicle(cardNumber:String,iOType:Int=0){
        val currentTime= SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        val reqBody= VehicleParkingCardReqBody(cardNumber,iOType,Utils.getDeviceId(context),sessionManager.getUserId(),currentTime)
        val call=client.vehicleParkingCard(reqBody)

        call.enqueue(object :Callback<VehicleParkingCardResponse>{
            override fun onResponse(
                call: Call<VehicleParkingCardResponse>,
                response: Response<VehicleParkingCardResponse>
            ) {
                viewModelScope.launch {
                    if (response.body()?.status==true){
                        _mutualSharedflow.emit(VehicleCardEvents.VehicleCardParked(response.body()!!))
                    }
                    else {
                        _mutualSharedflow.emit(VehicleCardEvents.VehicleCardError(response.body()?.msg ?:"Something went wrong"))
                    }

                }

            }

            override fun onFailure(call: Call<VehicleParkingCardResponse>, t: Throwable) {

                viewModelScope.launch {
                    _mutualSharedflow.emit(VehicleCardEvents.VehicleCardError(t.message ?:"Something went wrong"))

                }

            }

        })
    }

    sealed class VehicleCardEvents{
        class VehicleCardParked(val data:VehicleParkingCardResponse):VehicleCardEvents()
        class VehicleCardError(val message:String):VehicleCardEvents()

    }

}