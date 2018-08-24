package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingActiveTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.widget.PaddingUtil;
import org.sagebionetworks.research.mpower.tracking.widget.SelectionUIFormItemWidget;

public class SelectionItemViewHolder extends RecyclerView.ViewHolder {
    private SelectionUIFormItemWidget widget;
    private TrackingItem trackingItem;
    private boolean selected;
    private TrackingActiveTaskViewModel<?, ?> viewModel;

    public SelectionItemViewHolder(final SelectionUIFormItemWidget itemView, TrackingActiveTaskViewModel<?, ?> viewModel) {
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

        // Set the top padding for an item to 16.
        PaddingUtil.setTopPadding(this.widget.getText(), 16);
        this.widget.getText().setTextSize(16f);
        this.setLabels(trackingItem.getIdentifier(), trackingItem.getDetail());
    }

    public void setContent(@NonNull TrackingSection trackingSection) {
        this.trackingItem = null;
        // A section cannot be selected.
        this.setSelected(false);
        // Increase the top padding for a section to 28.
        PaddingUtil.setTopPadding(this.widget.getText(), 28);
        this.widget.getText().setTextSize(20f);
        this.setLabels(trackingSection.getIdentifier(), trackingSection.getDetail());
    }

    private void setSelected(boolean selected) {
        if (this.selected != selected) {
            if (selected) {
                this.widget.getBackgroundView()
                        .setBackgroundColor(this.widget.getResources().getColor(R.color.royal200));
            } else {
                this.widget.getBackgroundView()
                        .setBackgroundColor(this.widget.getResources().getColor(R.color.transparent));
            }

            this.selected = selected;
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
