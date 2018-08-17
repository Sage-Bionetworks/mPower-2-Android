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

package org.sagebionetworks.research.motor_control_module.widget;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import org.sagebionetworks.research.motor_control_module.R;
import org.sagebionetworks.research.motor_control_module.R2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TapCountResultView extends ConstraintLayout {
    private TapCountResultViewBinding binding;
    private Unbinder unbinder;

    public class TapCountResultViewBinding {
        @BindView(R2.id.tapping_completion_count)
        TextView count;

        @BindView(R2.id.tapping_completion_description)
        TextView description;
    }

    public TapCountResultView(final Context context) {
        super(context);
        this.commonInit();
    }

    public TapCountResultView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.commonInit();
    }

    public TapCountResultView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.commonInit();
    }

    protected void commonInit() {
        inflate(this.getContext(), R.layout.tap_count_result_view, this);
        this.binding = new TapCountResultViewBinding();
        this.unbinder = ButterKnife.bind(this.binding, this);
    }

    public void setCount(int count) {
        String countString = count + "";
        this.binding.count.setText(countString);
    }

    public void setDescription(String description) {
        this.binding.description.setText(description);
    }
}
