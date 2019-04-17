package br.com.rossiny.usecases

import br.com.rossiny.data.MoviesRepository
import br.com.rossiny.domain.ApiCallback
import br.com.rossiny.domain.Movie

class RequestPopularMovies(private val moviesRepository: MoviesRepository) {

    fun call(apiCallback: ApiCallback<List<Movie>>) = moviesRepository.requestNewMovie(apiCallback)

}
