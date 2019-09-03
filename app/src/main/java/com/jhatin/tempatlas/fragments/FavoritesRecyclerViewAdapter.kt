package com.jhatin.tempatlas.fragments

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jhatin.tempatlas.R
import com.jhatin.tempatlas.fragments.FavoritesFragment.OnListFragmentInteractionListener
import com.jhatin.tempatlas.models.Favorites
import kotlinx.android.synthetic.main.fragment_favorites.view.*

/**
 * [RecyclerView.Adapter] that can display a [Favorite] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class FavoritesRecyclerViewAdapter(
    private val mValues: List<Favorites.Favorite>,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<FavoritesRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Favorites.Favorite

            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_favorites, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.mLocationView.text = item.name
        holder.mCoordinateView.text = item.formattedCoord()

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mLocationView: TextView = mView.favorite_location
        val mCoordinateView: TextView = mView.favorite_coordinates

        override fun toString(): String {
            return super.toString() + " '${mLocationView.text}, ${mCoordinateView.text}'"
        }
    }
}
