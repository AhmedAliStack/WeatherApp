package com.ahmedrafat.weather.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ahmedrafat.weather.R
import com.ahmedrafat.weather.model.REQUEST_CHECK_SETTINGS
import com.ahmedrafat.weather.model.REQUEST_LOCATION
import com.ahmedrafat.weather.utils.CheckNetwork
import com.ahmedrafat.weather.utils.LocationGetter
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Register Network Callback
        val network = CheckNetwork(applicationContext)
        network.registerNetworkCallback()


    }
}