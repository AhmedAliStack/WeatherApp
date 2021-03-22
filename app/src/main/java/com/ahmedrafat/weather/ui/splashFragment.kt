package com.ahmedrafat.weather.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.ahmedrafat.weather.R
import com.ahmedrafat.weather.model.REQUEST_CHECK_SETTINGS
import com.ahmedrafat.weather.model.REQUEST_LOCATION
import com.ahmedrafat.weather.model.defaultCity
import com.ahmedrafat.weather.ui.mainfragment.MainFragment
import com.ahmedrafat.weather.utils.LocationGetter
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import java.io.IOException
import java.util.*


class splashFragment : Fragment(R.layout.fragment_splash) {
    private lateinit var locationManager: LocationManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //handle location permission
        handlePermission()

    }

    private fun handlePermission() {
        locationManager = requireActivity().getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locPermission()
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                locPermission()
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOCATION)
            } else
                autoNavigate()
                requireContext().startService(Intent(requireContext(), LocationGetter::class.java))
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
        val mSettingsClient: SettingsClient = LocationServices.getSettingsClient(requireContext())
        mSettingsClient
            .checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener { locationSettingsResponse ->
                requireContext().startService(Intent(requireContext(), LocationGetter::class.java))
            }
            .addOnFailureListener { e ->
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val rae: ResolvableApiException = e as ResolvableApiException
                        rae.startResolutionForResult(requireActivity(), REQUEST_CHECK_SETTINGS)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        autoNavigate()
        when (requestCode) {
            REQUEST_LOCATION -> {
                if (grantResults.isEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.location_permission),
                        Toast.LENGTH_LONG
                    ).show()
                }else{
                    requireContext().startService(Intent(requireContext(), LocationGetter::class.java))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun autoNavigate() {
        val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if(location?.latitude != null)
            addressFetch(location.latitude,location.longitude)
        else
            Handler(Looper.getMainLooper()).postDelayed(Runnable { /* Create an Intent that will start the Navigation host activity . */
                findNavController().navigate(R.id.action_splashFragment_to_mainFragment)
            }, 5000)
    }

    //fetch city from address
    fun addressFetch(lattitude: Double, longitude: Double) {
        val addresses: List<Address>
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(lattitude, longitude, 1)
            var city:String
            if (addresses.isNotEmpty()) {
                city = addresses[0].locality
            }else{
                city = defaultCity
            }
            val args = Bundle()
            args.putString("city",city)
            Handler(Looper.getMainLooper()).postDelayed(Runnable { /* Create an Intent that will start the Navigation host activity . */
                findNavController().navigate(R.id.action_splashFragment_to_mainFragment,args)
            }, 5000)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}