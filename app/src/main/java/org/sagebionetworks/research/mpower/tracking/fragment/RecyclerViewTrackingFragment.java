package org.sagebionetworks.research.mpower.tracking.fragment;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mobile_ui.widget.NavigationActionBar;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog;

import butterknife.BindView;

/**
 * A RecyclerViewTrackingFragment allows for easier creation of TrackingFragments that use a recycler view to display
 * their options. It requires a subclass to override initializeAdapter to give the recycler view it's adapter but handles
 * the logic of setting up the recycler view, and laying it out appropriately.
 * @param <ConfigType> The type of TrackingItemConfig.
 * @param <LogType> The type of TrackingItemLog.
 * @param <ViewModelType> The type of TrackingTaskViewModel.
 */
public abstract class RecyclerViewTrackingFragment
        <ConfigType extends TrackingItemConfig, LogType extends TrackingItemLog, ViewModelType extends TrackingTaskViewModel<ConfigType, LogType>>
        extends TrackingFragment<ConfigType, LogType, ViewModelType> {
    @BindView(R.id.rs2_recycler_view)
    protected RecyclerView recyclerView;
    @BindView(R.id.rs2_title)
    protected TextView title;
    @BindView(R.id.rs2_detail)
    protected TextView detail;
    @BindView(R.id.rs2_step_navigation_action_cancel)
    protected ActionButton cancelButton;
    @Nullable
    @BindView(R.id.rs2_step_navigation_action_add_more)
    protected ActionButton addMore;
    @BindView(R.id.rs2_step_navigation_action_bar)
    protected NavigationActionBar navigationActionBar;

    protected LayoutManager layoutManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        OnApplyWindowInsetsListener topInsetListener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
        ViewCompat.setOnApplyWindowInsetsListener(this.cancelButton, topInsetListener);
        this.layoutManager = this.initializeLayoutManager();
        this.recyclerView.setLayoutManager(this.layoutManager);
        this.recyclerView.setFocusable(false);
        // Enables fling scrolling
        ViewCompat.setNestedScrollingEnabled(this.recyclerView, false);
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Recycler view initialization deliberately in on start so up to date information is received from the view
        // model every time the fragment starts.
        this.recyclerView.setAdapter(this.initializeAdapter());
        ItemDecoration itemDecoration = this.initializeItemDecoration();
        if (itemDecoration != null) {
            this.recyclerView.addItemDecoration(itemDecoration);
        }

        ViewCompat.requestApplyInsets(this.getView());
    }

    /**
     * Initializes and returns the layout manager to use on the fragment's recycler view. The default behavior is to use a
     * linear layout manager.
     *
     * @return the layout manager to use on the fragments recycler view.
     */
    @NonNull
    public LayoutManager initializeLayoutManager() {
        return new LinearLayoutManager(this.recyclerView.getContext());
    }

    /**
     * Initializes and returns the item decoration to use on the fragment's recycler view. Can return null to use no
     * item decoration. The default behavior is to use a DividerItemDecoration with the form_step_divider drawable.
     *
     * @return the item decoration to use on the fragment's recycler view.
     */
    @Nullable
    public ItemDecoration initializeItemDecoration() {
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this.recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable drawable = this.getContext().getResources().getDrawable(R.drawable.form_step_divider);
        itemDecoration.setDrawable(drawable);
        return itemDecoration;
    }

    /**
     * Initializes and returns the Adapter to use on the fragment's recycler view.
     *
     * @return the Adapter to use on the fragment's recycler view.
     */
    @NonNull
    public abstract RecyclerView.Adapter<?> initializeAdapter();
}
