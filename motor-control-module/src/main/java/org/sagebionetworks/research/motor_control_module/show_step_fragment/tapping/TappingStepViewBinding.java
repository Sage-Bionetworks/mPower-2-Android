package org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping;

import android.view.View;

import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.ActiveUIStepViewBinding;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.motor_control_module.R2;
import org.sagebionetworks.research.motor_control_module.step_view.TappingStepView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TappingStepViewBinding extends ActiveUIStepViewBinding<TappingStepView> {
    protected static class TappingStepViewHolder {
        @BindView(R2.id.leftTappingButton)
        public ActionButton leftTappingButton;

        @BindView(R2.id.rightTappingButton)
        public ActionButton rightTappingButton;

        @BindView(R2.id.tappingButtonView)
        public View tappingButtonView;
    }

    protected TappingStepViewHolder tappingStepViewHolder;
    protected Unbinder tappingStepViewUnbinder;

    public TappingStepViewBinding(View view) {
        super(view);
        this.tappingStepViewHolder = new TappingStepViewHolder();
        this.tappingStepViewUnbinder = ButterKnife.bind(this.tappingStepViewHolder, view);
    }

    public ActionButton getLeftTapButton() {
        return this.tappingStepViewHolder.leftTappingButton;
    }

    public ActionButton getRightTapButton() {
        return this.tappingStepViewHolder.rightTappingButton;
    }

    public View getTappingButtonView() {
        return this.tappingStepViewHolder.tappingButtonView;
    }

    @Override
    public void unbind() {
        super.unbind();
        this.tappingStepViewUnbinder.unbind();
    }
}
