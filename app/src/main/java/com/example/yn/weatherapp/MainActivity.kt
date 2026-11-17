package com.example.yn.weatherapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val cityLoader = Executors.newSingleThreadExecutor()
    private val gson = Gson()

    private lateinit var cityAdapter: CityAdapter
    private lateinit var cityListView: ListView
    private lateinit var hintTextView: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cityListView = findViewById(R.id.city_list)
        hintTextView = findViewById(R.id.city_list_hint)
        progressBar = findViewById(R.id.progress_bar)
        cityAdapter = CityAdapter(this)
        cityListView.adapter = cityAdapter

        cityListView.setOnItemClickListener { _, _, position, _ ->
            val city = cityAdapter.getItem(position)
            startActivity(
                Intent(this, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_CITY_CODE, city.cityCode)
                    putExtra(DetailActivity.EXTRA_CITY_NAME, city.cityName)
                }
            )
        }

        loadCities()
    }

    private fun loadCities() {
        progressBar.visibility = View.VISIBLE
        hintTextView.text = getString(R.string.loading_cities)

        cityLoader.execute {
            try {
                val json = resources.openRawResource(R.raw.citycode)
                    .bufferedReader()
                    .use { it.readText() }
                val cities = gson.fromJson(json, Array<City>::class.java)
                    ?.filter { it.cityCode.isNotBlank() }
                    .orEmpty()

                runOnUiThread {
                    cityAdapter.replaceAll(cities)
                    progressBar.visibility = View.GONE
                    hintTextView.text = getString(R.string.city_count_format, cities.size)
                }
            } catch (exception: Exception) {
                Log.e(TAG, "Failed to load cities", exception)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    hintTextView.text = getString(
                        R.string.city_list_load_failed,
                        exception.message ?: exception.javaClass.simpleName
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        cityLoader.shutdownNow()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
