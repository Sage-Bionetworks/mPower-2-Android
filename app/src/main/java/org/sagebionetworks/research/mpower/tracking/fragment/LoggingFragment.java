package org.sagebionetworks.research.mpower.tracking.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.sagebionetworks.research.mobile_ui.show_step.ShowStepFragment;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;

public abstract class LoggingFragment
        <ConfigType extends TrackingItemConfig, LogType extends TrackingItemLog,
        ViewModelType extends TrackingTaskViewModel<ConfigType, LogType>>
        extends RecyclerViewTrackingFragment<ConfigType, LogType, ViewModelType> {
    @Override
    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        this.title.setText(this.stepView.getLoggingInfo().getTitle());
        this.detail.setText(this.stepView.getLoggingInfo().getDetail());
        if (this.addMore != null) {
            this.addMore.setOnClickListener((view) -> {
                ShowStepFragment nextFragment = this.getNextFragment();
                this.getFragmentManager().beginTransaction().replace(R.id.rs2_step_container, nextFragment)
                        .commit();
            });
        }

        return result;
    }

    @Override
    @Nullable
    public ItemDecoration initializeItemDecoration() {
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        Drawable drawable = this.getResources().getDrawable(R.drawable.mpower2_logging_item_decoration);
        itemDecoration.setDrawable(drawable);
        return itemDecoration;
    }

    @Override
    public int getLayoutId() {
        return R.layout.mpower2_logging_step;
    }

    @NonNull
    public abstract ShowStepFragment getNextFragment();
}
