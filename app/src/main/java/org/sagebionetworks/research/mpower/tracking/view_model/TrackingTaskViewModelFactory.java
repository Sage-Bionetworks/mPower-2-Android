package org.sagebionetworks.research.mpower.tracking.view_model;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import android.arch.lifecycle.ViewModelProvider.Factory;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.Tasks;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory which produces TrackingTaskViewModels for TrackingStepViews. The decision for which type of
 * TrackingTaskViewModel to create is made based on what type of selection and logging info types the stepView has.
 */
@Singleton
public class TrackingTaskViewModelFactory {
    public static final String SYMPTOM_LOGGING_TYPE_KEY = "symptomLogging";

    @Inject
    public TrackingTaskViewModelFactory() {

    }

    public ViewModelProvider.Factory create(@NonNull final TrackingStepView trackingStepView) {
        return new Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
                String selectionType = trackingStepView.getSelectionInfo().getType();
                String loggingType = trackingStepView.getLoggingInfo().getType();
                if (selectionType == null && loggingType == null) {
                    return (T) new SimpleTrackingTaskViewModel(trackingStepView);
                } else if (selectionType == null && loggingType.equals(SYMPTOM_LOGGING_TYPE_KEY)) {
                    return (T) new SymptomTrackingTaskViewModel(trackingStepView);
                }

                throw new IllegalArgumentException(
                        "Cannot instantiate view model with selectionType: " + selectionType + " and loggingType: "
                                + loggingType);
            }
        };
    }
}
