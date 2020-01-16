package com.jhatin.tempatlas

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.jhatin.tempatlas.models.WeatherResponse
import org.jetbrains.anko.doAsync
import java.lang.Exception
import java.net.URL
import java.net.URLEncoder

object WeatherAPI {

    enum class Units {
        IMPERIAL, METRIC
    }

    var units = Units.IMPERIAL
    private val mBaseApiUrl = "https://api.openweathermap.org/data/2.5/weather?"
    private val mAppId = "&APPID=YOUR_APP_ID_HERE"

    fun getWeatherBy(city: String, completionHandler: (Boolean, WeatherResponse.Forecast?) -> Unit) {
        val encodedCity = URLEncoder.encode(city, "UTF-8")
        val urlString = mBaseApiUrl + "q=$encodedCity&units=$units$mAppId"

        doAsync {
            try {
                val result = URL(urlString).readText()
                val weatherResponse = Gson().fromJson(result, WeatherResponse.Forecast::class.java)
                completionHandler(true, weatherResponse)
            }
            catch (exception: Exception) {
                completionHandler(false, null)
            }
        }
    }

    fun getWeatherBy(coordinates: LatLng, completionHandler: (Boolean, WeatherResponse.Forecast?) -> Unit) {
        val urlString = mBaseApiUrl + "lat=${coordinates.latitude}&lon=${coordinates.longitude}&units=$units$mAppId"

        doAsync {
            try {
                val result = URL(urlString).readText()
                val weatherResponse = Gson().fromJson(result, WeatherResponse.Forecast::class.java)
                completionHandler(true, weatherResponse)
            }
            catch (exception: Exception) {
                completionHandler(false, null)
            }
        }
    }
}
