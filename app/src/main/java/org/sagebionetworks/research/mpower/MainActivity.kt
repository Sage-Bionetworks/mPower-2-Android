package org.sagebionetworks.research.mpower

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_main.navigation
import org.sagebionetworks.research.mpower.history.HistoryFragment;
import org.sagebionetworks.research.mpower.insights.InsightsFragment;
import org.sagebionetworks.research.mpower.profile.ProfileFragment;
import org.sagebionetworks.research.mpower.tracking.TrackingFragment;

import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {
    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, findOrCreateFragment(item))
                .commit()
        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, TrackingFragment())
                .commit()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    // TODO: fragment caching, don't create a new one each time, ask FragmentManager if it has an instance already
    fun findOrCreateFragment(item: MenuItem): Fragment {
        return when (item.itemId) {
            R.id.navigation_tracking -> TrackingFragment()

            R.id.navigation_history -> HistoryFragment()

            R.id.navigation_insights -> InsightsFragment()

            R.id.navigation_profile -> ProfileFragment()

            else -> TrackingFragment()
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentInjector
    }
}