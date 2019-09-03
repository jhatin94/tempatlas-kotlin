package com.jhatin.tempatlas.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jhatin.tempatlas.R
import com.jhatin.tempatlas.models.Favorites

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [FavoritesFragment.OnListFragmentInteractionListener] interface.
 */
class FavoritesFragment : Fragment() {

    private var favoritesCollection = ArrayList<Favorites.Favorite>()
    private var listener: OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            val json = it.getString(ARG_FAVORITES_LIST)
            if (!json.isNullOrEmpty()) {
                val arrayListType = object : TypeToken<ArrayList<Favorites.Favorite>>() {}.type
                favoritesCollection = Gson().fromJson(json, arrayListType)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_favorite_list, container, false)
        val view = rootView.findViewById<RecyclerView>(R.id.list)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = FavoritesRecyclerViewAdapter(favoritesCollection, listener)
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // determine if no data text should appear
        val noDataView = view.findViewById<TextView>(R.id.no_data)
        noDataView.visibility = when {
            favoritesCollection.count() > 0 -> View.GONE
            else -> View.VISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnListFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(favorite: Favorites.Favorite?)
    }

    companion object {

        // parameter argument names
        const val ARG_FAVORITES_LIST = "favorite_list_arg"

        // parameter initialization
        @JvmStatic
        fun newInstance(favorites: Favorites) =
                FavoritesFragment().apply {
                    arguments = Bundle().apply {
                        val json = Gson().toJson(favorites.getFavorites())
                        putString(ARG_FAVORITES_LIST, json)
                    }
                }
    }
}
