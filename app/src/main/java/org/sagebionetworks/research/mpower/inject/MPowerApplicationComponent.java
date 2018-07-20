package org.sagebionetworks.research.mpower.inject;

import android.app.Application;
import dagger.BindsInstance;
import dagger.Component;
import org.sagebionetworks.bridge.android.di.BridgeServiceModule;
import org.sagebionetworks.bridge.android.di.S3Module;
import org.sagebionetworks.research.mpower.researchstack.inject.MPowerResearchStackModule;

import javax.inject.Singleton;

@Singleton
@Component(modules = {MPowerResearchStackModule.class, S3Module.class, BridgeServiceModule.class})
public interface MPowerApplicationComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        MPowerApplicationComponent build();
    }
}
