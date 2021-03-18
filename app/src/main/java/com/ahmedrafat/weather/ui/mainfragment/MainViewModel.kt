package com.ahmedrafat.weather.ui.mainfragment

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import androidx.work.WorkInfo
import com.ahmedrafat.weather.WeatherApp
import com.ahmedrafat.weather.model.ApiService
import com.ahmedrafat.weather.model.apimodel.WeatherModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @ViewModelInject constructor(private val client: Retrofit): ViewModel() {
    //Variables LifeData
    val weatherModelMutableLiveData: MutableLiveData<WeatherModel> = MutableLiveData()
    val errorMutableLiveData: MutableLiveData<String> = MutableLiveData()
    val loading :MutableLiveData<Boolean> = MutableLiveData()

    private val apiClient: ApiService = client.create(ApiService::class.java)

    //Deferred to Receive Response
    private lateinit var deferred: Deferred<Response<WeatherModel>>

    //request function
    fun getWeatherData(city:String){
        loading.value = true

        //call api from Background
        viewModelScope.launch(Dispatchers.IO) {
            try{
                deferred = apiClient.getCityWeather(city)
                val response = deferred.await()
                //handle response on the main thread
                viewModelScope.launch(Dispatchers.Main) {
                    loading.value = false
                    weatherModelMutableLiveData.setValue(response.body())
                }
            }catch (e:Exception){
                //handle exception on the main thread
                viewModelScope.launch(Dispatchers.Main) {
                    loading.value = false
                    weatherModelMutableLiveData.value = null
                    errorMutableLiveData.value = e.localizedMessage
                }
            }
        }
    }
}