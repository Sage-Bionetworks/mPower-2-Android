package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;

import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig;
import org.sagebionetworks.research.mpower.tracking.widget.MedicationReviewWidget;

public class MedicationReviewViewHolder extends ViewHolder{
    public interface MedicationReviewListener {
        void editButtonPressed(@NonNull MedicationConfig config, int position);
    }

    private MedicationReviewWidget widget;
    private MedicationReviewListener medicationReviewListener;

    public MedicationReviewViewHolder(@NonNull final MedicationReviewWidget itemView,
            @NonNull final MedicationReviewListener medicationReviewListener) {
        super(itemView);
        widget = itemView;
    }

    public void setContent(@NonNull final MedicationConfig config, int position) {
        widget.getTitle().setText(config.getIdentifier());
        // TODO rkolmos 09/13/2018 set the times label to the appropriate text.
        // TODO rkolmos 09/13/2018 set the days label to the appripriate text.
        widget.getEditButton().setOnClickListener(view -> medicationReviewListener.editButtonPressed(config, position));
    }
}
