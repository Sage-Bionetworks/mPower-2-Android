package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.SortUtil;
import org.sagebionetworks.research.mpower.tracking.model.SelectionUIFormItem;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SelectionItemAdapter;
import org.sagebionetworks.research.mpower.tracking.recycler_view.SelectionItemViewHolder;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A SelectionFragment represents the screen where the user selects which Symptoms, Triggers, or Medications, apply to
 * them to narrow down the list of choices. The SelectionFragment allows a subclass to override which fragment is navigated
 * to next by overriding getNextFragment();
 * @param <ConfigType> The type of TrackingItemConfig.
 * @param <LogType> The type of TrackingItemLog.
 * @param <ViewModelType> The type of TrackingTaskViewModel.
 */
public abstract class SelectionFragment<ConfigType extends TrackingItemConfig, LogType extends TrackingItemLog,
                ViewModelType extends TrackingTaskViewModel<ConfigType, LogType>>
        extends RecyclerViewTrackingFragment<ConfigType, LogType, ViewModelType, SelectionItemAdapter> {

    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        this.title.setText(this.stepView.getSelectionInfo().getTitle());
        this.detail.setText(this.stepView.getSelectionInfo().getDetail());
        this.navigationActionBar.setActionButtonClickListener((actionButton -> {
            if (actionButton.getId() == R.id.rs2_step_navigation_action_forward) {
                if (getFragmentManager().getBackStackEntryCount() > 0) {
                    getFragmentManager().popBackStack();
                } else {
                    Fragment nextFragment = this.getNextFragment();
                    replaceWithFragment(nextFragment);
                }
            }
        }));

        return result;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        refreshNextButtonEnabled();
    }

    @Override
    @NonNull
    public SelectionItemAdapter initializeAdapter() {
        ImmutableList<SelectionUIFormItem> availableItems = ImmutableList.copyOf(
                SortUtil.getAvailableElementsSorted(viewModel.getAvailableElements().getValue()));
        Set<Integer> selectedIndices = getSelectedIndices(availableItems);
        return new SelectionItemAdapter(availableItems, getSelectionListener(), selectedIndices);
    }

    protected SelectionItemViewHolder.SelectionListener getSelectionListener() {
        SelectionItemViewHolder.SelectionListener selectionListener = (item, position) -> {
            if (viewModel.getActiveElementsById().getValue() == null) {
                return; // NPE guard
            }
            if (viewModel.getActiveElementsById().getValue().containsKey(item.getIdentifier())) {
                viewModel.itemDeselected(item);
            } else {
                viewModel.itemSelected(item);
            }

            adapter.toggleSelected(position);
            adapter.notifyItemChanged(position);
            refreshNextButtonEnabled();
        };
        return selectionListener;
    }

    private Set<Integer> getSelectedIndices(@NonNull ImmutableList<SelectionUIFormItem> availableItems) {
        if (viewModel.getActiveElementsById().getValue() == null) {
            return new HashSet<>();  // NPE Guard
        }
        Set<String> selectedIdentifiers = viewModel.getActiveElementsById().getValue().keySet();
        Set<Integer> selectedIndices = new HashSet<>();
        for (int i = 0; i < availableItems.size(); i++) {
            if (selectedIdentifiers.contains(availableItems.get(i).getIdentifier())) {
                selectedIndices.add(i);
            }
        }

        return selectedIndices;
    }

    @Override
    public int getLayoutId() {
        return R.layout.mpower2_selection_step;
    }

    /**
     * Returns the Fragment that should replace this fragment when the forward button is pressed.
     * @return the Fragment that should replace this fragment when the forward button is pressed.
     */
    public abstract TrackingFragment<?, ?, ?> getNextFragment();

    /**
     * Refreshes the enabled state of the next button.
     * It is enabled if we have at least 1 item selected, false otherwise.
     */
    protected void refreshNextButtonEnabled() {
        Map<String, ConfigType> selectedItemsMap = viewModel.getActiveElementsById().getValue();
        navigationActionBar.setForwardButtonEnabled(selectedItemsMap != null && !selectedItemsMap.isEmpty());
    }
}
