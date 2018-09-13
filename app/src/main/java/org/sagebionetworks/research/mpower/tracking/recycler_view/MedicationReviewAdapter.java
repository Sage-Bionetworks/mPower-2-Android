package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationReviewViewHolder.MedicationReviewListener;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig;
import org.sagebionetworks.research.mpower.tracking.widget.MedicationReviewWidget;

import java.util.ArrayList;
import java.util.List;

public class MedicationReviewAdapter extends Adapter<MedicationReviewViewHolder> {
    private MedicationReviewListener medicationReviewListener;
    private List<MedicationConfig> medicationConfigs;

    public MedicationReviewAdapter(@NonNull final List<MedicationConfig> configs,
            @NonNull final MedicationReviewListener medicationReviewListener) {
        this.medicationReviewListener = medicationReviewListener;
        this.medicationConfigs = new ArrayList<>(configs);
    }

    @NonNull
    @Override
    public MedicationReviewViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        MedicationReviewWidget medicationReviewWidget = (MedicationReviewWidget)
                LayoutInflater.from(parent.getContext()).inflate(R.layout.medication_review_view_holder, parent, false);
        return new MedicationReviewViewHolder(medicationReviewWidget, medicationReviewListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final MedicationReviewViewHolder holder, final int position) {
        MedicationConfig config = medicationConfigs.get(position);
        holder.setContent(config, position);
    }

    @Override
    public int getItemCount() {
        return medicationConfigs.size();
    }
}
