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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.DaggerAppCompatActivity
import org.sagebionetworks.research.mpower.util.ActivityTransitionUtil
import org.sagebionetworks.research.mpower.viewmodel.PassiveGaitViewModel
import org.slf4j.LoggerFactory

class EntryActivity : DaggerAppCompatActivity() {

    private val LOGGER = LoggerFactory.getLogger(EntryActivity::class.java)

    private var transitionMonitoringService: TransitionMonitoringService? = null

    private var isBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            transitionMonitoringService = (binder as TransitionMonitoringService.ServiceBinder).service
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            transitionMonitoringService = null
        }
    }

    private val passiveGaitViewModel: PassiveGaitViewModel by lazy {
        ViewModelProvider(this).get(PassiveGaitViewModel::class.java)
    }

    private val trackingTransitionsObserver = Observer<Boolean> { tracking ->
        LOGGER.debug("OBSERVER: $tracking")
        if (tracking) {
            if (!isBound) {
                LOGGER.debug("bindTransitionMonitoringService")
                val intent = Intent(this, TransitionMonitoringService::class.java)
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            }
        } else {
            unbindTransitionMonitoringService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.entry_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, EntryFragment())
                    .commitNow()
        }

        passiveGaitViewModel.trackTransitions.observe(this, trackingTransitionsObserver)
    }

    private fun onBackPressed(fm: androidx.fragment.app.FragmentManager): Boolean {
        for (frag in fm.fragments) {
            if (frag.isVisible) {
                val childFm = frag.childFragmentManager
                if (onBackPressed(childFm)) {
                    return true
                } else if (childFm.backStackEntryCount > 0) {
                    childFm.popBackStack()
                    return true
                }
            }
        }
        return false
    }

    /**
     * Override the default back behavior so that it works for nested fragments.
     */
    override fun onBackPressed() {
        if (onBackPressed(supportFragmentManager)) {
            return
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!ActivityTransitionUtil.activityRecognitionPermissionApproved(this)) {
            ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    ActivityTransitionUtil.PERMISSION_REQUEST_ACTIVITY_RECOGNITION
            )
        }
    }

    override fun onDestroy() {
        LOGGER.debug("onDestroy")
        unbindTransitionMonitoringService()
        super.onDestroy()
    }

    private fun unbindTransitionMonitoringService() {
        if (isBound) {
            LOGGER.debug("unbindTransitionMonitoringService")
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        val permissionResult = "Request code: $requestCode, Permissions: ${permissions.contentToString()}, " +
                "Results: ${grantResults.contentToString()}"
        LOGGER.debug("onRequestPermissionsResult(): $permissionResult")

        when (requestCode) {
            ActivityTransitionUtil.PERMISSION_REQUEST_ACTIVITY_RECOGNITION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // do nothing, because tracking is enabled by child component
                    LOGGER.debug("ACTIVITY RECOGNITION Permission Granted")
                } else {
                    LOGGER.debug("ACTIVITY RECOGNITION Permission Denied")
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
}
