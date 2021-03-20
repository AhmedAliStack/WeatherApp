package com.ahmedrafat.weather.ui.mainfragment

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmedrafat.weather.model.ApiService
import com.ahmedrafat.weather.model.EMPTY_VALIDATION
import com.ahmedrafat.weather.model.INTERNET_VALIDATION
import com.ahmedrafat.weather.model.apimodel.WeatherModel
import com.ahmedrafat.weather.model.isNetworkConnected
import com.ahmedrafat.weather.utils.convertError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit

@HiltViewModel
class MainViewModel @ViewModelInject constructor(private val client: Retrofit) : ViewModel() {
    //Variables LifeData
    val weatherModelMutableLiveData: MutableLiveData<WeatherModel> = MutableLiveData()
    val errorMutableLiveData: MutableLiveData<String> = MutableLiveData()
    val loading: MutableLiveData<Boolean> = MutableLiveData()

    private val apiClient: ApiService = client.create(ApiService::class.java)

    //Deferred to Receive Response
    private lateinit var deferred: Deferred<Response<WeatherModel>>

    //request function
    fun getWeatherData(city: String) {
        if (city.trim().isNotEmpty()) {
            if(isNetworkConnected) {
                loading.value = true

                //call api from Background
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        deferred = apiClient.getCityWeather(city)
                        val response = deferred.await()
                        //handle response on the main thread
                        viewModelScope.launch(Dispatchers.Main) {
                            loading.value = false
                            if (response.code() == 200)
                            //pass data to ui layer
                                weatherModelMutableLiveData.setValue(response.body())
                            else
                            //parse error from api
                                errorMutableLiveData.value =
                                    response.errorBody()?.let { convertError(it, client)?.message }
                        }
                    } catch (e: Exception) {
                        //handle exception on the main thread
                        viewModelScope.launch(Dispatchers.Main) {
                            loading.value = false
                            weatherModelMutableLiveData.value = null
                            errorMutableLiveData.value = e.localizedMessage
                        }
                    }
                }
            }else{
                errorMutableLiveData.value = INTERNET_VALIDATION
            }
        } else {
            errorMutableLiveData.value = EMPTY_VALIDATION
        }
    }
}