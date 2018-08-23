package org.sagebionetworks.research.mpower.tracking.fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingActiveTaskViewModel;

public class TrackingItemViewHolder extends RecyclerView.ViewHolder {
    private SelectionUIFormItemWidget widget;
    private TrackingItem trackingItem;
    private boolean selected;
    private TrackingActiveTaskViewModel<?, ?> viewModel;

    public TrackingItemViewHolder(final SelectionUIFormItemWidget itemView, TrackingActiveTaskViewModel<?, ?> viewModel) {
        super(itemView);
        this.viewModel = viewModel;
        this.widget = itemView;
        this.trackingItem = null;
        this.widget.setOnClickListener(view -> {
            // We only care about the user click if it occurred on a tracking item and not a section.
            if (this.trackingItem != null) {
                this.setSelected(!this.selected);
                if (this.selected) {
                    this.viewModel.itemSelected(this.trackingItem);
                } else {
                    this.viewModel.itemDeselected(this.trackingItem);
                }
            }
        });
        this.selected = false;
    }

    public void setContent(@NonNull TrackingItem trackingItem) {
        this.trackingItem = trackingItem;
        if (this.viewModel.isSelected(trackingItem)) {
            this.setSelected(true);
        } else {
            this.setSelected(false);
        }

        this.widget.getText().setTextSize(16f);
        this.setLabels(trackingItem.getIdentifier(), trackingItem.getDetail());
    }

    public void setContent(@NonNull TrackingSection trackingSection) {
        this.trackingItem = null;
        // A section cannot be selected.
        this.setSelected(false);
        this.widget.getText().setTextSize(20f);
        this.setLabels(trackingSection.getIdentifier(), trackingSection.getDetail());
    }

    private void setSelected(boolean selected) {
        if (this.selected != selected) {
            if (selected) {
                this.widget.getBackgroundView()
                        .setBackgroundColor(this.widget.getResources().getColor(R.color.royal400));
            } else {
                this.widget.getBackgroundView()
                        .setBackgroundColor(this.widget.getResources().getColor(R.color.appWhite));
            }

            this.selected = selected;
        }
    }

    private void setLabels(@NonNull String text, @Nullable String detail) {
        this.widget.getText().setText(text);
        if (detail != null) {
            this.widget.getDetail().setText(detail);
        } else {
            this.widget.getDetail().setVisibility(View.GONE);
        }
    }
}
