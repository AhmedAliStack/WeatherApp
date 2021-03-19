package com.ahmedrafat.weather.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ahmedrafat.weather.R
import com.ahmedrafat.weather.utils.CheckNetwork
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register Callback
        val network = CheckNetwork(applicationContext)
        network.registerNetworkCallback()
    }
}