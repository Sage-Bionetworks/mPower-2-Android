package org.sagebionetworks.research.motor_control_module.show_step_fragment;

import android.graphics.Matrix;
import android.view.Display;
import android.widget.ImageView;
import android.widget.TextView;

import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.domain.task.Task;
import org.sagebionetworks.research.mobile_ui.show_step.view.ShowUIStepFragmentBase;
import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.UIStepViewBinding;
import org.sagebionetworks.research.motor_control_module.step.HandStepHelper;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.model.interfaces.UIStepView;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowUIStepViewModel;

public class HandStepUIHelper {
    // private constructor to prevent instantiation
    private HandStepUIHelper() {}

    public static <S extends UIStepView, SB extends UIStepViewBinding<S>>
    void update(TaskResult taskResult, S stepView, SB stepViewBinding) {
        // First we figure out which hand will go next.
        HandStepHelper.Hand nextHand = HandStepHelper.nextHand(taskResult);
        // Contains placeholder will be true if any of the strings contains the formatting placeholder %@.
        boolean containsPlaceholder = false;
        // Formatting the title.
        TextView titleTextView = stepViewBinding.getTitle();
        String title = stepView.getTitle().getDisplayString();
        containsPlaceholder |= title != null && title.contains(HandStepHelper.JSON_PLACEHOLDER);
        title = getFormattedStringFor(nextHand, title);
        if (titleTextView != null) {
            titleTextView.setText(title);
        }

        // Formatting the text.
        TextView textTextView = stepViewBinding.getText();
        String text = stepView.getText().getDisplayString();
        containsPlaceholder |= text != null && text.contains(HandStepHelper.JSON_PLACEHOLDER);
        text = getFormattedStringFor(nextHand, text);
        if (textTextView != null) {
            textTextView.setText(text);
        }

        // Updating the image view, we flip the image view if the next hand is right, and at least one
        // of the strings contained the placeholder. This is necessary to avoid flipping all image views
        // when the next hand is the right.
        ImageView imageView = stepViewBinding.getImageView();
        if (imageView != null) {
            Matrix matrix = new Matrix();
            // If it is the right hand we reverse the image view, otherwise we revert it back to normal
            if (nextHand == HandStepHelper.Hand.RIGHT && containsPlaceholder) {
                imageView.setScaleX(-1f);
            } else {
                imageView.setScaleX(1f);
            }

            imageView.invalidate();
        }
    }

    public static String getFormattedStringFor(HandStepHelper.Hand hand, String originalString) {
        if (hand != null && originalString != null) {
            return originalString.replaceAll(HandStepHelper.JSON_PLACEHOLDER, hand.toString().toUpperCase());
        } else {
            return originalString;
        }
    }
}
