package com.example.parkingagent.UI.fragments

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.AppAuthApi
import com.example.parkingagent.data.remote.models.Menu.MenuRequest
import com.example.parkingagent.data.remote.models.Menu.MenuResponse
import com.example.parkingagent.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val repository: AppAuthApi,
    private val sharedPreferenceManager: SharedPreferenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _mutualSharedflow= MutableSharedFlow<GetMenuEvents>()
    val mutualSharedflow: SharedFlow<GetMenuEvents> = _mutualSharedflow

    fun getMenu(){
        val deviceId=Utils.getDeviceId(context)
        val entityId=sharedPreferenceManager.getEntityId()

        val call=repository.getMenu(MenuRequest(entityId.toString(),deviceId))

        call.enqueue(object: Callback<MenuResponse>{
            override fun onResponse(call: Call<MenuResponse>, response: Response<MenuResponse>) {
                viewModelScope.launch {
                    if (response.isSuccessful && response.body()?.status==true){
                        _mutualSharedflow.emit(GetMenuEvents.GetMenuSuccessful(response.body()!!))
                    }
                    else{
                        _mutualSharedflow.emit(GetMenuEvents.GetMenuFailed(response.body()?.msg?:"Unknown Error"))
                    }
                }
            }

            override fun onFailure(call: Call<MenuResponse>, t: Throwable) {
                viewModelScope.launch {
                    _mutualSharedflow.emit(GetMenuEvents.GetMenuFailed(t.message?:"Unknown Error"))
                }
            }

        })


    }

    sealed class GetMenuEvents {
        class GetMenuSuccessful(val menuResponse: MenuResponse):GetMenuEvents()
        class GetMenuFailed(val message:String):GetMenuEvents()

    }

}