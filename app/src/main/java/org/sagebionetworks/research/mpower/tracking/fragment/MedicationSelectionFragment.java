package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.mpower.tracking.SortUtil;
import org.sagebionetworks.research.mpower.tracking.model.SelectionUIFormItem;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SelectionItemAdapter;
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Subtype of SelectionFragment specific to the flow and data types of the Medication task.
 */
public class MedicationSelectionFragment extends SelectionFragment<MedicationLog, MedicationLog, MedicationTrackingTaskViewModel> {
    @NonNull
    public static MedicationSelectionFragment newInstance(@NonNull StepView step) {
        MedicationSelectionFragment fragment = new MedicationSelectionFragment();
        Bundle args = TrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @NonNull
    public SelectionItemAdapter initializeAdapter() {
        List<SelectionUIFormItem> availableItems = SortUtil.getAvailableElementsSorted(viewModel.getAvailableElements().getValue());
        if (viewModel.getActiveElementsById().getValue() != null) {
            Set<String> selectedIdentifiers = viewModel.getActiveElementsById().getValue().keySet();
            List<SelectionUIFormItem> unselectedAvailableItems = new LinkedList<>();
            for (int i = 0; i < availableItems.size(); i++) {
                SelectionUIFormItem item = availableItems.get(i);
                if (!selectedIdentifiers.contains(item.getIdentifier())) {
                    unselectedAvailableItems.add(item);
                }
            }
            availableItems = unselectedAvailableItems;
        }

        return new SelectionItemAdapter(ImmutableList.copyOf(availableItems), getSelectionListener(), new HashSet<Integer>());
    }

    @Override
    public TrackingFragment<?, ?, ?> getNextFragment() {
        return MedicationReviewFragment.Companion.newInstance(stepView, true);
    }
}
