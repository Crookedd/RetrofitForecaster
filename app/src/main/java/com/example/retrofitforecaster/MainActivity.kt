package com.example.retrofitforecaster

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView = findViewById(R.id.r_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ViewAdapter()
        recyclerView.adapter = adapter
        CoroutineScope(Dispatchers.IO).launch {
            val weatherData = RetrofitService.create().getForecastData("Shklov", "ec9102a991f613fe3368ac64d021a84a", "metric")
            withContext(Dispatchers.Main) {
                adapter.submitList(weatherData.list)
            }
        }
    }
}


interface RetrofitService {
    @GET("forecast")
    suspend fun getForecastData(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Response

    companion object {
        fun create(): RetrofitService {
            return Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RetrofitService::class.java)
        }
    }
}

data class Response(val list: List<WeatherForecast>)
data class WeatherForecast(val dt_txt: String, val main: Main)
data class Main(val temp: Float)

class ViewAdapter : ListAdapter<WeatherForecast, RecyclerView.ViewHolder>(DIFF_CALLBACK) {
//тут принимается два метода, которые сравнивают старые и новые элементы и возвращают трезультаты сравнения,чтобы обновлять.\
    //информацию
    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<WeatherForecast>() {
            override fun areItemsTheSame(oldItem: WeatherForecast, newItem: WeatherForecast): Boolean {
                return oldItem.dt_txt == newItem.dt_txt
            }

            override fun areContentsTheSame(oldItem: WeatherForecast, newItem: WeatherForecast): Boolean {
                return oldItem == newItem
            }
        }
    }
// зависимости от температуры мы возвращаем определеный тип представления
    override fun getItemViewType(position: Int): Int {
        val temp = getItem(position).main.temp
        return if (temp < 0) R.layout.item_view_cold else R.layout.item_view_hot
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return if (viewType == R.layout.item_view_hot) ViewHolderHot(view) else ViewHolderCold(view)
    }
//
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val forecast = getItem(position)
        if (holder is ViewHolderHot) {
            holder.bind(forecast)
        } else if (holder is ViewHolderCold) {
            holder.bind(forecast)
        }
    }
}

//два подкласса, которые принимают View и текствивдля отображения информации, внутри этих классов отображен бин, который принимает объект WeatherForecast
//идёт установка соответствуюзих данных.
class ViewHolderHot(view: View) : RecyclerView.ViewHolder(view) {
    private val textView: TextView = view.findViewById(R.id.textView)

    fun bind(forecast: WeatherForecast) {
        textView.text = "${forecast.dt_txt}: ${forecast.main.temp} °C"
    }
}

class ViewHolderCold(view: View) : RecyclerView.ViewHolder(view) {
    private val textView: TextView = view.findViewById(R.id.textView)

    fun bind(forecast: WeatherForecast) {
        textView.text = "${forecast.dt_txt}: ${forecast.main.temp} °C"
    }
}
