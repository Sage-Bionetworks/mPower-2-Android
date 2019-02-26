package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.sagebionetworks.research.mobile_ui.show_step.view.ShowStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowUIStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.motor_control_module.show_step_view_model.ShowTappingCompletionStepViewModel;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper.Hand;
import org.sagebionetworks.research.motor_control_module.step_view.TappingCompletionStepView;
import org.sagebionetworks.research.motor_control_module.widget.TapCountResultView;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

public class ShowTappingCompletionStepFragment extends
        ShowUIStepFragmentBase<TappingCompletionStepView, ShowTappingCompletionStepViewModel,
                TappingCompletionStepViewBinding> {
    public static ShowTappingCompletionStepFragment newInstance(@NonNull StepView stepView) {
        ShowTappingCompletionStepFragment fragment = new ShowTappingCompletionStepFragment();
        Bundle args = ShowStepFragmentBase.createArguments(stepView);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        ImageView imageView = this.stepViewBinding.getImageView();
        if (imageView != null) {
            OnApplyWindowInsetsListener listener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
            ViewCompat.setOnApplyWindowInsetsListener(imageView, listener);
        }

        return result;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.mpower2_tapping_completion_step;
    }

    @NonNull
    @Override
    protected TappingCompletionStepViewBinding instantiateAndBindBinding(View view) {
        return new TappingCompletionStepViewBinding(view);
    }

    @Override
    public void update(TappingCompletionStepView stepView) {
        super.update(stepView);
        TapCountResultView leftResultView = this.stepViewBinding.getLeftResult();
        this.updateTapCountResultView(leftResultView, Hand.LEFT);
        TapCountResultView rightResultView = this.stepViewBinding.getRightResult();
        this.updateTapCountResultView(rightResultView, Hand.RIGHT);
        // Update the text view discussing the amount of time
        TextView timeLabel = this.stepViewBinding.getTimeLabel();
        timeLabel.setText(R.string.tapping_completion_time_label_text);
    }

    /**
     * Updates the given result view with the information from the given TappingResult.
     * @param view the TapCountResultView to update.
     */
    private void updateTapCountResultView(@NonNull TapCountResultView view, @NonNull Hand hand) {
        Integer tapCount = this.showStepViewModel.getTappingCount(hand);
        if (tapCount != null) {
            // If we have a result we update the view.
            view.setVisibility(View.VISIBLE);
            view.setCount(tapCount);
            String description = this.getResources().getString(this.showStepViewModel.getDescriptionText(hand));
            view.setDescription(description);
        } else {
            // If we don't have a result we hide the view.
            view.setVisibility(View.GONE);
        }
    }
}
