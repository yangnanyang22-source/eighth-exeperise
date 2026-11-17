package com.example.yn.weatherapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CityAdapter(
    private val context: Context
) : BaseAdapter() {

    private val cities = mutableListOf<City>()

    fun replaceAll(newCities: List<City>) {
        cities.clear()
        cities.addAll(newCities)
        notifyDataSetChanged()
    }

    override fun getCount(): Int = cities.size

    override fun getItem(position: Int): City = cities[position]

    override fun getItemId(position: Int): Long = getItem(position).id.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_city, parent, false)
        val holder = view.tag as? ViewHolder ?: ViewHolder(
            name = view.findViewById(R.id.city_name),
            code = view.findViewById(R.id.city_code)
        ).also { view.tag = it }

        val city = getItem(position)
        holder.name.text = city.cityName
        holder.code.text = context.getString(R.string.city_code_format, city.cityCode)
        return view
    }

    private data class ViewHolder(
        val name: TextView,
        val code: TextView
    )
}
