package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.recycler_view.MedicationAddDetailsViewHolder.MedicationAddDetailsListener;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.MedicationConfig;
import org.sagebionetworks.research.mpower.tracking.widget.MedicationAddDetailsWidget;

import java.util.ArrayList;
import java.util.List;

public class MedicationAddDetailsAdapter extends Adapter<MedicationAddDetailsViewHolder> {
    @NonNull
    private MedicationAddDetailsListener medicationAddDetailsListener;
    @NonNull
    private List<MedicationConfig> unconfiguredElements;

    public MedicationAddDetailsAdapter(@NonNull final List<MedicationConfig> unconfiguredElements,
            @NonNull final MedicationAddDetailsListener medicationAddDetailsListener) {
        this.medicationAddDetailsListener = medicationAddDetailsListener;
        this.unconfiguredElements = new ArrayList<>(unconfiguredElements);
    }

    public void itemConfigured(int position) {
        unconfiguredElements.remove(position);
    }

    @NonNull
    @Override
    public MedicationAddDetailsViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        MedicationAddDetailsWidget widget = (MedicationAddDetailsWidget) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.mpower2_medication_add_details_view_holder, parent, false);
        return new MedicationAddDetailsViewHolder(widget, medicationAddDetailsListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final MedicationAddDetailsViewHolder holder, final int position) {
        holder.setContent(unconfiguredElements.get(position), position);
    }

    @Override
    public int getItemCount() {
        return unconfiguredElements.size();
    }
}
