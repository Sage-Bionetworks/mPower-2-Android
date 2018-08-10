package org.sagebionetworks.research.mpower

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
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
import org.sagebionetworks.research.mpower.authentication.ExternalIdSignInActivity
import org.sagebionetworks.research.mpower.profile.ProfileFragment
import org.sagebionetworks.research.mpower.tracking.TrackingFragment
import org.slf4j.LoggerFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity(), HasSupportFragmentInjector {
    private val LOGGER = LoggerFactory.getLogger(MainActivity::class.java)
    private val CONSENT_URI = Uri.parse("http://mpower.sagebridge.org/study/intro")
    private val SIGNUP_TASK_ID = "Signup"

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var mainViewModelProviderFactory: MainViewModelFactory

    // TODO inject this dependency, ideally switch to TaskLauncher @liujoshua 2018/08/06
    @Inject
    lateinit var taskLauncher: TaskLauncher

    private lateinit var mainViewModel: MainViewModel

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, findOrCreateFragment(item))
                .commit()
        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel = ViewModelProviders.of(this, mainViewModelProviderFactory.create())
                .get(MainViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        if (!mainViewModel.isAuthenticated) {
            showSignUpActivity()
        } else if (!mainViewModel.isConsented) {
            // FIXME check for local consent pending upload @liujoshua 2018/08/06
            showConsentActivity()
        } else {
            showMainActivityLayout()
        }
    }

    fun showSignUpActivity() {
        LOGGER.debug("Showing sign up activity")

        startActivity(Intent(Intent(this, ExternalIdSignInActivity::class.java)))
    }

    fun showConsentActivity() {
        LOGGER.debug("Showing consent activity")

        // TODO use ChromeTab @liujoshua 2018/08/06
        val browserIntent = Intent(Intent.ACTION_VIEW, CONSENT_URI)
        startActivity(browserIntent)
    }

    fun showMainActivityLayout() {
        LOGGER.debug("Showing main activity")

        supportFragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, TrackingFragment())
                .commit()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    // TODO: fragment caching, don't create a new one each time, ask FragmentManager if it has an instance already
    // @liujoshua 2018/08/06
    fun findOrCreateFragment(item: MenuItem): Fragment {
        return when (item.itemId) {
            R.id.navigation_tracking -> TrackingFragment()

//            R.id.navigation_history -> HistoryFragment()
//
//            R.id.navigation_insights -> InsightsFragment()

            R.id.navigation_profile -> ProfileFragment()

            else -> TrackingFragment()
        }
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentInjector
    }
}