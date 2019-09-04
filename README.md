# TempAtlas in Kotlin
Google Maps combined with the OpenWeather API written in Kotlin

***
# Build Info

* Android Studio 3.4.2
* Android 8.0 (SDK 26)

***
# Dependencies

* Anko 0.10.8
* Gson 2.8.5
* Google Maps 17.0.0

***
#  Project Structure

The app is a single activity application. At the root of the project lives the `MapActivity`, which is the main activity of the app, and the `WeatherAPI` singleton. The `MapsActivity` presents the map and handles all the functionality of the app. This activity presents a full screen fragment to display a list of all the user's saved favorites, and implements the fragment's interface to handle when a favorite is selected so that the map will update.

The `WeatherAPI` singleton contains the functions and data necessary to make calls to the OpenWeather API to get weather data by city name and by coordinates. It publicly exposes it's units so that it can be modified by the metric `Switch` in the `MapsActivity`

## Models

This folder contains the `WeatherResponse` and `Favorites` models. `WeatherResponse` contains all the data classes necessary to decode an API response into an object. `Favorites` contains a data class for the `Favorite` object, a private collection to hold them, and public functions to modify the collection.

## Fragments

The Fragments folder contains the `FavoritesFragment` and `FavoritesRecyclerViewAdapter` which are needed to display favorites data in the full screen Fragment. The `FavoritesFragment` looks for a collection of `Favorite` objects to be passed in its Bundle arguments. It also declares the `OnListFragmentInteractionListener` interface, which is implemented in the `MapsActivity` and called in each `View`'s `OnClickListener` in the `FavoritesRecyclerViewAdapter`. The adapter simply gets each `Favorite` from the passed in collection and places the appropriate data in the right `TextView`s in the `ListView` cells.

## Layouts

Three layout files exist for the app. `activity_maps.xml` contains almost all of the UI code for the app. It lays out the `SearchView`, Map fragment, the Favorites fragment placeholder `FrameLayout`, and the entire Forecast Info Section inside its own `LinearLayout`. 

`fragment_favorites_list.xml` is simply a `LinearLayout` that contains a `TextView` that appears when there are no favorites and the `ListView` that appears when there are favorites to display

`fragment_favorites.xml` is the layout file for each `ListView` cell and is simply two `TextView`s in a vertical `LinearLayout` where the font is smaller in the bottom `TextView`. This is for displaying the Location name and then the coordinates beneath.
