/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.mpower

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.base.Supplier
import com.google.common.collect.ImmutableMap
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_main.navigation
import org.sagebionetworks.research.mpower.history.HistoryItemFragment
import org.sagebionetworks.research.mpower.profile.MPowerProfileSettingsFragment
import org.sagebionetworks.research.mpower.tracking.TrackingTabFragment
import org.sagebionetworks.research.mpower.util.Debounce
import org.sagebionetworks.research.mpower.viewmodel.PassiveGaitViewModel
import org.sagebionetworks.research.sageresearch.dao.room.AppConfigRepository
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository
import org.sagebionetworks.research.sageresearch.profile.ProfileDataLoader
import org.sagebionetworks.research.sageresearch.profile.ProfileManager
import org.slf4j.LoggerFactory
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class MainFragment : DaggerFragment(), OnRequestPermissionsResultCallback {
    private val LOGGER = LoggerFactory.getLogger(MainFragment::class.java)

    // tag for identifying an instance of a fragment
    private val TAG_FRAGMENT_TRACKING = "tracking"
    private val TAG_FRAGMENT_PROFILE = "profile"
    private val TAG_FRAGMENT_HISTORY = "history"

    // Mapping of a tag to a creation method for a fragment
    private val FRAGMENT_TAG_TO_CREATOR = ImmutableMap.Builder<String, Supplier<androidx.fragment.app.Fragment>>()
            .put(TAG_FRAGMENT_TRACKING, Supplier { TrackingTabFragment() })
            .put(TAG_FRAGMENT_PROFILE, Supplier { MPowerProfileSettingsFragment() })
            .put(TAG_FRAGMENT_HISTORY, Supplier { HistoryItemFragment() })
            .build()

    // mapping of navigation IDs to a fragment tag
    private val FRAGMENT_NAV_ID_TO_TAG = ImmutableMap.Builder<Int, String>()
            .put(R.id.navigation_tracking, TAG_FRAGMENT_TRACKING)
            .put(R.id.navigation_profile, TAG_FRAGMENT_PROFILE)
            .put(R.id.navigation_history, TAG_FRAGMENT_HISTORY)
            .build()

    @Inject
    lateinit var taskLauncher: TaskLauncher

    @Inject
    lateinit var reportRepo: ReportRepository

    @Inject
    lateinit var appConfigRepo: AppConfigRepository

    lateinit var profileManager: ProfileManager

    private var debounceCheckPassiveGait = Debounce(500, this::checkPassiveGait)

    private lateinit var profileDataLoader: ProfileDataLoader

    private val passiveDataAllowedObserver = Observer<ProfileDataLoader> { profileDataLoader ->
        this.profileDataLoader = profileDataLoader
        debounceCheckPassiveGait.schedule()
    }

    private fun checkPassiveGait() {
        val passiveDataAllowed = profileDataLoader.getValueString("passiveDataAllowed")
        LOGGER.debug("--- checkPassiveGait: $passiveDataAllowed ---")
        passiveDataAllowed?.let { passiveDataAllowed ->
            if (passiveDataAllowed.toBoolean()) {
                passiveGaitViewModel.enableTracking()
            } else {
                passiveGaitViewModel.disableTracking()
            }
        } ?: run {
            passiveGaitViewModel.disableTracking()
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        showFragment(FRAGMENT_NAV_ID_TO_TAG[item.itemId])
        return@OnNavigationItemSelectedListener true
    }

    private val passiveGaitViewModel: PassiveGaitViewModel by lazy {
        ViewModelProvider(requireActivity()).get(PassiveGaitViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showFragment(TAG_FRAGMENT_TRACKING)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        profileManager = ProfileManager(reportRepo, appConfigRepo)
        profileManager.profileDataLoader().observe(viewLifecycleOwner, passiveDataAllowedObserver)
    }

    /**
     * Show the fragment specified by a certain tag. The fragment currently displayed in fragment_container is
     * detached from the UI. If this is the first time we are showing a fragment, an instance is created and added to
     * the fragment_container. If we have previously displayed a fragment, we retrieve it from the fragment manager
     * and re-attached to the UI.
     */
    fun showFragment(fragmentTag: String?) {
        if (fragmentTag == null) {
            LOGGER.warn("could not show fragment with null tag")
        }

        val fragmentTransaction = childFragmentManager.beginTransaction()

        val previousFragment = childFragmentManager
                .findFragmentById(R.id.fragment_container)
        if (previousFragment != null) {
            LOGGER.debug("detaching fragment with tag: {}", previousFragment.tag)
            fragmentTransaction.detach(previousFragment)
        }

        var nextFragment = childFragmentManager.findFragmentByTag(fragmentTag)
        if (nextFragment == null) {
            LOGGER.debug("no fragment found for tag: {}, creating a new one ", fragmentTag)
            val fragmentSupplier: Supplier<androidx.fragment.app.Fragment>? = FRAGMENT_TAG_TO_CREATOR[fragmentTag]
                    ?: FRAGMENT_TAG_TO_CREATOR[TAG_FRAGMENT_TRACKING]

            if (fragmentSupplier == null) {
                LOGGER.warn("no supplier found for fragment with tag: {}", fragmentTag)
                return
            }
            nextFragment = fragmentSupplier.get()

            fragmentTransaction
                    .add(R.id.fragment_container, nextFragment, fragmentTag)
        } else {
            LOGGER.debug("reattaching fragment with tag: {}", nextFragment.tag)
            fragmentTransaction.attach(nextFragment)
        }
        fragmentTransaction.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Due to a behavior issue in nested child fragments
        // We must call the onActivityResult on all the children
        for (fragment in childFragmentManager.fragments) {
            LOGGER.info("Calling onActivityResult for fragment " + fragment.id)
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        LOGGER.debug("MainFragment onResume")
        super.onResume()
    }

    override fun onPause() {
        LOGGER.debug("MainFragment onPause")
        // profileManager.profileDataLoader().removeObserver(passiveDataAllowedObserver)
        // NOTE: Do not disable passive gait tracking here.  This commented code is left here to allow
        // developers to disable for local testing.
        // passiveGaitViewModel.disableTracking()
        super.onPause()
    }
}
