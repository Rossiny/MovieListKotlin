package br.com.rossiny.movielist.framework

import br.com.rossiny.data.MoviePersistenceSource
import br.com.rossiny.domain.ApiCallback
import br.com.rossiny.domain.Movie

class LocalMoviePersistenceSource : MoviePersistenceSource {

    private var movies : List<Movie> = emptyList()

    override fun getPersistedMovies(apiCallback: ApiCallback<List<Movie>>) {

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveNewMovie(movie: Movie) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}