package com.example.yn.weatherapp

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("pid")
    val parentId: Int = 0,
    @SerializedName("city_code")
    val cityCode: String = "",
    @SerializedName("city_name")
    val cityName: String = ""
)
