package com.ahmedrafat.weather.utils

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.ahmedrafat.weather.model.LATITUDE
import com.ahmedrafat.weather.model.LONGITUDE
import com.ahmedrafat.weather.model.loc_receiver
import java.util.*


class LocationGetter : Service() {

    private var latitude:Double = 0.0
    private var longitude:Double = 0.0
    private lateinit var mTimer: Timer
    val mHandler: Handler = Handler(Looper.getMainLooper())
    var intent: Intent? = null

    override fun onCreate() {
        super.onCreate()
        //init location update
        getLocationData()

        //sending location updates
        mTimer = Timer()
        mTimer.schedule(object : TimerTask() {
            override fun run() {
                mHandler.post(Runnable { getLocationData() })
            }
        }, 5, 1000)
        intent = Intent(loc_receiver)
    }

    fun getLocationData() {
        //Initialize Location manager
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //validate gps
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //  Looking for location from Network Provides or GPS
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 2000, 100f, locationListenerGPS
                )
            }
        }
    }

    //send location updates and make sure they are not the same
    private fun locationUpdate(location: Location) {
        if(Math.round(latitude * 10000.0) / 10000.0 != Math.round(location.latitude * 10000.0) / 10000.0
            && Math.round(longitude * 10000.0) / 10000.0 != Math.round(location.longitude * 10000.0) / 10000.0 ) {
            latitude = location.latitude
            longitude = location.longitude
            intent!!.putExtra(LATITUDE, latitude)
            intent!!.putExtra(LONGITUDE, longitude)
            sendBroadcast(intent)
        }
    }


    //location listener from gps
    private var locationListenerGPS: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationUpdate(location)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            //TODO: status change
        }
        override fun onProviderEnabled(provider: String) {
            //TODO: provider enabled
        }
        override fun onProviderDisabled(provider: String) {
            //TODO: provider disabled
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}