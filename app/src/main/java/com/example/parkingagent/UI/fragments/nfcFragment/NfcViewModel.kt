package com.example.parkingagent.UI.fragments.nfcFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingagent.UI.fragments.LoginFragment.LoginViewModel.LoginEvents
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.ParkingApis
import com.example.parkingagent.data.remote.models.GuestRegistration.GuestRegistrationReqBody
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

    fun registerGuest(name:String,contactNumber:String,vehicleNumber:String,amount:Double,cardNumber:String,VehicleTypeId:Int){

        val reqBody=GuestRegistrationReqBody(sessionManager.getEntityId().toString(),contactNumber,cardNumber,vehicleNumber,name,amount.toString(),true,VehicleTypeId)
        val guestRegistrationCall=client.guestRegistration(reqBody)

        guestRegistrationCall.enqueue(object : Callback<GuestRegistrationResponse> {
            override fun onResponse(
                call: Call<GuestRegistrationResponse>,
                response: Response<GuestRegistrationResponse>
            ) {
                viewModelScope.launch {
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

   sealed class GuestEvents{
       class GuestRegistered(data: GuestRegistrationResponse):GuestEvents()
       class Error(val message:String):GuestEvents()
   }

}