package com.example.parkingagent.UI.fragments.home

import androidx.lifecycle.ViewModel
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.data.remote.api.ParkingApis
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    client:ParkingApis,
    sharedPreferenceManager: SharedPreferenceManager
):ViewModel(){



}