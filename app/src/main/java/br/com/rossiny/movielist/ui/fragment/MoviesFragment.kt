package br.com.rossiny.movielist.ui.fragment

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import br.com.rossiny.domain.Movie
import br.com.rossiny.domain.Result
import br.com.rossiny.movielist.BuildConfig
import br.com.rossiny.movielist.R
import br.com.rossiny.movielist.framework.MovieApi
import br.com.rossiny.movielist.framework.MovieService
import br.com.rossiny.movielist.ui.adapter.CustomListAdapter
import br.com.rossiny.movielist.utils.AdapterCallback
import br.com.rossiny.movielist.utils.LocalScrollListener
import br.com.rossiny.movielist.utils.PreferencesHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.util.concurrent.TimeoutException

private const val ARG_TYPE_VIEW = "param1"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [MoviesFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [MoviesFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class MoviesFragment : Fragment(), AdapterCallback {

    private var isFavoriteView: Boolean = false
    private var listener: OnFragmentInteractionListener? = null

    internal var adapter: CustomListAdapter? = null
    internal var layoutManager: GridLayoutManager? = null

    private var editText: EditText? = null
    private var rv: RecyclerView? = null
    internal var swipeRefresh: SwipeRefreshLayout? = null
    internal var progressBar: ProgressBar? = null
    private var errorLayout: LinearLayout? = null
    private var btnRetry: Button? = null
    private var txtError: TextView? = null

    private var loading = false

    private var currentPage = PAGE_START

    private var movieService: MovieService? = null

    private val isNetworkConnected: Boolean
        get() {
            val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isFavoriteView = it.getBoolean(ARG_TYPE_VIEW)
        }
    }

    override fun onStart() {
        super.onStart()
        adapter?.clear()
        val movieList = getCurrentList()
        adapter?.addAll(movieList)
        adapter?.notifyDataSetChanged()
    }

    private fun getCurrentList(): List<Movie> {
        return if (isFavoriteView) getLocalMovies() else movieListApi
    }

    private var movieListDevice: List<Movie> = emptyList()
    private var movieListApi: List<Movie> = emptyList()
    private var preventLoading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_movies, container, false)

        view.apply {
            rv = findViewById(R.id.main_recycler)
            editText = findViewById(R.id.edit_text)
            swipeRefresh = findViewById(R.id.swipe_refresh)
            progressBar = findViewById(R.id.main_progress)
            errorLayout = findViewById(R.id.error_layout)
            btnRetry = findViewById(R.id.error_btn_retry)
            txtError = findViewById(R.id.error_txt_cause)
        }

        context?.apply {
            adapter = CustomListAdapter(this)
            layoutManager = GridLayoutManager(this, 2)
        }
        rv?.layoutManager = layoutManager
        rv?.itemAnimator = DefaultItemAnimator()
        rv?.adapter = adapter

        if (!isFavoriteView) {
            rv?.addOnScrollListener(object : LocalScrollListener(layoutManager!!) {

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

            swipeRefresh?.setOnRefreshListener {
                //preferencesHelper.results = ""
                adapter?.clear()
                adapter?.notifyDataSetChanged()
                loading = false
                loadFirstPage()
            }
        } else {
            swipeRefresh?.isEnabled = false
            //swipeRefresh?.visibility = GONE
        }

        movieService = MovieApi.getClient()?.create()

        if (!isFavoriteView) {
            loadFirstPage()
        } else {
            //movieListDevice : List<Movie> = emptyList()
            movieListDevice = getLocalMovies()
            hideErrorView()
            progressBar?.visibility = View.GONE
            adapter?.addAll(movieListDevice)
            adapter?.notifyDataSetChanged()
            //adapter?.addLoadingFooter()
        }

        btnRetry?.setOnClickListener { loadFirstPage() }

        editText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterListMovies(s.toString())
            }

        })

        return view
    }

    private fun filterListMovies(search: String) {
        if (search.isNullOrEmpty()) {
            onStart()
            preventLoading = false
            swipeRefresh?.isEnabled = !isFavoriteView
            return
        }
        preventLoading = true
        swipeRefresh?.isEnabled = false
        swipeRefresh?.isRefreshing = false
        var list = getCurrentList()
        list = list.filter {
            m -> m.title?.toLowerCase()?.contains(search.toLowerCase()) ?: false ||
                m.releaseDate?.substring(0, 4)?.contains(search) ?: false
        }.toMutableList()
        adapter?.clear()
        adapter?.addAll(list)
        adapter?.notifyDataSetChanged()

    }

    private fun getLocalMovies(): List<Movie> {
        val preferencesHelper = context?.let { PreferencesHelper(it) }
        val list = Gson().fromJson<List<Movie>>(
            preferencesHelper?.results,
            object : TypeToken<List<Movie>>() {}.type
        )
        Log.d(TAG, "Movie count: " + list.size)
        return list
    }

    private fun loadFirstPage() {
        hideErrorView()

        if (preventLoading)
            return

        currentPage = 1
        callApi(currentPage)?.enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                swipeRefresh?.isRefreshing = false
                hideErrorView()

                val results = fetchResults(response)
                movieListApi = results
                progressBar?.visibility = View.GONE
                adapter?.addAll(results)
                //saveData()

                adapter?.addLoadingFooter()
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

        callApi(currentPage + 1)?.enqueue(object : Callback<Result> {
            override fun onResponse(call: Call<Result>, response: Response<Result>) {
                swipeRefresh?.isRefreshing = false
                adapter?.removeLoadingFooter()
                loading = false

                val results = fetchResults(response)
                movieListApi = movieListApi + results
                adapter?.addAll(results)
                //saveData()

                adapter?.addLoadingFooter()
            }

            override fun onFailure(call: Call<Result>, t: Throwable) {
                t.printStackTrace()
                loading = false
                adapter?.showRetry(true, fetchErrorMessage(t))
            }
        })
    }


    private fun callApi(page: Int): Call<Result>? {
        return movieService?.getPopularMovies(BuildConfig.ApiKey, "", page)
    }


    override fun retryPageLoad() {
        loadNextPage()
    }


    private fun showErrorView(throwable: Throwable) {

        if (errorLayout?.visibility == View.GONE) {
            errorLayout?.visibility = View.VISIBLE
            progressBar?.visibility = View.GONE
            swipeRefresh?.isRefreshing = false

            txtError?.text = fetchErrorMessage(throwable)
        }
    }

    private fun fetchErrorMessage(throwable: Throwable): String {
        var errorMsg = resources.getString(R.string.error_msg_unknown)

        if (!isNetworkConnected) {
            errorMsg = resources.getString(R.string.error_msg_no_internet)
        } else if (throwable is TimeoutException) {
            errorMsg = resources.getString(R.string.error_msg_timeout)
        }

        return errorMsg
    }


    private fun hideErrorView() {
        if (errorLayout?.visibility == View.VISIBLE) {
            errorLayout?.visibility = View.GONE
            progressBar?.visibility = View.VISIBLE
        }
    }



    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
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
         * @param isFavoriteView Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MoviesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(isFavoriteView: Boolean) =
            MoviesFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_TYPE_VIEW, isFavoriteView)
                }
            }

        val TAG = "MainActivity"

        private val PAGE_START = 1
    }
}
