package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.SortUtil;
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationReviewAdapter;
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationReviewViewHolder.MedicationReviewListener;
import org.sagebionetworks.research.mpower.tracking.view_model.MedicationTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

import java.util.List;

public class MedicationReviewFragment extends RecyclerViewTrackingFragment<MedicationConfig, SimpleTrackingItemLog,
                MedicationTrackingTaskViewModel, MedicationReviewAdapter> {
    @NonNull
    public static MedicationReviewFragment newInstance(@NonNull StepView step) {
        MedicationReviewFragment fragment = new MedicationReviewFragment();
        Bundle args = TrackingFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,  ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        title.setText(R.string.medication_review_title);
        detail.setVisibility(View.GONE);
        addMore.setText(R.string.medication_add_more);
        return result;
    }

    @Override
    public int getLayoutId() {
        return R.layout.mpower2_logging_step;
    }

    @NonNull
    @Override
    public MedicationReviewAdapter initializeAdapter() {
        MedicationReviewListener medicationReviewListener = (config, position) -> {
            // TODO rkolmos 09/13/2018 implement the listener.
        };

        List<MedicationConfig> medicationConfigs =
                SortUtil.getActiveElementsSorted(viewModel.getActiveElementsById().getValue());
        return new MedicationReviewAdapter(medicationConfigs, medicationReviewListener);
    }
}
