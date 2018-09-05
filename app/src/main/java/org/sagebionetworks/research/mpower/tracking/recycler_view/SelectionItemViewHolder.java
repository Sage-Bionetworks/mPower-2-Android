package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.fragment.TrackingFragment;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.widget.SelectionUIFormItemWidget;

/**
 * View Holder for the Selection options from the Selection Screen in a Tracking Task.
 */
public class SelectionItemViewHolder extends RecyclerView.ViewHolder {
    private SelectionUIFormItemWidget widget;

    private TrackingItem trackingItem;

    private LiveData<Boolean> selected;

    private int marginTrackingItem;

    private int marginTrackingSectionTop;

    private TrackingTaskViewModel<? extends TrackingItemConfig, ?> viewModel;

    private TrackingFragment<?, ?, ?> trackingFragment;

    public SelectionItemViewHolder(final SelectionUIFormItemWidget itemView,
            TrackingTaskViewModel<? extends TrackingItemConfig, ?> viewModel,
            TrackingFragment<?, ?, ?> trackingFragment) {
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

        this.marginTrackingItem = this.widget.getResources().getDimensionPixelSize(R.dimen.margin_tracking_item);
        this.marginTrackingSectionTop = this.widget.getResources()
                .getDimensionPixelSize(R.dimen.margin_tracking_section_top);
    }

    public void setContent(@NonNull TrackingItem trackingItem) {
        this.trackingItem = trackingItem;
        TextView text = this.widget.getText();
        text.setPadding(text.getPaddingLeft(), this.marginTrackingItem, text.getPaddingRight(), text.getPaddingBottom());
        text.setTextSize(16f);
        this.setLabels(trackingItem.getIdentifier(), trackingItem.getDetail());
        this.setupSelectedObserver();
    }

    public void setContent(@NonNull TrackingSection trackingSection) {
        this.trackingItem = null;
        // Increase the top padding for a section to 28.
        TextView text = this.widget.getText();
        text.setPadding(text.getPaddingLeft(), this.marginTrackingSectionTop, text.getPaddingRight(), text.getPaddingBottom());
        text.setTextSize(20f);
        this.setLabels(trackingSection.getIdentifier(), trackingSection.getDetail());
        this.setupSelectedObserver();
    }

    /**
     * Sets up the observer for whether or not this item is selected.
     */
    private void setupSelectedObserver() {
        this.selected = Transformations.map(this.viewModel.getActiveElement(this.trackingItem.getIdentifier()),
                activeElement -> activeElement != null);
        this.selected.observe(this.trackingFragment, this::updateSelected);
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
        TextView detailLabel = this.widget.getDetail();
        TextView textLabel = this.widget.getText();
        if (detail != null) {
            detailLabel.setText(detail);
            detailLabel.setPadding(detailLabel.getPaddingLeft(), detailLabel.getPaddingTop(), detailLabel.getPaddingRight(), this.marginTrackingItem);
            textLabel.setPadding(textLabel.getPaddingLeft(), textLabel.getPaddingTop(), textLabel.getPaddingRight(), 0);
        } else {
            detailLabel.setVisibility(View.GONE);
            textLabel.setPadding(textLabel.getPaddingLeft(), textLabel.getPaddingTop(), textLabel.getPaddingRight(), this.marginTrackingItem);
        }
    }

}
