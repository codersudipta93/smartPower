package com.example.parkingagent

import com.example.parkingagent.data.remote.api.AppAuthApi
import com.example.parkingagent.data.remote.api.ParkingApis
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
        retrofit: Retrofit
    ): AppAuthApi = retrofit.create(AppAuthApi::class.java)

    @Provides
    @Singleton
    fun provideParkingApiService(
        retrofit: Retrofit
    ): ParkingApis = retrofit.create(ParkingApis::class.java)



}