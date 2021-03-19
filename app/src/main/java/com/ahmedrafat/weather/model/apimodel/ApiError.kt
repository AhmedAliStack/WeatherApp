package com.ahmedrafat.weather.model.apimodel


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "cod")
    val cod: String? = "",
    @Json(name = "message")
    val message: String? = ""
)