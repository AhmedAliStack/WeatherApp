package com.ahmedrafat.weather.ui.mainfragment

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
import com.ahmedrafat.weather.model.EMPTY_VALIDATION
import com.ahmedrafat.weather.model.INTERNET_VALIDATION
import com.ahmedrafat.weather.model.apimodel.WeatherModel
import com.ahmedrafat.weather.model.isNetworkConnected
import com.ahmedrafat.weather.utils.blurLocalImage
import com.ahmedrafat.weather.utils.hideKeyboard
import com.ahmedrafat.weather.utils.loadImage
import com.ahmedrafat.weather.utils.observeConnection
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {

    //Inject viewmodel
    private val mainViewModel: MainViewModel by viewModels()
    var alertDialog:AlertDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init view binding
        val binding = FragmentMainBinding.bind(view)
        alertDialog = initLoading()

        context?.let {
            //detect connection it must be put in base fragment class
            observeConnection(it).observe(viewLifecycleOwner, { info ->
                if (info.state == WorkInfo.State.FAILED) {
                    context?.let {
                        Snackbar.make(
                            view,
                            getString(R.string.internet_required),
                            Snackbar.LENGTH_LONG
                        )
                            .setActionTextColor(getColor(it, R.color.white))
                            .show()
                    }
                }
                else if (info.state == WorkInfo.State.SUCCEEDED) {
                    mainViewModel.getWeatherData("Paris")
                }
            })

            //bluer main background
            blurLocalImage(it, R.drawable.background3x, binding.mainBg)
        }

        //call weather service
        mainViewModel.getWeatherData("Paris")

        //observe response from api
        mainViewModel.weatherModelMutableLiveData.observe(viewLifecycleOwner, { response ->
            if (response != null)
                displayData(response, binding)
        })

        //observe loading
        mainViewModel.loading.observe(viewLifecycleOwner,{
            if(it)
                alertDialog?.show()
            else
                alertDialog?.dismiss()
        })

        //observe if there is error
        mainViewModel.errorMutableLiveData.observe(viewLifecycleOwner, { apiError ->
            context?.let {
                when(apiError){
                    EMPTY_VALIDATION -> Snackbar.make(
                        view,
                        resources.getString(R.string.empty_validation),
                        Snackbar.LENGTH_LONG
                    )
                    INTERNET_VALIDATION -> Snackbar.make(
                        view,
                        resources.getString(R.string.internet_required),
                        Snackbar.LENGTH_LONG
                    )
                    else -> Snackbar.make(view, apiError, Snackbar.LENGTH_LONG)
                        .setActionTextColor(getColor(it, R.color.white))
                    .show()
                }
            }
        })
    }

    //display on ui
    private fun displayData(
        response: WeatherModel,
        binding: FragmentMainBinding
    ) {
        response.let {
            //apply data to ui
            binding.apply {
                toolbarTitle.text = response.name ?: "Cairo"

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
    private fun initLoading():AlertDialog?{
        val loadingDialog: AlertDialog
        return if(context != null) {
            loadingDialog =
                AlertDialog.Builder(requireContext()).setView(R.layout.dialog_loading).create()
            loadingDialog
        }else{
            null
        }
    }
}