package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.widget.SelectionUIFormItemWidget;

/**
 * View Holder for the Selection options from the Selection Screen in a Tracking Task.
 */
public class SelectionItemViewHolder extends RecyclerView.ViewHolder {
    public interface SelectionListener {
        void itemTapped(@NonNull TrackingItem item, int position);
    }

    private SelectionUIFormItemWidget widget;

    private int marginTrackingItem;

    private int marginTrackingSectionTop;

    @NonNull
    private SelectionListener selectionListener;

    public SelectionItemViewHolder(final SelectionUIFormItemWidget itemView,
            @NonNull final SelectionListener selectionListener) {
        super(itemView);
        this.selectionListener = selectionListener;
        widget = itemView;
        marginTrackingItem = widget.getResources().getDimensionPixelSize(R.dimen.margin_tracking_item);
        marginTrackingSectionTop = widget.getResources()
                .getDimensionPixelSize(R.dimen.margin_tracking_section_top);
    }

    public void setContent(@NonNull TrackingItem trackingItem, boolean selected, int position) {
        TextView text = widget.getText();
        text.setPadding(text.getPaddingLeft(), marginTrackingItem, text.getPaddingRight(), text.getPaddingBottom());
        text.setTextSize(16f);
        setLabels(trackingItem.getIdentifier(), trackingItem.getDetail());
        updateSelected(selected);
        widget.setOnClickListener(view -> selectionListener.itemTapped(trackingItem, position));
    }

    public void setContent(@NonNull TrackingSection trackingSection) {
        // Increase the top padding for a section to 28.
        TextView text = widget.getText();
        text.setPadding(text.getPaddingLeft(), marginTrackingSectionTop, text.getPaddingRight(), text.getPaddingBottom());
        text.setTextSize(20f);
        setLabels(trackingSection.getIdentifier(), trackingSection.getDetail());
        // When a section is tapped nothing should happen.
        widget.setOnClickListener(null);
    }

    public void updateSelected(boolean selected) {
        if (selected) {
            widget.getBackgroundView()
                    .setBackgroundColor(widget.getResources().getColor(R.color.royal200));
        } else {
            widget.getBackgroundView()
                    .setBackgroundColor(widget.getResources().getColor(R.color.transparent));
        }

    }

    private void setLabels(@NonNull String text, @Nullable String detail) {
        widget.getText().setText(text);
        TextView detailLabel = widget.getDetail();
        TextView textLabel = widget.getText();
        if (detail != null) {
            detailLabel.setText(detail);
            detailLabel.setPadding(detailLabel.getPaddingLeft(), detailLabel.getPaddingTop(), detailLabel.getPaddingRight(),
                    marginTrackingItem);
            textLabel.setPadding(textLabel.getPaddingLeft(), textLabel.getPaddingTop(), textLabel.getPaddingRight(),
                    0);
        } else {
            detailLabel.setVisibility(View.GONE);
            textLabel.setPadding(textLabel.getPaddingLeft(), textLabel.getPaddingTop(), textLabel.getPaddingRight(),
                    marginTrackingItem);
        }
    }

}
