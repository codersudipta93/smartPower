package com.example.parkingagent.UI.fragments.nfcFragment

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dotlottie.dlplayer.Event
import com.example.parkingagent.UI.fragments.LoginFragment.LoginViewModel.LoginEvents
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.ParkingApis
import com.example.parkingagent.data.remote.models.GuestRegistration.GuestRegistrationReqBody
import com.example.parkingagent.data.remote.models.GuestRegistration.GuestRechargeReqBody
import com.example.parkingagent.data.remote.models.GuestRegistration.GuestRegistrationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class NfcViewModel @Inject constructor(
    val sessionManager: SharedPreferenceManager,
    val client:ParkingApis
):ViewModel() {
    private val _mutualSharedflow= MutableSharedFlow<GuestEvents>()
    val mutualSharedflow: SharedFlow<GuestEvents> = _mutualSharedflow

    fun registerGuest(name:String, contactNumber:String, vehicleNumber:String, expiryDate: String, cardNumber:String, VehicleTypeId:Int, RechargeAmount:String, CompanyName:String){

        val reqBody=GuestRegistrationReqBody(sessionManager.getEntityId().toString(),contactNumber,cardNumber,vehicleNumber,name,expiryDate,true,VehicleTypeId,RechargeAmount,CompanyName)
        val guestRegistrationCall=client.guestRegistration(sessionManager.getAccessToken().toString(),reqBody)

        guestRegistrationCall.enqueue(object : Callback<GuestRegistrationResponse> {
            override fun onResponse(
                call: Call<GuestRegistrationResponse>,
                response: Response<GuestRegistrationResponse>
            ) {
                viewModelScope.launch {
                    Log.d("res", response.body().toString())
                    if (response.body()?.status ==true){

                        _mutualSharedflow.emit(GuestEvents.GuestRegistered(response.body()!!))
                    }
                    else{
                        _mutualSharedflow.emit(GuestEvents.Error(response.body()?.msg ?:"Something went wrong"))
                    }
                }
            }

            override fun onFailure(call: Call<GuestRegistrationResponse>, t: Throwable) {
                viewModelScope.launch {
                    _mutualSharedflow.emit(GuestEvents.Error(t.message ?:"Something went wrong"))
                }
            }
        })
    }


    fun doRecharge(
        CardNo: String,
        CardExpiryDate: String,
        RechargeAmount: String,
    ) {
        val bodyParam = GuestRechargeReqBody(
           sessionManager.getEntityId().toString(),
            CardNo,
            CardExpiryDate,
            RechargeAmount,
            sessionManager.getUserId().toString()
        )
        val rechargeAPICall=client.guestRecharge(sessionManager.getAccessToken().toString(),bodyParam)

        rechargeAPICall.enqueue(object : Callback<GuestRegistrationResponse> {
            override fun onResponse(
                call: Call<GuestRegistrationResponse>,
                response: Response<GuestRegistrationResponse>
            ) {
                viewModelScope.launch {
                    Log.d("RechargeRes", response.body().toString())
                    if (response.body()?.status == true) {
                        _mutualSharedflow.emit(GuestEvents.GuestRegistered(response.body()!!))
                    } else {
                        _mutualSharedflow.emit(GuestEvents.Error(response.body()?.msg ?: "Recharge failed"))
                    }
                }
            }

            override fun onFailure(call: Call<GuestRegistrationResponse>, t: Throwable) {
                viewModelScope.launch {
                    _mutualSharedflow.emit(GuestEvents.Error(t.message ?: "Recharge failed"))
                }
            }
        })
    }



//    sealed class GuestEvents{
//       class GuestRegistered(data: GuestRegistrationResponse):GuestEvents()
//       class Error(val message:String):GuestEvents()
//   }

    sealed class GuestEvents {
        data class GuestRegistered(val data: GuestRegistrationResponse) : GuestEvents()
        data class Error(val message: String) : GuestEvents()
    }

}