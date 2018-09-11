package org.sagebionetworks.research.mpower.tracking.fragment;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;

import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;

public class MedicationAddDetailsFragment extends
        RecyclerViewTrackingFragment<SimpleTrackingItemConfig, SimpleTrackingItemLog, SimpleTrackingTaskViewModel> {

    @Override
    public int getLayoutId() {
        return 0;
    }

    @NonNull
    @Override
    public Adapter<?> initializeAdapter() {
        return null;
    }
}
