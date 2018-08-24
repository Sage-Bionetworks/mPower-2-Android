package org.sagebionetworks.research.mpower.tracking.view_model;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import android.arch.lifecycle.ViewModelProvider.Factory;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.model.TrackingStep;
import org.sagebionetworks.research.mpower.tracking.model.TrackingStepView;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TrackingActiveTaskViewModelFactory {
    @Inject
    public TrackingActiveTaskViewModelFactory() {

    }

    public ViewModelProvider.Factory create(@NonNull final TrackingStepView trackingStepView) {
        return new Factory() {
            @NonNull
            @Override
            @SuppressWarnings("unchecked")
            public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
                if (modelClass.isAssignableFrom(SimpleTrackingActiveTaskViewModel.class)) {
                    return (T) new SimpleTrackingActiveTaskViewModel(trackingStepView);
                }

                throw new IllegalArgumentException("View Model is class is not registered");
            }
        };
    }
}
