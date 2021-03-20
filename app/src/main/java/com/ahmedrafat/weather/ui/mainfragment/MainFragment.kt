package com.ahmedrafat.weather.ui.mainfragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.work.WorkInfo
import com.ahmedrafat.weather.R
import com.ahmedrafat.weather.databinding.FragmentMainBinding
import com.ahmedrafat.weather.model.*
import com.ahmedrafat.weather.model.apimodel.WeatherModel
import com.ahmedrafat.weather.utils.blurLocalImage
import com.ahmedrafat.weather.utils.hideKeyboard
import com.ahmedrafat.weather.utils.loadImage
import com.ahmedrafat.weather.utils.observeConnection
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    //location broadcast
    private lateinit var locationReceiver: BroadcastReceiver

    //Inject viewmodel
    private val mainViewModel: MainViewModel by viewModels()

    //loading dialog
    var alertDialog: AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //init view binding
        val binding = FragmentMainBinding.bind(view)
        alertDialog = initLoading()

        //call weather service
        mainViewModel.getWeatherData(defaultCity)

        //init work manager observer
        initWorkManager(binding)

        //init api observers
        initObservers(binding)

        //receiver updates from broadcast
        locationReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val latitude = intent?.getDoubleExtra(LATITUDE, 0.0)
                val longitude = intent?.getDoubleExtra(LONGITUDE, 0.0)

                if (latitude != null && longitude != null)
                    addressFetch(latitude, longitude)
            }

        }
    }

    private fun initObservers(binding: FragmentMainBinding) {
        //observe response from api
        mainViewModel.weatherModelMutableLiveData.observe(viewLifecycleOwner, { response ->
            if (response != null)
                displayData(response, binding)
        })

        //observe loading
        mainViewModel.loading.observe(viewLifecycleOwner, {
            if (it)
                alertDialog?.show()
            else
                alertDialog?.dismiss()
        })

        //observe if there is error
        mainViewModel.errorMutableLiveData.observe(viewLifecycleOwner, { apiError ->
            view?.let {
                when (apiError) {
                    EMPTY_VALIDATION -> Snackbar.make(
                        it,
                        resources.getString(R.string.empty_validation),
                        Snackbar.LENGTH_LONG
                    )
                    INTERNET_VALIDATION -> Snackbar.make(
                        it,
                        resources.getString(R.string.internet_required),
                        Snackbar.LENGTH_LONG
                    )
                    else -> Snackbar.make(it, apiError, Snackbar.LENGTH_LONG)
                        .setActionTextColor(getColor(requireContext(), R.color.white))
                        .show()
                }
            }
        })
    }

    private fun initWorkManager(binding: FragmentMainBinding) {

        //detect connection it must be put in base fragment class
        observeConnection(requireContext()).observe(viewLifecycleOwner, { info ->
            if (info.state == WorkInfo.State.FAILED) {
                context?.let {
                    view?.let { it1 ->
                        Snackbar.make(
                            it1,
                            getString(R.string.internet_required),
                            Snackbar.LENGTH_LONG
                        )
                            .setActionTextColor(getColor(it, R.color.white))
                            .show()
                    }
                }
            } else if (info.state == WorkInfo.State.SUCCEEDED) {
                if (binding.toolbarTitle.text != "")
                    mainViewModel.getWeatherData(binding.toolbarTitle.text.toString())
                else
                    mainViewModel.getWeatherData(defaultCity)
            }
        })

        //bluer main background
        blurLocalImage(requireContext(), R.drawable.background3x, binding.mainBg)

    }

    override fun onResume() {
        super.onResume()
        activity?.registerReceiver(locationReceiver, IntentFilter(loc_receiver))
    }

    override fun onPause() {
        super.onPause()
        activity?.unregisterReceiver(locationReceiver)
    }

    //display on ui
    private fun displayData(
        response: WeatherModel,
        binding: FragmentMainBinding
    ) {
        response.let {
            //apply data to ui
            binding.apply {
                toolbarTitle.text = response.name ?: defaultCity
                searchCity.queryHint = response.name ?: defaultCity
                //load weather icon
                response.weather?.get(0)?.let {
                    if (it.icon != null)
                        loadImage(it.icon, ivWeatherStatus)

                    description.text = it.description ?: ""
                }

                temp.text = "${response.main?.temp.toString()} ${getString(R.string.temp_unit)}"
                pressureValue.text = response.main?.pressure.toString()
                windValue.text = "${response.wind?.speed.toString()} Km"
                humidityValue.text = "${response.main?.humidity.toString()}%"

                //convert time from unix to Time
                val sunRiseMilli: Long? = response.sys?.sunrise?.toLong()
                sunRiseMilli?.let {
                    val date = Date(it * 1000)
                    sunriseValue.text =
                        "${getString(R.string.sun_rise)} ${SimpleDateFormat("hh:MM a").format(date)}"
                }
                val sunSetMilli: Long? = response.sys?.sunset?.toLong()

                sunSetMilli?.let {
                    val date = Date(it * 1000)
                    sunsetValue.text =
                        "${getString(R.string.sun_set)}   ${SimpleDateFormat("hh:MM a").format(date)}"

                }

                //submit search result
                searchCity.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (query != null) {
                            Log.d("Network", isNetworkConnected.toString())
                            mainViewModel.getWeatherData(query)
                            activity?.let { it1 -> hideKeyboard(it1) }
                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }
                })
            }
        }
    }

    //display loading
    private fun initLoading(): AlertDialog? {
        val loadingDialog: AlertDialog
        return if (context != null) {
            loadingDialog =
                AlertDialog.Builder(requireContext()).setView(R.layout.dialog_loading).create()
            loadingDialog
        } else {
            null
        }
    }

    //fetch city from address
    fun addressFetch(lattitude: Double, longitude: Double) {
        val addresses: List<Address>
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            addresses = geocoder.getFromLocation(lattitude, longitude, 1)
            if (addresses.isNotEmpty()) {
                mainViewModel.getWeatherData(addresses[0].locality)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}