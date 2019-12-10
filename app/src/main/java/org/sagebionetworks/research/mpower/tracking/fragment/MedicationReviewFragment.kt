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
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog
import org.sagebionetworks.research.presentation.model.interfaces.StepView

class MedicationReviewFragment : RecyclerViewTrackingFragment<MedicationLog, MedicationLog,
        MedicationTrackingTaskViewModel, MedicationReviewAdapter>(), RecordMedicationDialogFragment.Listener {


    companion object {

        val ARGUMENT_IS_SETUP = "isSetup"

        fun newInstance(step : StepView, isSetup : Boolean) : MedicationReviewFragment {
            val fragment = MedicationReviewFragment()
            val args = TrackingFragment.createArguments(step)
            args.putBoolean(ARGUMENT_IS_SETUP, isSetup)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val result = super.onCreateView(inflater, container, savedInstanceState)
        val isSetup = arguments?.getBoolean(ARGUMENT_IS_SETUP) ?: false
        if (isSetup) {
            title.setText(R.string.medication_add_details_title)
        } else {
            title.setText(R.string.medication_review_title)
        }
        detail.setText(R.string.medication_add)
        addMore?.visibility = View.GONE
        detail!!.setOnClickListener { _ ->
            val fragment = MedicationSelectionFragment.newInstance(stepView)
            addChildFragmentOnTop(fragment, "MedicationSelectionFragment")
        }

        navigationActionBar.skipButton.setText(R.string.medication_add_details_later)
        navigationActionBar.forwardButton.setText(R.string.button_save)
        navigationActionBar.setActionButtonClickListener{ actionButton ->
            if (isSetup && actionButton.id == R.id.rs2_step_navigation_action_forward) {
                val fragment = RecordMedicationDialogFragment.newInstance()
                fragment.setTargetFragment(this, 123)
                fragment.show(fragmentManager!!, "dialog")

            } else if (actionButton.id == R.id.rs2_step_navigation_action_forward
                    || actionButton.id == R.id.rs2_step_navigation_action_skip) {
                if (fragmentManager!!.backStackEntryCount > 0) {
                    //Pop the back stack to take user back to medication logging
                    fragmentManager!!.popBackStack()
                } else {
                    val fragment = MedicationLoggingFragment.newInstance(stepView)
                    replaceWithFragment(fragment)
                }
            }
        }

        return result
    }


    override fun onResume() {
        super.onResume()
        updateButtonState()
    }

    private fun updateButtonState() {
        val medicationConfigs =
                SortUtil.getActiveElementsSorted(viewModel.activeElementsById.value!!)
        if (medicationConfigs.indexOfFirst { !it.isConfigured } == -1) {
            //All configured
            navigationActionBar.skipButton.visibility = View.GONE
            navigationActionBar.setForwardButtonEnabled(true)
        } else {
            //Not all configured
            navigationActionBar.skipButton.visibility = View.VISIBLE
            navigationActionBar.setForwardButtonEnabled(false)
        }

    }

    override fun initializeAdapter(): MedicationReviewAdapter {
        val medicationReviewListener : MedicationReviewListener = object : MedicationReviewListener {
            override fun editButtonPressed(config: MedicationLog, position: Int) {
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

    override fun onRecordbuttonClicked() {
        val fragment = MedicationLoggingFragment.newInstance(stepView)
        replaceWithFragment(fragment)
    }

    override fun onSkipButtonClicked() {
        val loggingResult = viewModel.loggingCollection
        performTaskViewModel.addStepResult(loggingResult)
        performTaskFragment.goForward()
    }
}