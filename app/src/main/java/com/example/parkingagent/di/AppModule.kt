package com.dev.tnevi.di

import android.content.Context
import com.example.parkingagent.data.local.SharedPreferenceManager
import com.example.parkingagent.di.PrimaryRetrofit
import com.example.parkingagent.di.SecondaryRetrofit

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

import android.util.Log

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    // Primary Retrofit instance for your default API
    @Singleton
    @Provides
    @PrimaryRetrofit
    fun providePrimaryRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://103.231.40.240/SmartPowerAPI/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

//    // Secondary Retrofit instance using dynamic base URL from shared preferences.
//    @Singleton
//    @Provides
//    @SecondaryRetrofit
//    fun provideSecondaryRetrofit(
//        okHttpClient: OkHttpClient,
//        sharedPreferenceManager: SharedPreferenceManager
//    ): Retrofit {
//        // Retrieve IP and port from shared preferences
//        val ip = sharedPreferenceManager.getIpAddress()
//        val port = sharedPreferenceManager.getPort()
//        Log.d("IP Address", sharedPreferenceManager.getIpAddress().toString(), null)
//        // You might want to provide a default or throw an error if null.
//        val baseUrl = if (!ip.isNullOrBlank() && !port.isNullOrBlank()) {
//            "http://$ip:$port/"
//        } else {
//            // Fallback URL (or you may throw an exception)
//            "http://default.example.com/"
//        }
//
//        return Retrofit.Builder()
//            .baseUrl(baseUrl)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(okHttpClient)
//            .build()
//    }


    // Secondary Retrofit instance using dynamic base URL from shared preferences.
    @Singleton
    @Provides
    @SecondaryRetrofit
    fun provideSecondaryRetrofit(
        sharedPreferenceManager: SharedPreferenceManager
    ): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            // 🔹 Interceptor to dynamically update IP & port for every request
            .addInterceptor { chain ->
                val ip = sharedPreferenceManager.getIpAddress()
                val port = sharedPreferenceManager.getPort()

                val newUrl = chain.request().url.newBuilder()
                    .scheme("http")
                    .host(ip ?: "default.example.com")
                    .port(port?.toIntOrNull() ?: 80)
                    .build()

                Log.d("DynamicBaseUrl", "Using IP: $ip, Port: $port")
                chain.proceed(chain.request().newBuilder().url(newUrl).build())
            }
            .build()

        return Retrofit.Builder()
            .baseUrl("http://default.example.com/") // Placeholder — will be replaced dynamically
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideSharedPrefManager(context: Context): SharedPreferenceManager {
        return SharedPreferenceManager(context)
    }
}
