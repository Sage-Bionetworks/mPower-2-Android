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

package org.sagebionetworks.research.mpower.tracking.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_medication_remove.medication_remove_back
import kotlinx.android.synthetic.main.dialog_medication_remove.medication_remove_detail
import kotlinx.android.synthetic.main.dialog_medication_remove.medication_remove_save
import kotlinx.android.synthetic.main.dialog_medication_remove.medication_remove_title
import org.sagebionetworks.research.mpower.R
import org.slf4j.LoggerFactory

class MedicationRemoveFragment : AppCompatDialogFragment() {

    private val logger = LoggerFactory.getLogger(MedicationRemoveFragment::class.java)

    companion object {
        val ARG_MED_TITLE = "ARG_MED_TITLE"

        fun newInstance(medicationTitle: String, listener: MedicationRemoveListener): MedicationRemoveFragment {
            val fragment = MedicationRemoveFragment()
            fragment.listener = listener
            val args = Bundle()
            args.putString(ARG_MED_TITLE, medicationTitle)
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var customView: View
    lateinit var listener: MedicationRemoveListener
    lateinit var medicationTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            medicationTitle = arguments?.getString(ARG_MED_TITLE) ?: ""
        } else {
            medicationTitle = savedInstanceState.getString(ARG_MED_TITLE) ?: ""
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        logger.debug("onCreateView()")
        return customView
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        logger.debug("onCreateDialog()")

        customView = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_medication_remove, null)

        return AlertDialog.Builder(context)
                .setView(customView)
                .create()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logger.debug("onViewCreated()")

        medication_remove_title.text = getString(R.string.medication_remove_title).format(medicationTitle)
        medication_remove_detail.text = getString(R.string.medication_remove_detail)

        medication_remove_back.setOnClickListener { _ ->
            dismiss()
        }

        medication_remove_save.setOnClickListener { _ ->
            listener.onRemoveMedicationConfirmed()
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ARG_MED_TITLE, medicationTitle)
    }
}

interface MedicationRemoveListener {
    fun onRemoveMedicationConfirmed()
}