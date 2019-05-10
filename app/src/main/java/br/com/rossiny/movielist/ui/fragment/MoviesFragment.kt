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
import android.widget.*
import br.com.rossiny.domain.Movie
import br.com.rossiny.domain.Result
import br.com.rossiny.movielist.BuildConfig
import br.com.rossiny.movielist.R
import br.com.rossiny.movielist.framework.MovieApi
import br.com.rossiny.movielist.framework.MovieService
import br.com.rossiny.movielist.ui.adapter.CustomListAdapter
import br.com.rossiny.movielist.utils.AdapterCallback
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
open class MoviesFragment : Fragment() {

    private var listener: OnFragmentInteractionListener? = null

    protected lateinit var adapter: CustomListAdapter
    protected lateinit var layoutManager: GridLayoutManager

    protected lateinit var rv: RecyclerView
    protected lateinit var swipeRefresh: SwipeRefreshLayout
    protected lateinit var progressBar: ProgressBar
    protected lateinit var btnRetry: Button

    private lateinit var editText: EditText
    private lateinit var errorLayout: LinearLayout
    private lateinit var txtError: TextView

    protected var movieList: List<Movie> = emptyList()

    protected var preventLoading: Boolean = false

    private val isNetworkConnected: Boolean
        get() {
            val cm = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            return cm.activeNetworkInfo != null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        context?.apply {
            adapter = CustomListAdapter(this)
            layoutManager = GridLayoutManager(this, 2)
        }
    }

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

        rv.layoutManager = layoutManager
        rv.itemAnimator = DefaultItemAnimator()
        rv.adapter = adapter

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMovieList(s.toString())
            }

        })

        return view
    }

    private fun filterMovieList(search: String) {
        if (search.isEmpty()) {
            adapter.setList(movieList)
            preventLoading = false
            return
        }
        adapter.removeLoadingFooter()
        preventLoading = true
        swipeRefresh.isEnabled = false
        swipeRefresh.isRefreshing = false
        var list = movieList
        list = list.filter {
            m -> m.title?.toLowerCase()?.contains(search.toLowerCase()) ?: false ||
                m.releaseDate?.substring(0, 4)?.contains(search) ?: false
        }.toMutableList()
        adapter.setList(list)
    }

    protected fun showErrorView(throwable: Throwable) {

        if (errorLayout.visibility == View.GONE) {
            errorLayout.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
            swipeRefresh.isRefreshing = false

            txtError.text = fetchErrorMessage(throwable)
        }
    }

    protected fun fetchErrorMessage(throwable: Throwable): String {
        var errorMsg = resources.getString(R.string.error_msg_unknown)

        if (!isNetworkConnected) {
            errorMsg = resources.getString(R.string.error_msg_no_internet)
        } else if (throwable is TimeoutException) {
            errorMsg = resources.getString(R.string.error_msg_timeout)
        }

        return errorMsg
    }


    protected fun hideErrorView() {
        if (errorLayout.visibility == View.VISIBLE) {
            errorLayout.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
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
         * @return A new instance of fragment MoviesFragment.
         */
        @JvmStatic
        fun newInstance() =
            MoviesFragment().apply {
                arguments = Bundle()
            }

        val TAG = "MainActivity"
    }
}
