package org.sagebionetworks.research.mpower.tracking.recycler_view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.fragment.TrackingFragment;
import org.sagebionetworks.research.mpower.tracking.model.SelectionUIFormItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SelectionItemViewHolder.SelectionListener;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.widget.SelectionUIFormItemWidget;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Adapter for a Recycler view which produces items for the selection screen in a TrackingTask.
 */
public class SelectionItemAdapter extends RecyclerView.Adapter<SelectionItemViewHolder> {
    private final ImmutableList<SelectionUIFormItem> selectionItems;
    private final SelectionItemViewHolder.SelectionListener selectionListener;

    public SelectionItemAdapter(@NonNull Map<TrackingSection, Set<TrackingItem>> selectionItems,
            @NonNull SelectionItemViewHolder.SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
        ImmutableList.Builder<SelectionUIFormItem> selectionItemsBuilder = new ImmutableList.Builder<>();
        for (Entry<TrackingSection, Set<TrackingItem>> entry : selectionItems.entrySet()) {
            selectionItemsBuilder.add(entry.getKey());
            selectionItemsBuilder.addAll(entry.getValue());
        }

        this.selectionItems = selectionItemsBuilder.build();
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
            holder.setContent(trackingItem);
        }
    }

    @Override
    public int getItemCount() {
        return selectionItems.size();
    }
}
