package org.sagebionetworks.research.motor_control_module.result;

import android.support.annotation.StringDef;

import org.sagebionetworks.research.domain.result.ResultType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@StringDef({ResultType.TASK, ResultType.FILE, ResultType.ERROR, ResultType.COLLECTION, ResultType.ANSWER,
        ResultType.BASE, AppResultType.TAPPING})
public @interface AppResultType {
    String TAPPING = "fingerTapping";
}
