package com.ahmedrafat.weather.ui

import android.Manifest
import android.content.Context
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

    private lateinit var locationManager:LocationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //handle location permission
        handlePermission()

        // Register Network Callback
        val network = CheckNetwork(applicationContext)
        network.registerNetworkCallback()


    }

    private fun handlePermission() {
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locPermission()
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locPermission()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION
                )
            } else
                startService(Intent(this, LocationGetter::class.java))
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION -> {
                if (grantResults.isEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.location_permission),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    //open location dialog
    private fun locPermission() {
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
        val mLocationSettingsRequest: LocationSettingsRequest
        builder.addLocationRequest(
            LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        )
        builder.setAlwaysShow(true)
        mLocationSettingsRequest = builder.build()
        val mSettingsClient: SettingsClient = LocationServices.getSettingsClient(this)
        mSettingsClient
            .checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener { locationSettingsResponse ->
                startService(Intent(this, LocationGetter::class.java))
            }
            .addOnFailureListener { e ->
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae: ResolvableApiException = e as ResolvableApiException
                        rae.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                    } catch (sie: Exception) {
                        Log.e("GPS", "Unable to execute request.")
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.e(
                        "GPS",
                        "Location settings are inadequate, and cannot be fixed here. Fix in Settings."
                    )
                }
            }
            .addOnCanceledListener { Log.e("GPS", "checkLocationSettings -> onCanceled") }
    }
}