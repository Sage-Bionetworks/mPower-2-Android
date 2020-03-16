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

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.common.base.Supplier
import com.google.common.collect.ImmutableMap
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_main.navigation
import org.sagebionetworks.research.mpower.history.HistoryItemFragment
import org.sagebionetworks.research.mpower.profile.MPowerProfileSettingsFragment
import org.sagebionetworks.research.mpower.tracking.TrackingTabFragment
import org.slf4j.LoggerFactory
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class MainFragment : DaggerFragment(), OnRequestPermissionsResultCallback {
    private val LOGGER = LoggerFactory.getLogger(MainFragment::class.java)

    private val runningQOrLater = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    private var activityTrackingEnabled = false
    private val activityTransitions: MutableList<ActivityTransition> by lazy {
        val transitions = mutableListOf<ActivityTransition>()
        transitions.add(
                ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build()
        )
        transitions.add(
                ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build()
        )
        transitions.add(
                ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build()
        )
        transitions.add(
                ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                        .build()
        )
        transitions
    }

    private lateinit var pendingIntent: PendingIntent
    // private val transitionsReceiver = ActivityTransitionsReceiver()

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

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        showFragment(FRAGMENT_NAV_ID_TO_TAG[item.itemId])
        return@OnNavigationItemSelectedListener true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPendingIntentForActivityTransitions()
    }

    private fun setupPendingIntentForActivityTransitions() {
        LOGGER.debug("setupPendingIntentForActivityTransitions")
        val intent = Intent(requireContext().applicationContext, ActivityTransitionsReceiver::class.java)
        intent.action = ActivityTransitionsReceiver.INTENT_ACTION
        pendingIntent = PendingIntent.getBroadcast(requireContext().applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

//        val intent = Intent(ActivityTransitionsReceiver.INTENT_ACTION)
//        pendingIntent = PendingIntent.getBroadcast(requireContext().applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showFragment(TAG_FRAGMENT_TRACKING)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
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

    /**
     * Registers callbacks for [ActivityTransition] events via a custom
     * [BroadcastReceiver]
     */
    private fun enableActivityTransitions() {
        LOGGER.debug("enableActivityTransitions()")
        val request = ActivityTransitionRequest(activityTransitions)
        val task: Task<Void> = ActivityRecognition.getClient(requireActivity())
                .requestActivityUpdates(3000, pendingIntent)
                // .requestActivityTransitionUpdates(request, pendingIntent)
        // NOTE: Using activity updates for now.  Activity transitions are very delayed.  In testing, I end up walking
        // around and sitting down before the first transition event even makes it to the BroadcastReceiver

        task.addOnSuccessListener {
            activityTrackingEnabled = true
            LOGGER.debug("Transitions Api was successfully registered.")
        }
        task.addOnFailureListener { e ->
            LOGGER.error("Transitions Api could NOT be registered: $e", e)
        }
    }

    /**
     * Unregisters callbacks for [ActivityTransition] events via a custom
     * [BroadcastReceiver]
     */
    private fun disableActivityTransitions() {
        LOGGER.debug("disableActivityTransitions()")
        val task: Task<Void> = ActivityRecognition.getClient(requireActivity())
                .removeActivityUpdates(pendingIntent)
                // .removeActivityTransitionUpdates(pendingIntent)

        task.addOnSuccessListener {
            activityTrackingEnabled = false
                    LOGGER.debug("Transitions successfully unregistered.")
        }
        .addOnFailureListener { e ->
            LOGGER.error("Transitions could not be unregistered: $e", e)
        }
    }

    /**
     * On devices Android 10 and beyond (29+), you need to ask for the ACTIVITY_RECOGNITION via the
     * run-time permissions.
     */
    private fun activityRecognitionPermissionApproved(): Boolean {
        return if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        } else {
            true
        }
    }

    private fun initTracking() {
        if (activityRecognitionPermissionApproved()) {
            toggleTracking()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                PERMISSION_REQUEST_ACTIVITY_RECOGNITION
            )
        }
    }

    private fun toggleTracking() {
        if (activityTrackingEnabled) {
            disableActivityTransitions()
        } else {
            enableActivityTransitions()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        val permissionResult = "Request code: $requestCode, Permissions: ${permissions.contentToString()}, Results: ${grantResults.contentToString()}"
        LOGGER.debug("onRequestPermissionsResult(): $permissionResult")

        when (requestCode) {
            PERMISSION_REQUEST_ACTIVITY_RECOGNITION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    toggleTracking()
                } else {
                    // TODO: Handle or ignore?
                    // requireActivity().finish()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        initTracking()
    }

    override fun onPause() {
        if (activityTrackingEnabled) {
            disableActivityTransitions()
        }
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    companion object {
        private const val PERMISSION_REQUEST_ACTIVITY_RECOGNITION = 45
    }
}
