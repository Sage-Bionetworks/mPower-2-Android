package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.view.View;
import android.widget.TextView;

import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.UIStepViewBinding;
import org.sagebionetworks.research.motor_control_module.R2;
import org.sagebionetworks.research.motor_control_module.step_view.TappingCompletionStepView;
import org.sagebionetworks.research.motor_control_module.widget.TapCountResultView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TappingCompletionStepViewBinding extends UIStepViewBinding<TappingCompletionStepView> {
    public static class TappingCompletionStepViewHolder {
        @BindView(R2.id.tapping_completion_left_result)
        public TapCountResultView leftResult;

        @BindView(R2.id.tapping_completion_right_result)
        public TapCountResultView rightResult;

        @BindView(R2.id.tapping_completion_time_label)
        public TextView timeLabel;
    }

    private Unbinder tappingCompletionUnbinder;
    private TappingCompletionStepViewHolder tappingCompletionStepViewHolder;

    public TappingCompletionStepViewBinding(View view) {
        super(view);
        this.tappingCompletionStepViewHolder = new TappingCompletionStepViewHolder();
        this.tappingCompletionUnbinder = ButterKnife.bind(this.tappingCompletionStepViewHolder, view);
    }

    public void unbind() {
        super.unbind();
        this.tappingCompletionUnbinder.unbind();
    }

    public TapCountResultView getLeftResult() {
        return this.tappingCompletionStepViewHolder.leftResult;
    }

    public TapCountResultView getRightResult() {
        return this.tappingCompletionStepViewHolder.rightResult;
    }

    public TextView getTimeLabel() {
        return this.tappingCompletionStepViewHolder.timeLabel;
    }
}
