package org.sagebionetworks.research.mpower.tracking.fragment;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModelFactory;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.sagebionetworks.research.presentation.perform_task.PerformTaskViewModel;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

/**
 * A TrackingFragment is an Fragment used in the TrackingTask. This class provides functionality common to all fragments
 * used in the Tracking Tasks such as getting access to the shared view model.
 * @param <ConfigType> The type of TrackingItemConfig.
 * @param <LogType> The type of TrackingItemLog.
 * @param <ViewModelType> The type of TrackingTaskViewModel.
 */
public abstract class TrackingFragment
        <ConfigType extends TrackingItemConfig, LogType extends TrackingItemLog, ViewModelType extends TrackingTaskViewModel<ConfigType, LogType>>
        extends Fragment  {
    public static final String ARGUMENT_STEP_VIEW = "stepView";

    protected PerformTaskViewModel performTaskViewModel;
    protected PerformTaskFragment performTaskFragment;
    protected TrackingStepView stepView;
    protected ViewModelType viewModel;
    protected Unbinder unbinder;

    @Inject
    protected TrackingTaskViewModelFactory trackingActiveTaskViewModelFactory;

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

        // Find PerformTaskFragment
        Fragment parentFragment = getParentFragment();
        while (parentFragment != null) {
            if (parentFragment instanceof PerformTaskFragment) {
                performTaskFragment = (PerformTaskFragment)parentFragment;
                this.performTaskViewModel = ViewModelProviders
                        .of(performTaskFragment).get(PerformTaskViewModel.class);
                parentFragment = null;  // break out of the loop
            } else {
                parentFragment = parentFragment.getParentFragment();
            }
        }

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
                (ViewModelType) ViewModelProviders.of(this.getParentFragment(),
                        this.trackingActiveTaskViewModelFactory.create(this.stepView, performTaskViewModel.getTaskResult()))
                        .get(TrackingTaskViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(this.getLayoutId(), container, false);
        this.unbinder = ButterKnife.bind(this, result);
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
        // This call will only transition to another fragment on initial load of the view model
        // It must be in onStart(), so that if we do transition fragment, everything will be loaded
        // and ready to do an add child or replace fragment transition.
        this.viewModel.proceedToInitialFragment(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.unbinder.unbind();
    }

    /**
     * Adds a child fragment on top of this fragment and adds this fragment to the back stack with the provided tag.
     * @param childFragment The fragment to add on top of this fragment.
     * @param tag The tag for this fragment on the back stack.
     */
    public void addChildFragmentOnTop(Fragment childFragment, @Nullable String tag) {
        getFragmentManager()
                .beginTransaction()
                .detach(this)
                .add(((ViewGroup)this.getView().getParent()).getId(), childFragment)
                .addToBackStack(tag)
                .commit();
    }

    /**
     * Replaces this fragment with the given fragment. Doesn't add the transaction to the back stack.
     * @param fragment The fragment to replace this fragment with.
     */
    public void replaceWithFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(((ViewGroup)this.getView().getParent()).getId(), fragment)
                .commit();
    }

    /**
     * Returns the id of the layout to inflate for this fragment.
     *
     * @return the id of the layout to inflate for this fragment.
     */
    @LayoutRes
    public abstract int getLayoutId();
}
