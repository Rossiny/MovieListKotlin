package br.com.rossiny.movielist.ui.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import br.com.rossiny.domain.Movie
import br.com.rossiny.movielist.R
import br.com.rossiny.movielist.utils.PreferencesHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [FavoriteMoviesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [FavoriteMoviesFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class FavoriteMoviesFragment : MoviesFragment() {
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onStart() {
        super.onStart()
        movieList = getLocalMovies()
        adapter.setList(movieList)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        swipeRefresh.isEnabled = false

        hideErrorView()
        progressBar.visibility = View.GONE
        movieList = getLocalMovies()
        adapter.addAll(movieList)
        //adapter?.addLoadingFooter()

        return view
    }

    private fun getLocalMovies(): List<Movie> {
        val preferencesHelper = context?.let { PreferencesHelper(it) }
        var list : List<Movie> = emptyList()
        preferencesHelper?.results?.let {
            if (it.isNotEmpty()) {
                list = Gson().fromJson<List<Movie>>(it,
                    object : TypeToken<List<Movie>>() {}.type
                )
            }
        }
        Log.d(TAG, "Movie count: " + list.size)
        return list
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment FavoriteMoviesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            FavoriteMoviesFragment().apply {
                arguments = Bundle()
            }
    }
}
