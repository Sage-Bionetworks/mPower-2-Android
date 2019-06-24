package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.SortUtil;
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationAddDetailsAdapter;
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationAddDetailsViewHolder.MedicationAddDetailsListener;
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MedicationAddDetailsFragment extends
        RecyclerViewTrackingFragment<MedicationLog, MedicationLog, MedicationTrackingTaskViewModel,
                MedicationAddDetailsAdapter> {
    @NonNull
    public static MedicationAddDetailsFragment newInstance(@NonNull StepView step) {
        MedicationAddDetailsFragment fragment = new MedicationAddDetailsFragment();
        Bundle args = TrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        title.setText(R.string.medication_add_details_title);
        detail.setText(R.string.medication_add_details_detail);
        if (addMore != null) {
            addMore.setText(R.string.medication_add_more);
            addMore.setOnClickListener(view -> {
                MedicationSelectionFragment fragment = MedicationSelectionFragment.newInstance(stepView);
                replaceWithFragment(fragment);
            });
        }

        navigationActionBar.setActionButtonClickListener(actionButton -> {
            if (actionButton.getId() == R.id.rs2_step_navigation_action_forward) {
                List<MedicationLog> unconfiguredElements = getActiveElements();
                if (unconfiguredElements.isEmpty()) {
                    MedicationReviewFragment fragment = MedicationReviewFragment.Companion.newInstance(stepView, false);
                    replaceWithFragment(fragment);
                } else {
                    MedicationLog nextConfig = unconfiguredElements.get(0);
                    MedicationSchedulingFragment fragment =
                            MedicationSchedulingFragment.Companion.newInstance(stepView, nextConfig.getIdentifier());
                    addChildFragmentOnTop(fragment, "medicationAddDetails");
                }
            }
        });

        return result;
    }

    @Override
    public void onStart() {
        super.onStart();

        Map<String, MedicationLog> activeElementsMap =
                viewModel.getActiveElementsById().getValue();
        if (activeElementsMap == null) {
            return;
        }
        List<MedicationLog> sortedElements =
                SortUtil.getActiveElementsSorted(activeElementsMap);
        for (MedicationLog config : sortedElements) {
            if (!config.isConfigured()) {
                return;  // still need to configure some elements, stay on this screen
            }
        }
        replaceWithFragment(MedicationReviewFragment.Companion.newInstance(stepView, false));
    }

    @Override
    public int getLayoutId() {
        return R.layout.mpower2_logging_step;
    }

    @NonNull
    @Override
    public MedicationAddDetailsAdapter initializeAdapter() {
        MedicationAddDetailsListener medicationAddDetailsListener = (config, position) -> {
            MedicationSchedulingFragment fragment
                    = MedicationSchedulingFragment.Companion.newInstance(stepView, config.getIdentifier());
            addChildFragmentOnTop(fragment, "Scheduling");
            adapter.itemConfigured(position);
            adapter.notifyDataSetChanged();
        };

        List<MedicationLog> unconfiguredElements = getActiveElements();
        return new MedicationAddDetailsAdapter(unconfiguredElements, medicationAddDetailsListener);
    }

    private List<MedicationLog> getActiveElements() {
        List<MedicationLog> result = new ArrayList<>();
        if (viewModel.getActiveElementsById().getValue() != null) {
            result.addAll(SortUtil.getActiveElementsSorted(viewModel.getActiveElementsById().getValue()));
        }
        return result;
    }
}
