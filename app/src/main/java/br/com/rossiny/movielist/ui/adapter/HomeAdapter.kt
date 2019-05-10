package br.com.rossiny.movielist.ui.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import br.com.rossiny.movielist.ui.fragment.FavoriteMoviesFragment
import br.com.rossiny.movielist.ui.fragment.MoviesFragment
import br.com.rossiny.movielist.ui.fragment.PopularMoviesFragment

class HomeAdapter(private val myContext: Context,
                  fm: FragmentManager,
                  internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    var popularMovies: PopularMoviesFragment? = null
    var favoriteMovies: FavoriteMoviesFragment? = null

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> {
                if (popularMovies == null)
                    popularMovies = PopularMoviesFragment.newInstance()
                popularMovies
            }
            1 -> {
                if (favoriteMovies == null)
                    favoriteMovies = FavoriteMoviesFragment.newInstance()
                favoriteMovies
            }
            else -> null
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}