package org.sagebionetworks.research.mpower.tracking.fragment;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.SelectionUIFormItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingActiveTaskViewModel;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TrackingItemAdapter extends RecyclerView.Adapter<TrackingItemViewHolder> {
    private final ImmutableList<SelectionUIFormItem> selectionItems;
    private final TrackingActiveTaskViewModel<?, ?> viewModel;

    public TrackingItemAdapter(@NonNull Map<TrackingSection, Set<TrackingItem>> selectionItems,
            @NonNull TrackingActiveTaskViewModel<?, ?> viewModel) {
        this.viewModel = viewModel;
        ImmutableList.Builder<SelectionUIFormItem> selectionItemsBuilder = new ImmutableList.Builder<>();
        for (Entry<TrackingSection, Set<TrackingItem>> entry : selectionItems.entrySet()) {
            selectionItemsBuilder.add(entry.getKey());
            selectionItemsBuilder.addAll(entry.getValue());
        }

        this.selectionItems = selectionItemsBuilder.build();
    }

    @NonNull
    @Override
    public TrackingItemViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        SelectionUIFormItemWidget widget = (SelectionUIFormItemWidget) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.mpower2_selection_view_holder, parent, false);
        return new TrackingItemViewHolder(widget, this.viewModel);
    }

    @Override
    public void onBindViewHolder(@NonNull final TrackingItemViewHolder holder, final int position) {
        SelectionUIFormItem selectionUIFormItem = this.selectionItems.get(position);
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
        return this.selectionItems.size();
    }
}
