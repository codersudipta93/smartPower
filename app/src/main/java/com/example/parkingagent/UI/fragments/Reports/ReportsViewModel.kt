
package com.example.parkingagent.UI.fragments.Reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parkingagent.data.local.SharedPreferenceManager

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
class ReportsViewModel @Inject constructor(
    private val sharedPreferenceManager: SharedPreferenceManager,
    @ApplicationContext private val context: Context
) : ViewModel() {


}