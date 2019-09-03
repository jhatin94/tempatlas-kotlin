package com.jhatin.tempatlas.models


class WeatherResponse {

    data class Forecast(
        var coord: Coordinate,
        var weather: Array<WeatherInfo>,
        var name: String,
        var main: WeatherData,
        var wind: Wind,
        var clouds: Clouds
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Forecast

            if (name != other.name) return false
            if (!weather.contentEquals(other.weather)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + weather.contentHashCode()
            return result
        }
    }

    data class Coordinate(
        var lat: Double,
        var lon: Double
    )

    data class WeatherInfo(
        var id: Int,
        var main: String,
        var description: String,
        var icon: String
    )

    data class WeatherData(
        var temp: Double,
        var humidity: Int,
        var temp_min: Double,
        var temp_max: Double
    )

    data class Wind(
        var speed: Double
    )

    data class Clouds(
        var all: Double
    )
}