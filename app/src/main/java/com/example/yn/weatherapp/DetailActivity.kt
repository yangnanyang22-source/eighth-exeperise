package com.example.yn.weatherapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import java.util.Locale

class DetailActivity : AppCompatActivity() {

    private val gson = Gson()
    private var requestQueue: RequestQueue? = null

    private lateinit var contentView: View
    private lateinit var loadingView: View
    private lateinit var errorView: TextView
    private lateinit var cityTextView: TextView
    private lateinit var updateTimeTextView: TextView
    private lateinit var currentIconView: ImageView
    private lateinit var currentTemperatureView: TextView
    private lateinit var currentTypeView: TextView
    private lateinit var currentRangeView: TextView
    private lateinit var weatherInfoView: TextView
    private lateinit var adviceView: TextView
    private lateinit var forecastContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_weather_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail_root)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bindViews()

        val cityCode = intent.getStringExtra(EXTRA_CITY_CODE).orEmpty()
        val cityName = intent.getStringExtra(EXTRA_CITY_NAME).orEmpty()
        if (cityCode.isBlank()) {
            showError(getString(R.string.invalid_city_code))
            return
        }

        cityTextView.text = cityName
        requestWeather(cityCode)
    }

    private fun bindViews() {
        contentView = findViewById(R.id.weather_content)
        loadingView = findViewById(R.id.loading_group)
        errorView = findViewById(R.id.error_text)
        cityTextView = findViewById(R.id.detail_city)
        updateTimeTextView = findViewById(R.id.update_time)
        currentIconView = findViewById(R.id.current_weather_icon)
        currentTemperatureView = findViewById(R.id.current_temperature)
        currentTypeView = findViewById(R.id.current_type)
        currentRangeView = findViewById(R.id.current_range)
        weatherInfoView = findViewById(R.id.weather_info)
        adviceView = findViewById(R.id.weather_advice)
        forecastContainer = findViewById(R.id.forecast_container)
    }

    private fun requestWeather(cityCode: String) {
        loadingView.visibility = View.VISIBLE
        contentView.visibility = View.INVISIBLE
        errorView.visibility = View.GONE

        val queue = Volley.newRequestQueue(this)
        requestQueue = queue
        val url = "$WEATHER_API_BASE$cityCode"
        val request = StringRequest(
            Request.Method.GET,
            url,
            { response -> showWeather(response) },
            { error ->
                showError(
                    error.networkResponse?.let { "HTTP ${it.statusCode}" }
                        ?: error.message
                        ?: getString(R.string.unknown_value)
                )
            }
        ).apply {
            tag = REQUEST_TAG
            retryPolicy = DefaultRetryPolicy(10_000, 1, 1f)
        }
        queue.add(request)
    }

    private fun showWeather(json: String) {
        try {
            val weather = gson.fromJson(json, WeatherResponse::class.java)
            if (weather == null || weather.status != 200) {
                showError(weather?.message ?: getString(R.string.unknown_value))
                return
            }

            val today = weather.data.forecast.firstOrNull()
            cityTextView.text = weather.cityInfo.city.ifBlank {
                intent.getStringExtra(EXTRA_CITY_NAME).orEmpty()
            }
            updateTimeTextView.text = getString(
                R.string.update_time_format,
                weather.cityInfo.updateTime.ifBlank { weather.time }
            )
            currentTemperatureView.text = getString(
                R.string.temperature_format,
                weather.data.wendu
            )
            currentTypeView.text = today?.type.orEmpty()
            currentRangeView.text = if (today == null) {
                getString(R.string.unknown_value)
            } else {
                "${today.high}  ${today.low}"
            }
            currentIconView.setImageResource(weatherIcon(today?.type.orEmpty()))
            currentIconView.contentDescription = getString(
                R.string.weather_icon_description,
                today?.type.orEmpty()
            )

            weatherInfoView.text = getString(
                R.string.weather_info_format,
                weather.data.shidu.ifBlank { getString(R.string.unknown_value) },
                weather.data.quality.ifBlank { getString(R.string.unknown_value) },
                today?.aqi ?: 0,
                formatDecimal(weather.data.pm25),
                formatDecimal(weather.data.pm10),
                today?.fx.orEmpty().ifBlank { getString(R.string.unknown_value) },
                today?.fl.orEmpty(),
                today?.sunrise.orEmpty().ifBlank { getString(R.string.unknown_value) },
                today?.sunset.orEmpty().ifBlank { getString(R.string.unknown_value) }
            )
            adviceView.text = getString(
                R.string.weather_advice_format,
                weather.data.ganmao.ifBlank { getString(R.string.unknown_value) }
            )

            bindForecast(weather.data.forecast)
            loadingView.visibility = View.GONE
            contentView.visibility = View.VISIBLE
        } catch (exception: Exception) {
            showError(exception.message ?: exception.javaClass.simpleName)
        }
    }

    private fun bindForecast(forecast: List<Forecast>) {
        forecastContainer.removeAllViews()
        forecast.forEach { item ->
            val itemView = LayoutInflater.from(this)
                .inflate(R.layout.item_forecast, forecastContainer, false)
            val iconView = itemView.findViewById<ImageView>(R.id.forecast_icon)
            iconView.setImageResource(weatherIcon(item.type))
            iconView.contentDescription = getString(
                R.string.weather_icon_description,
                item.type
            )
            itemView.findViewById<TextView>(R.id.forecast_date).text = getString(
                R.string.forecast_date_format,
                item.week,
                item.ymd
            )
            itemView.findViewById<TextView>(R.id.forecast_weather).text = getString(
                R.string.forecast_weather_format,
                item.type,
                item.high,
                item.low
            )
            itemView.findViewById<TextView>(R.id.forecast_note).text = item.notice
            itemView.findViewById<TextView>(R.id.forecast_wind).text = getString(
                R.string.forecast_wind_format,
                item.fx,
                item.fl
            )
            forecastContainer.addView(itemView)
        }
    }

    private fun formatDecimal(value: Double?): String {
        return value?.let { String.format(Locale.CHINA, "%.1f", it) }
            ?: getString(R.string.unknown_value)
    }

    private fun weatherIcon(type: String): Int {
        return when {
            type.contains("雷") -> R.drawable.thunder
            type.contains("雪") || type.contains("冰雹") -> R.drawable.snow
            type.contains("雨") -> R.drawable.rain
            type.contains("阴") -> R.drawable.mcloud
            type.contains("云") -> R.drawable.cloud
            type.contains("晴") -> R.drawable.sun
            else -> R.drawable.cloud
        }
    }

    private fun showError(message: String) {
        loadingView.visibility = View.GONE
        contentView.visibility = View.GONE
        errorView.visibility = View.VISIBLE
        errorView.text = getString(R.string.weather_request_failed, message)
    }

    override fun onDestroy() {
        requestQueue?.cancelAll(REQUEST_TAG)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_CITY_CODE = "extra_city_code"
        const val EXTRA_CITY_NAME = "extra_city_name"

        private const val WEATHER_API_BASE =
            "http://t.weather.itboy.net/api/weather/city/"
        private const val REQUEST_TAG = "weather_detail_request"
    }
}
