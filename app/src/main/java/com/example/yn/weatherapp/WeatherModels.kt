package com.example.yn.weatherapp

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val message: String = "",
    val status: Int = 0,
    val date: String = "",
    val time: String = "",
    @SerializedName("cityInfo")
    val cityInfo: WeatherCityInfo = WeatherCityInfo(),
    val data: WeatherData = WeatherData()
)

data class WeatherCityInfo(
    val city: String = "",
    val citykey: String = "",
    val parent: String = "",
    val updateTime: String = ""
)

data class WeatherData(
    val shidu: String = "",
    val pm25: Double? = null,
    val pm10: Double? = null,
    val quality: String = "",
    val wendu: String = "",
    val ganmao: String = "",
    val forecast: List<Forecast> = emptyList()
)

data class Forecast(
    val date: String = "",
    val high: String = "",
    val low: String = "",
    val ymd: String = "",
    val week: String = "",
    val sunrise: String = "",
    val sunset: String = "",
    val aqi: Int = 0,
    val fx: String = "",
    val fl: String = "",
    val type: String = "",
    val notice: String = ""
)
