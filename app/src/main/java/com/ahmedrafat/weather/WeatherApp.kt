package com.ahmedrafat.weather

import android.app.Application
import com.ahmedrafat.weather.di.AppModule.provideApiService
import com.ahmedrafat.weather.model.ApiService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeatherApp : Application(){
}