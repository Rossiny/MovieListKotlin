package br.com.rossiny.data

import br.com.rossiny.domain.ApiCallback
import br.com.rossiny.domain.Movie

class MoviesRepository(
    private val moviePersistenceSource: MoviePersistenceSource,
    private val movieApiSource: MovieApiSource
) {

    fun getSavedMovies(apiCallback: ApiCallback<List<Movie>>) = moviePersistenceSource.getPersistedMovies(apiCallback)

    fun requestNewMovie(apiCallback: ApiCallback<List<Movie>>) {
        movieApiSource.getMoviesFromApi(apiCallback)
    }

}

interface MoviePersistenceSource {

    fun getPersistedMovies(apiCallback: ApiCallback<List<Movie>>)
    fun saveNewMovie(movie: Movie)

}

interface MovieApiSource {

    fun getMoviesFromApi(apiCallback: ApiCallback<List<Movie>>)

}