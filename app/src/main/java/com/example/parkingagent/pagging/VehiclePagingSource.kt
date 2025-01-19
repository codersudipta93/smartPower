package com.example.parkingagent.pagging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.parkingagent.data.remote.api.ParkingApis
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleItem
import com.example.parkingagent.data.remote.models.GetVehicleList.VehicleListReqBody

class VehiclePagingSource(
    private val apiService: ParkingApis,
    private val deviceId: String
) : PagingSource<Int, VehicleItem>() {

    private var cachedData: List<VehicleItem> = emptyList()  // Cached data
    private var isDataLoaded = false  // Flag to track if data is already loaded

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, VehicleItem> {
        // If data is already loaded, return the cached data
        if (isDataLoaded) {
            return LoadResult.Page(
                data = cachedData,
                prevKey = null,
                nextKey = null // No more data to load since it's a one-time request
            )
        }

        val page = params.key ?: 1

        return try {
            // Make API call to get data for the first time
            val response = apiService.getVehicleList(VehicleListReqBody(deviceId, page))

            if (response.isSuccessful) {
                val vehicleList = response.body()?.data?.filterNotNull().orEmpty()

                // Cache the data and mark it as loaded
                cachedData = vehicleList
                isDataLoaded = true

                LoadResult.Page(
                    data = cachedData,
                    prevKey = null,
                    nextKey = null // No more data to load
                )
            } else {
                // Handle API failure
                Log.e("API Error", "Failed to fetch data: ${response.message()}")
                LoadResult.Error(Exception("API Error"))
            }
        } catch (e: Exception) {
            // Handle exception
            Log.e("API Error", "Exception: ${e.message}")
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, VehicleItem>): Int? {
        return state.anchorPosition
    }
}

