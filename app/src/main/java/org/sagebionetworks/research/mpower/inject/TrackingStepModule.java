package org.sagebionetworks.research.mpower.inject;

import static org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModelFactory.SYMPTOM_LOGGING_TYPE_KEY;

import com.google.gson.TypeAdapterFactory;

import org.sagebionetworks.research.domain.inject.GsonModule;
import org.sagebionetworks.research.domain.inject.StepModule.StepClassKey;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule.ShowStepFragmentFactory;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule.StepViewKey;
import org.sagebionetworks.research.mpower.tracking.fragment.SelectionFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.SymptomSelectionFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TriggersSelectionFragment;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStep;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.presentation.inject.StepViewModule.InternalStepViewFactory;
import org.sagebionetworks.research.presentation.inject.StepViewModule.StepTypeKey;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;

@Module(includes = GsonModule.class)
public abstract class TrackingStepModule {
    @Provides
    @IntoSet
    static TypeAdapterFactory provideAppAutoValueTypeAdapterFactory() {
        return AppAutoValueTypeAdapterFactory.create();
    }

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
        return (stepView -> {
            if (!(stepView instanceof TrackingStepView)) {
                throw new IllegalArgumentException("Provided StepView " + stepView + " is not a TrackingStepView");
            }

            TrackingStepView trackingStepView = (TrackingStepView)stepView;
            String selectionType = trackingStepView.getSelectionInfo().getType();
            String loggingType = trackingStepView.getLoggingInfo().getType();
            if (selectionType == null && loggingType == null) {
                return TriggersSelectionFragment.newInstance(trackingStepView);
            } else if (selectionType == null && loggingType.equals(SYMPTOM_LOGGING_TYPE_KEY)) {
                return SymptomSelectionFragment.newInstance(trackingStepView);
            }

            throw new IllegalArgumentException(
                    "Cannot instantiate fragment from TrackingStepView with selectionType: " + selectionType + " and loggingType: "
                            + loggingType);
        });
    }
}
