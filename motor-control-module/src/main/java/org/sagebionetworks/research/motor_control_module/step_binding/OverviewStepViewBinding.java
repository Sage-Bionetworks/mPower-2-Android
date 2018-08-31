/*
 * BSD 3-Clause License
 *
 * Copyright 2018  Sage Bionetworks. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2.  Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * 3.  Neither the name of the copyright holder(s) nor the names of any contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission. No license is granted to the trademarks of
 * the copyright holders even if such marks are included in this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sagebionetworks.research.motor_control_module.step_binding;

import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import org.sagebionetworks.research.mobile_ui.R2;
import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.UIStepViewBinding;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mobile_ui.widget.NavigationActionBar.ActionButtonClickListener;
import org.sagebionetworks.research.motor_control_module.step_view.OverviewStepView;
import org.sagebionetworks.research.motor_control_module.widget.DisablableScrollView;

import java.util.List;

/**
 * An OverviewStepViewBinding is an extension of UIStepViewBinding that also has icon views. These icon views consist
 * of an image view which displays the icon and a label which displays a description of the icon. There are three of
 * these views left, right, and center which are filled in the order center, left, right, depending on how many icons
 * are present.
 *
 * @param <S>
 *         The type of step view this binding expects it's update method to recieve.
 */
public class OverviewStepViewBinding<S extends OverviewStepView> extends UIStepViewBinding<S> {
    protected static class OverviewStepViewHolder {
        @BindViews({R2.id.centerIconImageView, R2.id.leftIconImageView, R2.id.rightIconImageView})
        public List<ImageView> iconImageViews;

        @BindViews({R2.id.centerIconLabel, R2.id.leftIconLabel, R2.id.rightIconLabel})
        public List<TextView> iconLabels;

        @BindView(R2.id.overallIconDescriptionLabel)
        public TextView overallIconDescriptionLabel;

        @BindView(R2.id.scrollView)
        public DisablableScrollView scrollView;
    }

    private final OverviewStepViewHolder overviewStepViewHolder;

    private final Unbinder overviewStepViewHolderUnbinder;

    public OverviewStepViewBinding(View view) {
        super(view);
        this.overviewStepViewHolder = new OverviewStepViewHolder();
        this.overviewStepViewHolderUnbinder = ButterKnife.bind(this.overviewStepViewHolder, view);
    }

    public List<ImageView> getIconImageViews() {
        return this.overviewStepViewHolder.iconImageViews;
    }

    public List<TextView> getIconLabels() {
        return this.overviewStepViewHolder.iconLabels;
    }

    public TextView getOverallIconDescriptionLabel() {
        return this.overviewStepViewHolder.overallIconDescriptionLabel;
    }

    public DisablableScrollView getScrollView() {
        return this.overviewStepViewHolder.scrollView;
    }

    @Override
    public void unbind() {
        super.unbind();
        this.overviewStepViewHolderUnbinder.unbind();
    }

    @Override
    public void update(S overviewStepView) {
        super.update(overviewStepView);
        // Make the reminder button underlined.
        ActionButton reminderButton = this.getSkipButton();
        if (reminderButton != null) {
            reminderButton.setPaintFlags(reminderButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }

        TextView title = this.getTitle();
        if (title != null) {
            title.setGravity(Gravity.CENTER);
        }

        TextView text = this.getText();
        if (text != null) {
            text.setGravity(Gravity.CENTER);
        }
    }

    @Override
    public void setActionButtonClickListener(ActionButtonClickListener listener) {
        super.setActionButtonClickListener(listener);
        final ActionButton cancelButton = this.getCancelButton();
        cancelButton.setOnClickListener(view -> listener.onClick(cancelButton));
        final ActionButton infoButton = this.getInfoButton();
        infoButton.setOnClickListener(view -> listener.onClick(infoButton));
    }
}
