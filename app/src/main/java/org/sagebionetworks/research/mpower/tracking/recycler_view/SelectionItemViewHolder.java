package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.fragment.TrackingFragment;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.widget.PaddingUtil;
import org.sagebionetworks.research.mpower.tracking.widget.SelectionUIFormItemWidget;

/**
 * View Holder for the Selection options from the Selection Screen in a Tracking Task.
 */
public class SelectionItemViewHolder extends RecyclerView.ViewHolder {
    private SelectionUIFormItemWidget widget;

    private TrackingItem trackingItem;
    private LiveData<Boolean> selected;
    private TrackingTaskViewModel<? extends TrackingItemConfig, ?> viewModel;
    private TrackingFragment<?, ?, ?> trackingFragment;

    public SelectionItemViewHolder(final SelectionUIFormItemWidget itemView,
            TrackingTaskViewModel<? extends TrackingItemConfig, ?> viewModel, TrackingFragment<?, ?, ?> trackingFragment) {
        super(itemView);
        this.trackingFragment = trackingFragment;
        this.viewModel = viewModel;
        this.trackingItem = null;
        this.widget = itemView;
        this.widget.setOnClickListener(view -> {
            // We only care about the user click if it occurred on a tracking item and not a section.
            if (this.trackingItem != null) {
                if (this.selected.getValue() == null || !this.selected.getValue()) {
                    this.viewModel.itemSelected(this.trackingItem);
                } else {
                    this.viewModel.itemDeselected(this.trackingItem);
                }
            }
        });

        this.selected = Transformations.map(this.viewModel.getActiveElements(), elements -> {
            if (this.trackingItem != null) {
                for (TrackingItemConfig config : elements) {
                    if (config.getTrackingItem().getIdentifier().equals(this.trackingItem.getIdentifier())) {
                        // If there is an active config with the same identifier as this's content this is selected
                        return true;
                    }
                }
            }

            return false;
        });

        this.selected.observe(this.trackingFragment, this::updateSelected);
    }

    public void setContent(@NonNull TrackingItem trackingItem) {
        this.trackingItem = trackingItem;
        // Set the top padding for an item to 16.
        PaddingUtil.setTopPadding(this.widget.getText(), 16);
        this.widget.getText().setTextSize(16f);
        this.setLabels(trackingItem.getIdentifier(), trackingItem.getDetail());
    }

    public void setContent(@NonNull TrackingSection trackingSection) {
        this.trackingItem = null;
        // Increase the top padding for a section to 28.
        PaddingUtil.setTopPadding(this.widget.getText(), 28);
        this.widget.getText().setTextSize(20f);
        this.setLabels(trackingSection.getIdentifier(), trackingSection.getDetail());
    }

    private void updateSelected(@Nullable Boolean selected) {
        if (selected != null && selected) {
            this.widget.getBackgroundView()
                    .setBackgroundColor(this.widget.getResources().getColor(R.color.royal200));
        } else {
            this.widget.getBackgroundView()
                    .setBackgroundColor(this.widget.getResources().getColor(R.color.transparent));
        }

    }

    private void setLabels(@NonNull String text, @Nullable String detail) {
        this.widget.getText().setText(text);
        if (detail != null) {
            this.widget.getDetail().setText(detail);
            PaddingUtil.setBottomPadding(this.widget.getDetail(), 16);
            PaddingUtil.setBottomPadding(this.widget.getText(), 0);
        } else {
            this.widget.getDetail().setVisibility(View.GONE);
            PaddingUtil.setBottomPadding(this.widget.getText(), 16);
        }
    }

}
