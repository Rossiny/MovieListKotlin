package br.com.rossiny.movielist.ui.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import br.com.rossiny.movielist.ui.fragment.MoviesFragment

class HomeAdapter(private val myContext: Context,
                  fm: FragmentManager,
                  internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    var popularMovies: MoviesFragment? = null
    var favoriteMovies: MoviesFragment? = null

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment? {
        return when (position) {
            0 -> {
                if (popularMovies == null)
                    popularMovies = MoviesFragment.newInstance(isFavoriteView = false)
                popularMovies
            }
            1 -> {
                if (favoriteMovies == null)
                    favoriteMovies = MoviesFragment.newInstance(isFavoriteView = true)
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