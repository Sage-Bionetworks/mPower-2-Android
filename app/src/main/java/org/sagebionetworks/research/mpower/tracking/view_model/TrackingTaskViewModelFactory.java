package org.sagebionetworks.research.mpower.tracking.view_model;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProvider.Factory;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.research.MpIdentifier;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;

import javax.inject.Inject;

/**
 * Factory which produces TrackingTaskViewModels for TrackingStepViews. The decision for which type of
 * TrackingTaskViewModel to create is made based on what type of selection and logging info types the stepView has.
 */
public class TrackingTaskViewModelFactory {

    @Inject
    public TrackingTaskViewModelFactory() {

    }

    public ViewModelProvider.Factory create(@NonNull final TrackingStepView trackingStepView) {
        return new Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
                // TODO rkolmos 09/07/2018 get the previous logging collection and pass it to the view model constructor.
                String whichTask = trackingStepView.whichTask();
                if (whichTask != null) {
                    switch (whichTask) {
                        case MpIdentifier.TRIGGERS:
                            return (T) new SimpleTrackingTaskViewModel(trackingStepView, null);
                        case MpIdentifier.MEDICATION:
                            return (T) new MedicationTrackingTaskViewModel(trackingStepView, null);
                        case MpIdentifier.SYMPTOMS:
                            return (T) new SymptomTrackingTaskViewModel(trackingStepView, null);
                    }
                }

                throw new IllegalArgumentException(
                        "Cannot instantiate view model because TrackingStepView doesn't correspond to a format of known tasks.");
            }
        };
    }
}
