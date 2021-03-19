package com.ahmedrafat.weather.utils

import android.content.Context
import android.net.*
import android.net.ConnectivityManager.NetworkCallback
import android.os.Build
import androidx.annotation.RequiresApi
import com.ahmedrafat.weather.model.isNetworkConnected
import javax.inject.Inject


class CheckNetwork @Inject constructor(private val context: Context) {

    val connectivityManager =
    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    // Network Check
    fun registerNetworkCallback() {
        try {

            val builder = NetworkRequest.Builder()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(object : NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        isNetworkConnected = true // Global Static Variable
                    }

                    override fun onLost(network: Network) {
                        isNetworkConnected = false // Global Static Variable
                    }
                }
                )
            }else{
                connectivityManager.registerNetworkCallback(builder.build(),object:NetworkCallback(){
                    override fun onAvailable(network: Network) {
                        super.onAvailable(network)
                        isNetworkConnected = true // Global Static Variable
                    }

                    override fun onLost(network: Network) {
                        super.onLost(network)
                        isNetworkConnected = false // Global Static Variable
                    }
                })
            }
            isNetworkConnected = false
        } catch (e: Exception) {
            isNetworkConnected = false
        }
    }
}