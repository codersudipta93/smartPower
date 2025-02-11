package com.example.parkingagent.UI.fragments.LoginFragment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.AppAuthApi
import com.example.parkingagent.data.remote.models.AgentLogin.AgentLoginReqBody
import com.example.parkingagent.data.remote.models.AgentLogin.AgentLoginResponseBody
import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationReqBody
import com.example.parkingagent.data.remote.models.DeviceActivationModel.DeviceActivationResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val client: AppAuthApi,
    private val sessionManager: SharedPreferenceManager
):ViewModel() {

    private val _mutualSharedflow= MutableSharedFlow<LoginEvents>()
    val mutualSharedflow: SharedFlow<LoginEvents> = _mutualSharedflow


    fun callActivationApi(activationCode: String,deviceId:String,deviceIMEINumber:String) {
        val requestBody = DeviceActivationReqBody(deviceId,"2",deviceIMEINumber,activationCode)


            val activationCall= client.activateDevice(requestBody)
            activationCall.enqueue(object : Callback<DeviceActivationResponse> {

                override fun onResponse(
                    call: Call<DeviceActivationResponse>,
                    response: Response<DeviceActivationResponse>
                ) {
//                    LoginEvents.ActivationCodeSuccess(response.body()!!)
                    viewModelScope.launch {
                        if(response.isSuccessful && response.body()!=null && response.body()?.status==true){
                            sessionManager.setEntityId(response.body()?.entityId?:0)
                            _mutualSharedflow.emit(LoginEvents.ActivationCodeSuccess(response.body()!!))
                        }
                        else{
                            _mutualSharedflow.emit(LoginEvents.ActivationCodeFailed(response.message() ?:"Something went wrong"))
                        }

                    }


                }

                override fun onFailure(call: Call<DeviceActivationResponse>, t: Throwable) {

                    viewModelScope.launch {
                        _mutualSharedflow.emit(LoginEvents.ActivationCodeFailed(t.message ?:"Something went wrong"))
                    }

                }

            })



    }

    fun callAgentLogin(email:String,password:String,deviceId: String){
        val reqBody=AgentLoginReqBody(email,deviceId,password)

        val loginCall=client.agentLogin(reqBody)

        loginCall.enqueue(object : Callback<AgentLoginResponseBody>{
            override fun onResponse(
                call: Call<AgentLoginResponseBody>,
                response: Response<AgentLoginResponseBody>
            ) {

                viewModelScope.launch {
                    if (response.body()?.status==true){
                        _mutualSharedflow.emit(LoginEvents.LoginSuccess(response.body()!!))
                        if (sessionManager.getEntityId()!=0){

                        }

                    }
                    else {
                        _mutualSharedflow.emit(LoginEvents.ActivationCodeFailed(response.body()?.msg ?:"Something went wrong"))
                    }
                }

            }

            override fun onFailure(call: Call<AgentLoginResponseBody>, t: Throwable) {

                viewModelScope.launch {
                    _mutualSharedflow.emit(LoginEvents.ActivationCodeFailed(t.message ?:"Something went wrong"))
                }
            }

        })
    }


    sealed class LoginEvents {
        class LoginSuccess(data: AgentLoginResponseBody) : LoginEvents()

        class ActivationCodeSuccess(data: DeviceActivationResponse) : LoginEvents()

        class ActivationCodeFailed(val message: String) : LoginEvents()
    }

}