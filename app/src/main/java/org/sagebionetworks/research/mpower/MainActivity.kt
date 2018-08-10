package org.sagebionetworks.research.mpower

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.google.common.base.Supplier
import com.google.common.collect.ImmutableMap
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

    private val TAG_FRAGMENT_TRACKING = "tracking"
    private val TAG_FRAGMENT_PROFILE = "profile"
    private val FRAGMENT_TAG_TO_CREATOR = ImmutableMap.Builder<String, Supplier<Fragment>>()
            .put(TAG_FRAGMENT_TRACKING, Supplier { TrackingFragment() })
            .put(TAG_FRAGMENT_PROFILE, Supplier { ProfileFragment() })
            .build()
    private val FRAGMENT_NAV_ID_TO_TAG = ImmutableMap.Builder<Int, String>()
            .put(R.id.navigation_tracking, TAG_FRAGMENT_TRACKING)
            .put(R.id.navigation_profile, TAG_FRAGMENT_PROFILE)
            .build()

    @Inject
    lateinit var fragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var mainViewModelProviderFactory: MainViewModelFactory

    // TODO inject this dependency, ideally switch to TaskLauncher @liujoshua 2018/08/06
    @Inject
    lateinit var taskLauncher: TaskLauncher

    private lateinit var mainViewModel: MainViewModel

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        showFragment(item)
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

        showFragment(TAG_FRAGMENT_TRACKING)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    fun showFragment(item: MenuItem) {
        return showFragment(FRAGMENT_NAV_ID_TO_TAG[item.itemId])
    }

    fun showFragment(fragmentTag: String?) {
        var fragmentTransaction = supportFragmentManager.beginTransaction()

        var previousFragment = supportFragmentManager
                .findFragmentById(R.id.fragment_container)
        if (previousFragment != null) {
            fragmentTransaction.detach(previousFragment)
        }

        var nextFragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        if (nextFragment == null) {
            val fragmentSupplier: Supplier<Fragment> = FRAGMENT_TAG_TO_CREATOR[fragmentTag]
                    ?: FRAGMENT_TAG_TO_CREATOR[TAG_FRAGMENT_TRACKING]!!
            nextFragment = fragmentSupplier.get()!!

            fragmentTransaction
                    .add(R.id.fragment_container, nextFragment, fragmentTag)
        } else {
            fragmentTransaction.attach(nextFragment)
        }
        fragmentTransaction.commit()
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return fragmentInjector
    }
}