package org.sagebionetworks.research.mpower.tracking.fragment

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_button_list_dialog.*
import org.sagebionetworks.research.mpower.R

/**
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    RecordMedicationDialogFragment.newInstance().show(supportFragmentManager, "dialog")
 * </pre>
 *
 * You activity (or fragment) needs to implement [RecordMedicationDialogFragment.Listener].
 */
class RecordMedicationDialogFragment : BottomSheetDialogFragment() {
    private var mListener: Listener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_button_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rs2_step_navigation_action_bar.forwardButton.setText(R.string.medication_record)
        rs2_step_navigation_action_bar.skipButton.setText(R.string.medication_not_right_now)

        rs2_step_navigation_action_bar.setActionButtonClickListener { actionButton ->
            if (actionButton.id == R.id.rs2_step_navigation_action_forward) {
                mListener?.onRecordbuttonClicked()
            } else if (actionButton.id == R.id.rs2_step_navigation_action_skip) {
                mListener?.onSkipButtonClicked()
            }
            dismiss()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = targetFragment as Listener
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    interface Listener {
        fun onRecordbuttonClicked()
        fun onSkipButtonClicked()
    }



    companion object {

        fun newInstance(): RecordMedicationDialogFragment =
                RecordMedicationDialogFragment().apply {
                    arguments = Bundle().apply {

                    }
                }

    }
}
