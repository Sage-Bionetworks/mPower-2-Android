/*
 * Copyright 2015 Apple Inc.
 * Ported to Android from ResearchKit/ResearchKit 1.5
 */

package org.sagebionetworks.research.motor_control_module.show_step_fragment.tapping;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@StringDef({TappingButtonIdentifier.LEFT, TappingButtonIdentifier.RIGHT, TappingButtonIdentifier.NONE})
public @interface TappingButtonIdentifier {
    String LEFT = "left";
    String RIGHT = "right";
    String NONE = "none";
}