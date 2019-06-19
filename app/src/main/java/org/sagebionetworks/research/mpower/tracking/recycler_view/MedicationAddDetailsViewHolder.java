package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;

import org.sagebionetworks.research.mpower.tracking.view_model.logs.MedicationLog;
import org.sagebionetworks.research.mpower.tracking.widget.MedicationAddDetailsWidget;

public class MedicationAddDetailsViewHolder extends ViewHolder {
    public interface MedicationAddDetailsListener {
        void onClick(@NonNull MedicationLog config, int position);
    }

    private MedicationAddDetailsWidget widget;
    @NonNull
    private MedicationAddDetailsListener medicationAddDetailsListener;

    public MedicationAddDetailsViewHolder(@NonNull final MedicationAddDetailsWidget itemView,
            @NonNull final MedicationAddDetailsListener medicationAddDetailsListener) {
        super(itemView);
        widget = itemView;
        this.medicationAddDetailsListener = medicationAddDetailsListener;
    }

    public void setContent(@NonNull MedicationLog config, int position) {
        widget.getTitle().setText(config.getIdentifier());
        widget.setOnClickListener(view -> medicationAddDetailsListener.onClick(config, position));
    }
}
