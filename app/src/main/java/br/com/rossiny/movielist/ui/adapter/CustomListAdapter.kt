package br.com.rossiny.movielist.ui.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import br.com.rossiny.domain.Movie
import br.com.rossiny.movielist.R
import br.com.rossiny.movielist.ui.DetailActivity
import br.com.rossiny.movielist.utils.AdapterCallback
import br.com.rossiny.movielist.utils.LocalGlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target

open class CustomListAdapter internal constructor(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var movies: MutableList<Movie> = arrayListOf()

    private var isLoadingAdded = false
    private var retryPageLoad = false

    //private val mCallback: AdapterCallback = context as AdapterCallback

    private var errorMsg: String? = null

    val isEmpty: Boolean
        get() = itemCount == 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val viewHolder: RecyclerView.ViewHolder?
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> {
                val viewItem = inflater.inflate(R.layout.item_list, parent, false)
                viewHolder = MovieVH(viewItem)
            }
            else -> {
                val viewLoading = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingVH(viewLoading)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val movie = movies[position]

        movie.also {
            when (getItemViewType(position)) {

                ITEM -> {
                    val movieVH = holder as MovieVH
                    movieVH.mPosterImg.setOnClickListener {
                        context.startActivity(
                            DetailActivity.newIntent(
                                context,
                                movies[position]
                            )
                        )
                    }

                    movieVH.mContentTitle.text = context.getString(
                        R.string.title_year, movie.releaseDate?.substring(0, 4), movie.title)

                    // load movie thumbnail
                    movie.posterPath?.let {
                        LocalGlideModule.loadImage(BASE_URL_IMG + it, context)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any,
                                    target: Target<Drawable>,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    movieVH.mProgress.visibility = View.GONE
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable,
                                    model: Any,
                                    target: Target<Drawable>,
                                    dataSource: DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    // image ready, hide progress now
                                    movieVH.mProgress.visibility = View.GONE
                                    return false   // return false if you want Glide to handle everything else.
                                }
                            })
                            .into(movieVH.mPosterImg)
                    }
                }

                LOADING -> {
                    val loadingVH = holder as LoadingVH

                    if (retryPageLoad) {
                        loadingVH.mErrorLayout.visibility = View.VISIBLE
                        loadingVH.mProgressBar.visibility = View.GONE

                        loadingVH.mErrorTxt.text = if (errorMsg != null)
                            errorMsg
                        else
                            context.getString(R.string.error_msg_unknown)

                    } else {
                        loadingVH.mErrorLayout.visibility = View.GONE
                        loadingVH.mProgressBar.visibility = View.VISIBLE
                    }
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return movies.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == movies.size - 1 && isLoadingAdded)
            LOADING
        else ITEM
    }


    fun add(r: Movie) {
        movies.add(r)
        notifyItemInserted(movies.size - 1)
    }

    fun addAll(results: List<Movie>) {
        for (result in results) {
            add(result)
        }
        notifyDataSetChanged()
    }

    fun remove(r: Movie?) {
        val position = movies.indexOf(r)
        if (position > -1) {
            movies.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }


    fun addLoadingFooter() {
        isLoadingAdded = true
        add(Movie())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = movies.size - 1
        val result = getItem(position)

        if (result != null) {
            movies.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getItem(position: Int): Movie? {
        return movies[position]
    }

    fun getAllItems(): List<Movie> {
        return movies
    }

    fun showRetry(show: Boolean, errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(movies.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }

    fun setList(localMovies: List<Movie>) {
        movies = localMovies as MutableList<Movie>
        notifyDataSetChanged()
    }


    protected inner class MovieVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mContentTitle: TextView = itemView.findViewById(R.id.content_title)
        val mPosterImg: ImageView = itemView.findViewById(R.id.content_poster)
        val mProgress: ProgressBar = itemView.findViewById(R.id.movie_progress)
    }


    protected inner class LoadingVH(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mProgressBar: ProgressBar = itemView.findViewById(R.id.loadmore_progress)
        val mRetryBtn: ImageButton = itemView.findViewById(R.id.loadmore_retry)
        val mErrorTxt: TextView = itemView.findViewById(R.id.loadmore_errortxt)
        val mErrorLayout: LinearLayout = itemView.findViewById(R.id.loadmore_errorlayout)

        init {
            mRetryBtn.setOnClickListener(this)
            mErrorLayout.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            when (view.id) {
                R.id.loadmore_retry, R.id.loadmore_errorlayout -> {

                    showRetry(false, null)
                    //mCallback.retryPageLoad()
                }
            }
        }
    }

    companion object {

        // View Types
        private const val ITEM = 0
        private const val LOADING = 1

        const val BASE_URL_IMG = "https://image.tmdb.org/t/p/w200"
    }

}
