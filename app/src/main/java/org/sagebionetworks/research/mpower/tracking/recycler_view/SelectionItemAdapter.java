package org.sagebionetworks.research.mpower.tracking.recycler_view;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.SelectionUIFormItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.widget.SelectionUIFormItemWidget;

import java.util.HashSet;
import java.util.Set;

/**
 * Adapter for a Recycler view which produces items for the selection screen in a TrackingTask.
 */
public class SelectionItemAdapter extends RecyclerView.Adapter<SelectionItemViewHolder> {
    private final ImmutableList<SelectionUIFormItem> selectionItems;
    @NonNull
    private final SelectionItemViewHolder.SelectionListener selectionListener;
    private final Set<Integer> selectedIndices;

    public SelectionItemAdapter(@NonNull ImmutableList<SelectionUIFormItem> selectionItems,
            @NonNull SelectionItemViewHolder.SelectionListener selectionListener,
            @NonNull Set<Integer> selectedIndices) {
        this.selectionListener = selectionListener;
        this.selectedIndices = new HashSet<>(selectedIndices);
        this.selectionItems = selectionItems;
    }

    public void toggleSelected(int position) {
        if (!selectedIndices.contains(position)) {
            selectedIndices.add(position);
        } else {
            selectedIndices.remove(position);
        }
    }

    @NonNull
    @Override
    public SelectionItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        SelectionUIFormItemWidget widget = (SelectionUIFormItemWidget) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.mpower2_selection_view_holder, parent, false);
        return new SelectionItemViewHolder(widget, selectionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final SelectionItemViewHolder holder, final int position) {
        SelectionUIFormItem selectionUIFormItem = selectionItems.get(position);
        if (selectionUIFormItem instanceof TrackingSection) {
            TrackingSection trackingSection = (TrackingSection)selectionUIFormItem;
            holder.setContent(trackingSection);
        } else if (selectionUIFormItem instanceof TrackingItem) {
            TrackingItem trackingItem = (TrackingItem)selectionUIFormItem;
            boolean selected = selectedIndices.contains(position);
            holder.setContent(trackingItem, selected, position);
        }
    }

    @Override
    public int getItemCount() {
        return selectionItems.size();
    }
}
