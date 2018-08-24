package org.sagebionetworks.research.mpower.tracking.fragment;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.model.TrackingSection;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStep;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingActiveTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.SimpleTrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingActiveTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingActiveTaskViewModelFactory;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dagger.android.support.AndroidSupportInjection;

public class SelectionFragment extends Fragment {
    public static final String ARGUMENT_STEP_VIEW = "stepView";
    @BindView(R.id.rs2_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.rs2_title)
    TextView title;
    @BindView(R.id.rs2_detail)
    TextView detail;
    @BindView(R.id.rs2_step_navigation_action_cancel)
    ActionButton cancelButton;

    private Unbinder unbinder;
    private TrackingStepView stepView;
    @Inject
    TrackingActiveTaskViewModelFactory trackingActiveTaskViewModelFactory;
    private TrackingActiveTaskViewModel<SimpleTrackingItemConfig, SimpleTrackingItemLog> viewModel;

    @NonNull
    public static SelectionFragment newInstance(@NonNull StepView step) {
        SelectionFragment fragment = new SelectionFragment();
        Bundle args = SelectionFragment.createArguments(step);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

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
        this.viewModel = ViewModelProviders.of(this, this.trackingActiveTaskViewModelFactory.create(this.stepView))
                .get(SimpleTrackingActiveTaskViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.mpower2_selection_step, container, false);
        this.unbinder = ButterKnife.bind(this, result);
        OnApplyWindowInsetsListener topInsetListener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
        ViewCompat.setOnApplyWindowInsetsListener(this.cancelButton, topInsetListener);
        this.title.setText(this.stepView.getSelectionInfo().getTitle());
        this.detail.setText(this.stepView.getSelectionInfo().getDetail());
        LinearLayoutManager manager = new LinearLayoutManager(this.recyclerView.getContext());
        this.recyclerView.setLayoutManager(manager);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this.recyclerView.getContext(), manager.getOrientation());
        Drawable drawable = this.getContext().getResources().getDrawable(R.drawable.form_step_divider);
        itemDecoration.setDrawable(drawable);
        this.recyclerView.addItemDecoration(itemDecoration);
        TrackingItemAdapter adapter = new TrackingItemAdapter(this.viewModel.getAvailableElements().getValue(), this.viewModel);
        this.recyclerView.setAdapter(adapter);
        return result;
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
}
