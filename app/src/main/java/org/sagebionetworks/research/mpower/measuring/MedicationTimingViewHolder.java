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

package org.sagebionetworks.research.mpower.measuring;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.HandSelection;
import org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection.ShowHandSelectionStepFragment;

public class MedicationTimingViewHolder extends RecyclerView.ViewHolder {
    private final ActionButton button;
    private final MedicationTimingStepFragment fragment;
    private final RecyclerView parent;
    private String choice;

    public MedicationTimingViewHolder(final MedicationTimingStepFragment fragment, final RecyclerView parent,
            final ActionButton button) {
        super(button);
        this.button = button;
        this.parent = parent;
        this.fragment = fragment;
        this.button.setOnClickListener(view -> {
            // Write the hand order result to the task result.
            fragment.writeMedicationTimingResult(this.choice);
            // Write the selection to SharedPreferences.
            SharedPreferences prefs = fragment.getSharedPreferencesForTask();
            prefs.edit().putString(ShowHandSelectionStepFragment.HAND_SELECTION_KEY, this.choice).apply();
            // Reset all of the child views to appear as if they have not been selected
            int children = parent.getChildCount();
            int inactiveColor = parent.getContext().getResources().getColor(org.sagebionetworks.research.mobile_ui.R.color.transparent);
            for (int i = 0; i < children; i++) {
                View child = parent.getChildAt(i);
                child.setBackgroundColor(inactiveColor);
            }

            // Set the button that was touched to appear touched.
            int activeColor = view.getContext().getResources().getColor(org.sagebionetworks.research.mobile_ui.R.color.appLightGray);
            button.setBackgroundColor(activeColor);

            MedicationTimingAdapter<?> adapter = (MedicationTimingAdapter<?>)parent.getAdapter();
            adapter.setSelectedChoice(this.choice);
        });
    }

    public ActionButton getButton() {
        return this.button;
    }

    public void setChoice(@NonNull @HandSelection String choice) {
        this.choice = choice;
    }
}
