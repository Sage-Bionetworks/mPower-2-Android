package org.sagebionetworks.research.mpower.tracking.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_medication_list_empty.*
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper

import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.presentation.model.interfaces.StepView

class MedicationListEmptyFragment : TrackingFragment<MedicationLog, MedicationLog,
        MedicationTrackingTaskViewModel>() {

    companion object {
        fun newInstance(stepView: StepView): MedicationListEmptyFragment {
            val fragment = MedicationListEmptyFragment()
            val args = TrackingFragment.createArguments(stepView)
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_medication_list_empty
    }

    override fun onStart() {
        super.onStart()
        view?.let { ViewCompat.requestApplyInsets(it) }
        if (!viewModel.activeElementsById.value.isNullOrEmpty()) {
            replaceWithFragment(MedicationReviewFragment.newInstance(stepView, true));
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val topListener = SystemWindowHelper.getOnApplyWindowInsetsListener(SystemWindowHelper.Direction.TOP)
        button_select_meds.setOnClickListener { _ ->
            val fragment = MedicationSelectionFragment.newInstance(stepView)
            addChildFragmentOnTop(fragment, "Medication Selection")
        }

        rs2_step_navigation_action_cancel.setOnClickListener { this.performTaskFragment.cancelTask(false) }
        ViewCompat.setOnApplyWindowInsetsListener(rs2_step_navigation_action_cancel, topListener)

    }

}
