package com.ahmedrafat.weather.ui.mainfragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ahmedrafat.weather.R
import com.ahmedrafat.weather.databinding.FragmentMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main) {
    private val mainViewModel:MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentMainBinding.bind(view)
        mainViewModel.getWeatherData("Paris")
        mainViewModel.weatherModelMutableLiveData.observe(viewLifecycleOwner, { response ->
            response.name?.let { Log.d("Response", it) }
        })
    }
}