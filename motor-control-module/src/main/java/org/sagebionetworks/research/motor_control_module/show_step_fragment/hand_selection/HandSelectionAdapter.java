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

package org.sagebionetworks.research.motor_control_module.show_step_fragment.hand_selection;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.sagebionetworks.research.domain.mobile_ui.R;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.presentation.DisplayString;
import org.sagebionetworks.research.presentation.model.form.ChoiceView;

import java.util.List;

public class HandSelectionAdapter<T> extends RecyclerView.Adapter<HandSelectionViewHolder> {
    private List<ChoiceView<T>> choices;
    private RecyclerView recyclerView;
    private ShowHandSelectionStepFragment fragment;
    private String selectedChoice;

    public HandSelectionAdapter(final ShowHandSelectionStepFragment fragment, final RecyclerView recyclerView,
                                final List<ChoiceView<T>> choices, @HandSelection String defaultChoice) {
        this.choices = choices;
        this.recyclerView = recyclerView;
        this.fragment = fragment;
        this.selectedChoice = defaultChoice;
    }

    @NonNull
    @Override
    public HandSelectionViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        ActionButton button = (ActionButton) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.rs2_form_view_holder, parent, false);
        return new HandSelectionViewHolder(this.fragment, this.recyclerView, button);
    }

    public void setSelectedChoice(@HandSelection String selectedChoice) {
        this.selectedChoice = selectedChoice;
    }

    @Override
    public void onBindViewHolder(@NonNull final HandSelectionViewHolder holder, final int position) {
        ChoiceView<String> inputField = (ChoiceView<String>)this.choices.get(position);
        String choice = inputField.getAnswerValue();
        holder.setChoice(choice);
        ActionButton button = holder.getButton();
        DisplayString textDisplayString = inputField.getText();
        String text = "";
        if (textDisplayString != null) {
            text = textDisplayString.getDisplayString();
        }

        button.setText(text);
        int color;
        if (choice.equals(this.selectedChoice)) {
            // this button is selected.
            color = button.getContext().getResources().getColor(R.color.appLightGray);
        } else {
            // this button is not selected
            color = button.getContext().getResources().getColor(R.color.transparent);
        }

        button.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return this.choices.size();
    }
}
