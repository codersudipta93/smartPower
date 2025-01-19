package com.example.parkingagent.UI.fragments.ParkedVehicle

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.parkingagent.pagging.VehiclePagingSource
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.ParkingApis
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleItem
import com.example.parkingagent.utils.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class ParkedVehicleViewModel @Inject constructor(
     private val client: ParkingApis,
     private val sharedPreferenceManager: SharedPreferenceManager,
     @ApplicationContext private val context: Context
) : ViewModel() {

     // Flow of PagingData to observe in Fragment
     val vehicleListFlow: Flow<PagingData<VehicleItem>> = Pager(
          config = PagingConfig(
               pageSize = 10,
               enablePlaceholders = false
          ),
          pagingSourceFactory = {
               VehiclePagingSource(client, Utils.getDeviceId(context=context))
          }
     ).flow
}
