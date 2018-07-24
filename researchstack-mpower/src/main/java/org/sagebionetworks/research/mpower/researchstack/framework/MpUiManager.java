package org.sagebionetworks.research.mpower.researchstack.framework;

import android.content.Context;

import org.researchstack.backbone.ActionItem;
import org.researchstack.backbone.UiManager;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;

import java.util.ArrayList;
import java.util.List;

public class MpUiManager extends UiManager {
    @Override
    public List<ActionItem> getMainActionBarItems() {
        List<ActionItem> navItems = new ArrayList<>();

        return navItems;
    }

    @Override
    public List<ActionItem> getMainTabBarItems() {
        List<ActionItem> navItems = new ArrayList<>();

        return navItems;
    }

    @Override
    public Step getInclusionCriteriaStep(Context context) {
        return null;
    }

    @Override
    public boolean isInclusionCriteriaValid(StepResult result) {
        return false;
    }

    @Override
    public boolean isConsentSkippable() {
        return true;
    }
}
