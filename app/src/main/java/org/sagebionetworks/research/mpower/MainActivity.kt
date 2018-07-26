package org.sagebionetworks.research.mpower

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.navigation

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, findOrCreateFragment(item))
                .commit()
        return@OnNavigationItemSelectedListener true
    }

    // TODO: fragment caching
    fun findOrCreateFragment(item: MenuItem): Fragment {
        when (item.itemId) {
            R.id.navigation_logging -> return LoggingFragment()

            R.id.navigation_history -> return HistoryFragment()

            R.id.navigation_insights -> return InsightsFragment()

            R.id.navigation_profile -> return ProfileFragment()

            else -> return LoggingFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, LoggingFragment())
                .commit()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}