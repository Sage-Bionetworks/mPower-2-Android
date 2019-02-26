package org.sagebionetworks.research.mpower.tracking.view_model;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProvider.Factory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.domain.result.interfaces.TaskResult;
import org.sagebionetworks.research.mpower.research.MpIdentifier;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection;

import java.util.List;

import javax.inject.Inject;

/**
 * Factory which produces TrackingTaskViewModels for TrackingStepViews. The decision for which type of
 * TrackingTaskViewModel to create is made based on what type of selection and logging info types the stepView has.
 */
public class TrackingTaskViewModelFactory {

    @Inject
    public TrackingTaskViewModelFactory() {
    }

    public ViewModelProvider.Factory create(
            @NonNull final TrackingStepView trackingStepView, @Nullable TaskResult taskResult) {

        return new Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {

                // Attempt to read the previous task result from the task repository
                // and check to see if there is an async result that contains the last logging collection
                LoggingCollection previousLoggingCollection = null;
                if (taskResult != null) {
                    List<Result> asyncResults = taskResult.getAsyncResults();
                    if (asyncResults != null) {
                        for (Result result : asyncResults) {
                            if (result instanceof LoggingCollection) {
                                previousLoggingCollection = (LoggingCollection)result;
                            }
                        }
                    }
                }

                String whichTask = trackingStepView.whichTask();
                if (whichTask != null) {
                    switch (whichTask) {
                        case MpIdentifier.TRIGGERS:
                            return (T) new TriggersTrackingTaskViewModel(trackingStepView, previousLoggingCollection);
                        case MpIdentifier.MEDICATION:
                            return (T) new MedicationTrackingTaskViewModel(trackingStepView, previousLoggingCollection);
                        case MpIdentifier.SYMPTOMS:
                            return (T) new SymptomTrackingTaskViewModel(trackingStepView, previousLoggingCollection);
                    }
                }

                throw new IllegalArgumentException(
                        "Cannot instantiate view model because TrackingStepView doesn't correspond to a format of known tasks.");
            }
        };
    }
}
