package org.sagebionetworks.research.mpower.tracking.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.sagebionetworks.research.mpower.R
import org.sagebionetworks.research.mpower.tracking.SortUtil
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationReviewAdapter
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationReviewListener
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.presentation.model.interfaces.StepView

class MedicationReviewFragment : RecyclerViewTrackingFragment<MedicationConfig, MedicationLog,
        MedicationTrackingTaskViewModel, MedicationReviewAdapter>() {

    companion object {
        fun newInstance(step : StepView) : MedicationReviewFragment {
            val fragment = MedicationReviewFragment()
            val args = TrackingFragment.createArguments(step)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val result = super.onCreateView(inflater, container, savedInstanceState)
        title.setText(R.string.medication_review_title)
        detail.visibility = View.GONE
        addMore!!.setText(R.string.medication_add_more)
        addMore!!.setOnClickListener { _ ->
            val fragment = MedicationSelectionFragment.newInstance(stepView)
            replaceWithFragment(fragment)
        }

        navigationActionBar.setActionButtonClickListener{ actionButton ->
            if (actionButton.id == R.id.rs2_step_navigation_action_forward) {
                val fragment = MedicationLoggingFragment.newInstance(stepView)
                replaceWithFragment(fragment)
            }
        }

        return result
    }

    override fun initializeAdapter(): MedicationReviewAdapter {
        val medicationReviewListener : MedicationReviewListener = object : MedicationReviewListener {
            override fun editButtonPressed(config: MedicationConfig, position: Int) {
                val schedulingFragment = MedicationSchedulingFragment.newInstance(stepView, config.identifier)
                addChildFragmentOnTop(schedulingFragment, "MedicationReviewFragment")
            }
        }

        val medicationConfigs =
                SortUtil.getActiveElementsSorted(viewModel.activeElementsById.value!!)
        return MedicationReviewAdapter(medicationConfigs, medicationReviewListener)
    }

    override fun getLayoutId(): Int {
        return R.layout.mpower2_logging_step
    }
}