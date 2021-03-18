package com.ahmedrafat.weather.model

import com.ahmedrafat.weather.model.apimodel.WeatherModel
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("weather")
    fun getCityWeather(@Query("q") city: String , @Query("appid") id: String = APP_ID, @Query("units") unit: String = TEMP_UNIT): Deferred<Response<WeatherModel>>
}