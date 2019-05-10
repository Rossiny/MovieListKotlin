package br.com.rossiny.movielist.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import br.com.rossiny.domain.Movie
import br.com.rossiny.domain.Result
import br.com.rossiny.movielist.BuildConfig
import br.com.rossiny.movielist.framework.MovieApi
import br.com.rossiny.movielist.framework.MovieService
import br.com.rossiny.movielist.utils.AdapterCallback
import br.com.rossiny.movielist.utils.LocalScrollListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PopularMoviesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PopularMoviesFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PopularMoviesFragment : MoviesFragment(), AdapterCallback {
    // TODO: Rename and change types of parameters
    private var listener: OnFragmentInteractionListener? = null


    private var loading = false

    private var currentPage = PAGE_START

    private var movieService: MovieService? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        movieService = MovieApi.getClient()?.create()

        btnRetry.setOnClickListener { loadFirstPage() }

        rv.addOnScrollListener(object : LocalScrollListener(layoutManager) {

            override val totalPageCount: Int
                get() = currentPage

            override val isLoading: Boolean
                get() = loading

            override fun loadMoreItems() {
                loading = true
                currentPage += 1

                loadNextPage()
            }
        })

        swipeRefresh.setOnRefreshListener {
            adapter.clear()
            adapter.notifyDataSetChanged()
            loading = false
            loadFirstPage()
        }

        loadFirstPage()

        return view
    }

    private fun loadFirstPage() {
        hideErrorView()

        if (preventLoading)
            return

        currentPage = 1
        callApi(currentPage)?.enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                swipeRefresh.isRefreshing = false
                hideErrorView()

                val results = fetchResults(response)
                movieList = results
                progressBar.visibility = View.GONE
                adapter.addAll(results)
                //saveData()

                adapter.addLoadingFooter()
            }

            override fun onFailure(call: Call<Result>, t: Throwable) {
                t.printStackTrace()
                showErrorView(t)
            }
        })
    }

    private fun fetchResults(response: Response<Result>): List<Movie> {
        val result = response.body()
        result?.results?.also {
            return it
        }

        return arrayListOf()
    }

    private fun loadNextPage() {
        Log.d(TAG, "loadNext: $currentPage")

        if (preventLoading)
            return

        callApi(currentPage)?.enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                swipeRefresh.isRefreshing = false
                adapter.removeLoadingFooter()
                loading = false

                val results = fetchResults(response)
                movieList = movieList + results
                adapter.addAll(results)
                //saveData()

                adapter.addLoadingFooter()
            }

            override fun onFailure(call: Call<Result>, t: Throwable) {
                t.printStackTrace()
                loading = false
                adapter.showRetry(true, fetchErrorMessage(t))
            }
        })
    }


    private fun callApi(page: Int): Call<Result>? {
        return movieService?.getPopularMovies(BuildConfig.ApiKey, "", page)
    }


    override fun retryPageLoad() {
        loadNextPage()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PopularMoviesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            PopularMoviesFragment().apply {
                arguments = Bundle()
            }

        private const val PAGE_START = 1
    }
}
