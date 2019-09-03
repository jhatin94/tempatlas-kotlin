package com.jhatin.tempatlas.models

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jhatin.tempatlas.R

class   Favorites(withContext: Context) {

    private var mFavorites: ArrayList<Favorite> = ArrayList()
    private val mContext: Context = withContext
    private val mGson = Gson()

    fun isFavorite(favorite: Favorite): Boolean {
        return mFavorites.contains(favorite)
    }

    fun add(favorite: Favorite) {
        mFavorites.add(favorite)
    }

    fun remove(favorite: Favorite) {
        mFavorites.remove(favorite)
    }

    fun loadFavorites() {
        // check if data is saved
        val preferences = mContext.getSharedPreferences(mContext.getString(R.string.temp_atlas_prefs), 0)
        val storedFavs = preferences.getString(mContext.getString(R.string.favorites_pref_key), null)

        if (!storedFavs.isNullOrEmpty()) {
            // deserialize and return the array
            val arrayListType = object : TypeToken<ArrayList<Favorite>>() {}.type
            mFavorites = mGson.fromJson(storedFavs, arrayListType)
        }
    }

    fun saveFavorites() {
        // JSON encode favorites array
        val favoritesJson = mGson.toJson(mFavorites)

        // save to shared preferences
        val preferences = mContext.getSharedPreferences(mContext.getString(R.string.temp_atlas_prefs), 0)
        val editor = preferences.edit()
        editor.putString(mContext.getString(R.string.favorites_pref_key), favoritesJson)
        editor.apply()
    }

    fun getFavorites() : ArrayList<Favorite> {
        return mFavorites
    }

    data class Favorite(
        var name: String,
        var coordinate: WeatherResponse.Coordinate
    ) {
        fun formattedCoord(): String {
            return "${coordinate.lat}, ${coordinate.lon}"
        }
    }
}