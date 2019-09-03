package com.jhatin.tempatlas

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.jhatin.tempatlas.fragments.FavoritesFragment
import com.jhatin.tempatlas.models.Favorites
import com.jhatin.tempatlas.models.WeatherResponse

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, FavoritesFragment.OnListFragmentInteractionListener {

    private lateinit var mMap: GoogleMap
    private lateinit var mActiveMarker: Marker
    private var mForecastSection: LinearLayout? = null
    private var mFragmentSection: FrameLayout? = null
    private lateinit var mLocationManager: LocationManager
    private var mProvider: String? = null
    private val mLocationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {}
        override fun onProviderEnabled(provider: String?) {}
        override fun onProviderDisabled(provider: String?) {}
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }
    private val mLocationRequestCode = 3500
    private val mFavorites: Favorites = Favorites(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        setupLocationUpdates()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mForecastSection = findViewById(R.id.forecast_info)
        mForecastSection?.visibility = View.GONE
        mFragmentSection = findViewById(R.id.favorites_list)
        mFavorites.loadFavorites()
        setupInteractions()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has∂
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        var location = LatLng(-34.0, 151.0) // Sydney is the default location

        // ensure a provider exists and that permission was granted before attempting to get user location
        if (mProvider != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val provider = mProvider as String

            // get the last retrieved location and convert to LatLng
            val lastLoc = mLocationManager.getLastKnownLocation(provider)
            if (lastLoc != null) {
                location = LatLng(lastLoc.latitude, lastLoc.longitude)
            }
        }

        // Add a default marker and move the camera
        mActiveMarker = mMap.addMarker(MarkerOptions().position(location).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 11f))

        // Request forecast from coordinates that are tapped on
        mMap.setOnMapClickListener { coordinate ->
            WeatherAPI.getWeatherBy(coordinate) { success, response ->
                if (success && response != null) {
                    runOnUiThread {
                        onForecastDataReceived(response)
                    }
                } else {
                    mForecastSection?.visibility = View.GONE
                }
            }
        }

        // request forecast from the default location
        WeatherAPI.getWeatherBy(location) { success, response ->
            if (success && response != null) {
                runOnUiThread {
                    onForecastDataReceived(response)
                }
            } else {
                mForecastSection?.visibility = View.GONE
            }
        }
    }

    override fun onListFragmentInteraction(favorite: Favorites.Favorite?) {
        // Update map location and forecast based on the selected favorite coordinate
        val favCoord = when {
            favorite != null -> LatLng(favorite.coordinate.lat, favorite.coordinate.lon)
            else -> null
        }

        if (favCoord != null && !favCoord.equals(mActiveMarker.position)) {
            WeatherAPI.getWeatherBy(favCoord) { success, response ->
                if (success && response != null) {
                    runOnUiThread {
                        // close the fragment
                        closeFragment()
                        onForecastDataReceived(response)
                    }
                } else {
                    mForecastSection?.visibility = View.GONE
                }
            }
        } else {
            closeFragment() // return to map if coord is identical
        }
    }

    override fun onBackPressed() {
        // close the fragment manually if it's open otherwise do normal operations
        if (mFragmentSection?.visibility == View.VISIBLE) {
            closeFragment()
        } else {
            super.onBackPressed()
        }
    }

    private fun setupInteractions() {

        // Toggle units with metric switch
        findViewById<Switch>(R.id.metric_switch).setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) { // update units
                true -> WeatherAPI.units = WeatherAPI.Units.METRIC
                false -> WeatherAPI.units = WeatherAPI.Units.IMPERIAL
            }

            // Re-request data with new units
            val currentPos = mActiveMarker.position
            WeatherAPI.getWeatherBy(currentPos) { success, response ->
                if (success && response != null) {
                    runOnUiThread {
                        onForecastDataReceived(response, false) // don't update the map, just the marker title
                        updateActiveMarker(getString(R.string.temperature_placeholder, response.main.temp))
                    }
                } else {
                    mForecastSection?.visibility = View.GONE
                }
            }
        }

        // override the search view's listeners to run searches for city forecasts
        findViewById<SearchView>(R.id.search_bar).setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val city = query.orEmpty()
                if (city.isNotEmpty()) {
                    WeatherAPI.getWeatherBy(city) { success, response ->
                        if (success && response != null) {
                            runOnUiThread {
                                onForecastDataReceived(response)
                            }
                        } else {
                            mForecastSection?.visibility = View.GONE
                        }
                    }
                }
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                return false
            }
        })

        // set the favorites button to add or remove the current location as a favorite
        findViewById<Button>(R.id.add_remove_button).setOnClickListener {
            // get current location from active marker and current location name from forecast view
            val currentPos = mActiveMarker.position
            val locationName = findViewById<TextView>(R.id.location_value).text as String
            if (locationName.isNotEmpty()) {
                val favorite = Favorites.Favorite(locationName, WeatherResponse.Coordinate(currentPos.latitude, currentPos.longitude))

                // remove if it's in the favorites collection
                if (mFavorites.isFavorite(favorite)) {
                    mFavorites.remove(favorite)
                } else { // otherwise add it
                    mFavorites.add(favorite)
                }

                mFavorites.saveFavorites() // save the modifications

                // update the button text after the modification
                (it as Button).text = when (mFavorites.isFavorite(favorite)) {
                    true -> getString(R.string.remove_favorites_button)
                    false -> getString(R.string.add_favorites_button)
                }
            }
        }

        // display the favorites list fragment with the current favorites
        findViewById<Button>(R.id.favorites_button).setOnClickListener {
            val fragment = FavoritesFragment.newInstance(mFavorites)
            supportFragmentManager.beginTransaction()
                .replace(R.id.favorites_list, fragment)
                .addToBackStack(getString(R.string.fragment_back_stack_id))
                .commit()
            mFragmentSection?.visibility = View.VISIBLE
        }
    }

    private fun onForecastDataReceived(data: WeatherResponse.Forecast, updateMap: Boolean = true) {

        // Make Forecast visible
        if (mForecastSection?.visibility != View.VISIBLE) {
            mForecastSection?.visibility = View.VISIBLE
        }

        // update all text views
        findViewById<SearchView>(R.id.search_bar).setQuery("", false)
        findViewById<SearchView>(R.id.search_bar).clearFocus()
        findViewById<TextView>(R.id.location_value).text = data.name
        findViewById<TextView>(R.id.conditions_value).text = getString(R.string.conditions_response, data.weather[0].main, data.main.temp)
        findViewById<TextView>(R.id.high_value).text = getString(R.string.temperature_placeholder, data.main.temp_max)
        findViewById<TextView>(R.id.low_value).text = getString(R.string.temperature_placeholder, data.main.temp_min)
        findViewById<TextView>(R.id.humidity_value).text = getString(R.string.percentage_int_placeholder, data.main.humidity)
        findViewById<TextView>(R.id.wind_speed_value).text = getSpeedText(data.wind.speed)
        findViewById<TextView>(R.id.cloud_cover_value).text = getString(R.string.percentage_placeholder, data.clouds.all)

        // update the favorite button text based on if this coordinate is a favorite
        val responseAsFav = Favorites.Favorite(data.name, WeatherResponse.Coordinate(data.coord.lat, data.coord.lon))
        findViewById<Button>(R.id.add_remove_button).text = when (mFavorites.isFavorite(responseAsFav)) {
            true -> getString(R.string.remove_favorites_button)
            false -> getString(R.string.add_favorites_button)
        }

        // update map position and pin location
        if (updateMap) {
            val forecastLocation = LatLng(data.coord.lat, data.coord.lon)
            updateMap(forecastLocation, getString(R.string.temperature_placeholder, data.main.temp))
        }
    }

    // Helper functions

    private fun getSpeedText(speed: Double): String = when (WeatherAPI.units) {
        WeatherAPI.Units.IMPERIAL -> getString(R.string.imperial_speed_placeholder, speed)
        WeatherAPI.Units.METRIC -> getString(R.string.metric_speed_placeholder, speed)
    }

    private fun updateMap(location: LatLng, temperature: String) {
        mActiveMarker.remove()

        mActiveMarker = mMap.addMarker(MarkerOptions().position(location).title(temperature))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 11f))
    }

    private fun updateActiveMarker(title: String) {
        mActiveMarker.title = title

        // recycle the info window to display the new title
        if (mActiveMarker.isInfoWindowShown) {
            mActiveMarker.hideInfoWindow()
            mActiveMarker.showInfoWindow()
        }
    }

    private fun closeFragment() {
        val activeFragment = supportFragmentManager.findFragmentById(R.id.favorites_list)
        if (activeFragment != null) {
            supportFragmentManager.beginTransaction()
                .remove(activeFragment)
                .commit()
            supportFragmentManager.popBackStack(getString(R.string.fragment_back_stack_id), FragmentManager.POP_BACK_STACK_INCLUSIVE)
            mFragmentSection?.visibility = View.GONE
        }
    }

    private fun setupLocationUpdates() {
        // remove any subscribed updates
        mLocationManager.removeUpdates(mLocationListener)

        // check/request permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), mLocationRequestCode)
        } else {
            // get the location provider from this criteria
            val locationCriteria = Criteria()
            locationCriteria.accuracy = Criteria.ACCURACY_COARSE
            locationCriteria.powerRequirement = Criteria.POWER_MEDIUM

            mProvider = mLocationManager.getBestProvider(locationCriteria, true)

            if (mProvider != null) { // request updates from this provider
                val provider = mProvider as String
                mLocationManager.requestLocationUpdates(provider, 1000, 0f, mLocationListener)
            }
        }
    }
}
