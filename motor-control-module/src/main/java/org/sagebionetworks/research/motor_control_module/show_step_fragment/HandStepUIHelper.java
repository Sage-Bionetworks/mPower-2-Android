package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.graphics.Matrix;
import android.widget.ImageView;
import android.widget.TextView;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowUIStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.UIStepViewBinding;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.model.interfaces.UIStepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowUIStepViewModel;

public abstract class HandStepUIHelper {
   public static <S extends UIStepView, SB extends UIStepViewBinding<S>> void update(S stepView, SB stepViewBinding) {
       String identifier = stepView.getIdentifier();
       TextView titleTextView = stepViewBinding.getTitle();
       DisplayString title = HandStepHelper.getHandString(stepView.getTitle(), identifier);
       if (titleTextView != null && title != null) {
           titleTextView.setText(title.getDisplayString());
       }

       TextView textTextView = stepViewBinding.getText();
       DisplayString text = HandStepHelper.getHandString(stepView.getText(), identifier);
       if (textTextView != null && text != null) {
           textTextView.setText(text.getDisplayString());
       }

       ImageView imageView = stepViewBinding.getImageView();
       if (imageView != null) {
           HandStepHelper.Hand hand = HandStepHelper.whichHand(stepView.getIdentifier());
           Matrix matrix = new Matrix();
           // If it is the right hand we reverse the image view, otherwise we revert it back to normal
           if (hand == HandStepHelper.Hand.RIGHT) {
               imageView.setScaleX(-1f);
           } else {
               imageView.setScaleX(1f);
           }

           imageView.invalidate();
       }
   }
}
