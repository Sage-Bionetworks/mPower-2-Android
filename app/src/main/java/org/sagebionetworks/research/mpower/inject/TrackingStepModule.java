package org.sagebionetworks.research.mpower.inject;

import org.sagebionetworks.research.domain.inject.GsonModule;
import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule.ShowStepFragmentFactory;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule.StepViewKey;
import org.sagebionetworks.research.mpower.tracking.fragment.SelectionFragment;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStep;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.presentation.inject.StepViewModule.InternalStepViewFactory;
import org.sagebionetworks.research.presentation.inject.StepViewModule.StepTypeKey;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

@Module(includes = GsonModule.class)
public abstract class TrackingStepModule {
    @Provides
    @IntoMap
    @StepClassKey(TrackingStep.class)
    static String provideTrackingStepTypeKey() {
        return TrackingStep.TYPE_KEY;
    }

    @Provides
    @IntoMap
    @StepTypeKey(TrackingStepView.TYPE)
    static InternalStepViewFactory provideTrackingStepViewFactory() {
        return TrackingStepView::fromTrackingStep;
    }

    @Provides
    @IntoMap
    @StepViewKey(TrackingStepView.TYPE)
    static ShowStepFragmentFactory provideTrackingFragmentFactory() {
        return SelectionFragment::newInstance;
    }
}
