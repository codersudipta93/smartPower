package com.example.parkingagent.UI.fragments.parkingRate

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.ParkingApis
import com.example.parkingagent.data.remote.models.ParkingRate.ParkingRateDataItem
import com.example.parkingagent.data.remote.models.ParkingRate.ParkingRateReqBody
import com.example.parkingagent.data.remote.models.ParkingRate.ParkingRateResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@HiltViewModel
class ParkingRateViewModel @Inject constructor(
    private val parkingApis: ParkingApis,
    private val sharedPreferenceManager: SharedPreferenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _mutualSharedflow= MutableSharedFlow<GetParkingRateEvents>()
    val mutualSharedflow: SharedFlow<GetParkingRateEvents> = _mutualSharedflow

    fun getParkingRate() {
        val entityId = sharedPreferenceManager.getEntityId()
        val reqBody= ParkingRateReqBody(entityId.toInt(),sharedPreferenceManager.getUserId(),
            sharedPreferenceManager.getAccessToken()?.drop(7)
        )

        val call=parkingApis.getRateByEntity(reqBody)

        call.enqueue(object : Callback<ParkingRateResponse>{
            override fun onResponse(call: Call<ParkingRateResponse?>, response: Response<ParkingRateResponse?>) {

                viewModelScope.launch {
                    if (response.isSuccessful && response.body()?.status==true && !response.body()?.data.isNullOrEmpty()){
                        _mutualSharedflow.emit(GetParkingRateEvents.GetParkingRateSuccessful(
                            response.body()?.data
                        ))
                    }
                    else{
                        _mutualSharedflow.emit(GetParkingRateEvents.GetParkingRateFailed(response.body()?.msg?:"Unknown Error"))
                    }
                }

            }

            override fun onFailure(call: Call<ParkingRateResponse?>, t: Throwable) {
                viewModelScope.launch {
                    _mutualSharedflow.emit(GetParkingRateEvents.GetParkingRateFailed(t.message?:"Unknown Error"))
                }

            }

        })

    }


    sealed class GetParkingRateEvents {
        class GetParkingRateSuccessful(val parkingRateDataItem: List<ParkingRateDataItem?>?) : GetParkingRateEvents()
        class GetParkingRateFailed(val message: String) : GetParkingRateEvents()
    }
}