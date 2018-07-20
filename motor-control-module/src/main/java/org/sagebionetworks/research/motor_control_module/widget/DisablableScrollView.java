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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * A DisableableScrollView is a ScrollView with the added option to disable the ability to scroll. When disabled the
 * view stays at whatever position it was and user actions have no effect on it. Note: Disabling a scroll view only
 * disables user input from scrolling it, and it can still be moved in code.
 */
public class DisablableScrollView extends ScrollView {
    private boolean enabled;

    public DisablableScrollView(final Context context) {
        super(context);
        this.commonInit();
    }

    public DisablableScrollView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.commonInit();
    }

    public DisablableScrollView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.commonInit();
    }

    /**
     * Returns `true` if scrolling is allowed, `false` otherwise.
     *
     * @return `true` if scrolling is allowdd, `false` otherwise.
     */
    public boolean isScollingEnabled() {
        return this.enabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Override onTouchEvent to only process the event if scrolling is enabled.
        if (this.enabled) {
            return super.onTouchEvent(event);
        } else {
            return this.enabled;
        }
    }

    /**
     * Sets whether or not scrolling is allowed on this.
     *
     * @param enabled
     *         `true` if scrolling should be allowed `false` otherwise.
     */
    public void setScrollingEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected void commonInit() {
        this.enabled = true;
    }
}
