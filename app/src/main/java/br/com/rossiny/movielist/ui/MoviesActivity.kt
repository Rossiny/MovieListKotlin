package br.com.rossiny.movielist.ui

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import br.com.rossiny.movielist.R
import br.com.rossiny.movielist.ui.adapter.HomeAdapter
import br.com.rossiny.movielist.ui.fragment.MoviesFragment

class MoviesActivity : AppCompatActivity(), MoviesFragment.OnFragmentInteractionListener {

    override fun onFragmentInteraction(uri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movies)

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)

        tabLayout.addTab(tabLayout.newTab().setText("Popular"))
        tabLayout.addTab(tabLayout.newTab().setText("Favorites"))
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        homeAdapter = HomeAdapter(this, supportFragmentManager, tabLayout.tabCount)
        viewPager.adapter = homeAdapter

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                homeAdapter.popularMovies?.onStart()
                homeAdapter.favoriteMovies?.onStart()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {

            }
            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

    }
}
