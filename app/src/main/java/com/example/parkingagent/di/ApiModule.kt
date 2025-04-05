package com.example.parkingagent

import com.example.parkingagent.data.remote.api.AppAuthApi
import com.example.parkingagent.data.remote.api.LocalNetworkApis
import com.example.parkingagent.data.remote.api.ParkingApis
import com.example.parkingagent.di.PrimaryRetrofit
import com.example.parkingagent.di.SecondaryRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideAuthApiService(
        @PrimaryRetrofit retrofit: Retrofit
    ): AppAuthApi = retrofit.create(AppAuthApi::class.java)

    @Provides
    @Singleton
    fun provideParkingApiService(
        @PrimaryRetrofit retrofit: Retrofit
    ): ParkingApis = retrofit.create(ParkingApis::class.java)

    // New API service using the dynamic base URL.
    @Provides
    @Singleton
    fun provideNewApiService(
        @SecondaryRetrofit retrofit: Retrofit
    ): LocalNetworkApis = retrofit.create(LocalNetworkApis::class.java)
}