package com.ahmedrafat.weather.model

const val DOMAIN :String = "https://api.openweathermap.org/data/2.5/"
const val IMAGE_URL :String = "https://openweathermap.org/img/w/"
const val APP_ID :String = "3bc2dbd008837d08106feca86c51a4ad"
const val TEMP_UNIT :String = "metric"
const val EMPTY_VALIDATION :String = "Empty"
const val INTERNET_VALIDATION :String = "Internet"
const val loc_receiver: String = "location_broadcast"
const val defaultCity: String = "Paris"
const val LATITUDE: String = "latutide"
const val LONGITUDE: String = "longitude"
const val REQUEST_CHECK_SETTINGS: Int = 200
const val REQUEST_LOCATION: Int = 99

// Global variable used to store network state
var isNetworkConnected = false
