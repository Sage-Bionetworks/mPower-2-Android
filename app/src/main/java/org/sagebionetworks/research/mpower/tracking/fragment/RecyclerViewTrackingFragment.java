package org.sagebionetworks.research.mpower.tracking.fragment;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingActiveTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingActiveTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingActiveTaskViewModelFactory;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public abstract class RecyclerViewTrackingFragment
        <ConfigType extends TrackingItemConfig, LogType extends TrackingItemLog, ViewModelType extends TrackingActiveTaskViewModel<ConfigType, LogType>>
        extends Fragment {
    public static final String ARGUMENT_STEP_VIEW = "stepView";

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

    @Inject
    protected TrackingActiveTaskViewModelFactory trackingActiveTaskViewModelFactory;
    protected Unbinder unbinder;

    protected TrackingStepView stepView;
    protected ViewModelType viewModel;

    protected LayoutManager layoutManager;

    @NonNull
    public static Bundle createArguments(@NonNull StepView step) {
        checkNotNull(step);

        Bundle bundle = new Bundle();
        bundle.putSerializable(ARGUMENT_STEP_VIEW, step);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TrackingStepView stepView = null;
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                // noinspection unchecked
                stepView = (TrackingStepView)this.getArguments().getSerializable(ARGUMENT_STEP_VIEW);
            }
        } else {
            // noinspection unchecked
            stepView = (TrackingStepView)savedInstanceState.getSerializable(ARGUMENT_STEP_VIEW);
        }

        checkState(stepView != null, "stepView cannot be null");
        this.stepView = stepView;
        // noinspection unchecked
        this.viewModel =
                (ViewModelType) ViewModelProviders.of(this.getParentFragment(), this.trackingActiveTaskViewModelFactory.create(this.stepView))
                .get(SimpleTrackingActiveTaskViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(this.getLayoutId(), container, false);
        this.unbinder = ButterKnife.bind(this, result);
        OnApplyWindowInsetsListener topInsetListener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
        ViewCompat.setOnApplyWindowInsetsListener(this.cancelButton, topInsetListener);
        this.layoutManager = this.initializeLayoutManager();
        this.recyclerView.setLayoutManager(this.layoutManager);
        ItemDecoration itemDecoration = this.initializeItemDecoration();
        this.recyclerView.setFocusable(false);
        if (itemDecoration != null) {
            this.recyclerView.addItemDecoration(itemDecoration);
        }

        this.recyclerView.setAdapter(this.initializeAdapter());
        return result;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewCompat.requestApplyInsets(this.getView());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.unbinder.unbind();
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

    /**
     * Returns the id of the layout to inflate for this fragment.
     *
     * @return the id of the layout to inflate for this fragment.
     */
    @LayoutRes
    public abstract int getLayoutId();
}
