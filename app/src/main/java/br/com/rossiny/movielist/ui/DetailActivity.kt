package br.com.rossiny.movielist.ui

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import br.com.rossiny.domain.Movie
import br.com.rossiny.movielist.R
import br.com.rossiny.movielist.ui.adapter.CustomListAdapter
import br.com.rossiny.movielist.ui.fragment.MoviesFragment
import br.com.rossiny.movielist.utils.GenreName
import br.com.rossiny.movielist.utils.LocalGlideModule
import br.com.rossiny.movielist.utils.PreferencesHelper
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale.filter

class DetailActivity : AppCompatActivity() {

    lateinit var image: ImageView
    lateinit var name: TextView
    lateinit var overview: TextView
    lateinit var vote: TextView
    lateinit var genres: TextView
    lateinit var button: Button

    var movie: Movie? = null

    var movieList : MutableList<Movie> = mutableListOf()

    private lateinit var preferencesHelper: PreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        image = findViewById(R.id.image)
        name = findViewById(R.id.detail_title)
        overview = findViewById(R.id.overview)
        vote = findViewById(R.id.vote_average)
        genres = findViewById(R.id.genres)
        button = findViewById(R.id.button)

        preferencesHelper = PreferencesHelper(this)

        if (!preferencesHelper.results.isEmpty()) {
            val gson = Gson()
            movieList = gson.fromJson(preferencesHelper.results,
                object : TypeToken<MutableList<Movie>>() {}.type
            )
        }

        if (intent.hasExtra(INTENT_MOVIE_PARAM)) {
            movie = intent.getSerializableExtra(INTENT_MOVIE_PARAM) as Movie?
            movie?.also {
                fillScreen(it)
                button.text = if (isFavorite(it)) "Unlike" else "Like"
            }
        }

        button.setOnClickListener {
            movie?.let {
                if (isFavorite(it)) {
                    removeItem(it)
                } else {
                    movieList.plusAssign(it)
                }
                button.text = if (isFavorite(it)) "Unlike" else "Like"
            }
            saveData(movieList)
        }
    }

    private fun removeItem(movie: Movie) {
        movieList = movieList.filter {
                m -> m.id != movie.id
        }.toMutableList()
    }

    private fun isFavorite(movie: Movie) : Boolean {
        return movieList.any {
            m -> m.id == movie.id
        }
    }

    private fun saveData(movieList: MutableList<Movie>) {
        val gson = Gson()
        val results = gson.toJson(movieList)
        Log.d(MoviesFragment.TAG, "Movie count detail: " + movieList.size)
        preferencesHelper.results = results
    }

    private fun fillScreen(movie: Movie) {
        movie.also {
            it.posterPath?.let { url ->
                LocalGlideModule.loadImage(CustomListAdapter.BASE_URL_IMG + url, this).into(image)
            }

            name.text = it.title
            overview.text = it.overview
            vote.text = it.voteAverage.toString()
            val genresStr = it.genreIds.map { g -> GenreName.getGenreName(g) }.toString()
            genres.text = genresStr.substring(1, genresStr.length - 1)
            button.text = if (isFavorite(it)) "Unlike" else "Like"
        }
    }

    companion object {

        private const val INTENT_MOVIE_PARAM = "movie_param"

        fun newIntent(context: Context, movie: Movie): Intent {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(INTENT_MOVIE_PARAM, movie)
            return intent
        }
    }
}
