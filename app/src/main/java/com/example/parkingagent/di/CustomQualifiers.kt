package com.example.parkingagent.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PrimaryRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SecondaryRetrofit
