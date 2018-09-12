package org.sagebionetworks.research.mpower;

import com.google.gson.Gson;

import org.sagebionetworks.research.domain.inject.GsonModule;
import org.sagebionetworks.research.mpower.inject.TrackingStepModule;

import dagger.Component;

@Component(modules = {GsonModule.class, TrackingStepModule.class})
public interface TrackingTestComponent {
    Gson gson();
}
