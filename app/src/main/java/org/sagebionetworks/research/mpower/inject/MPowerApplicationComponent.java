package org.sagebionetworks.research.mpower.inject;

import android.app.Application;

import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider;
import org.sagebionetworks.research.domain.inject.TaskModule;
import org.sagebionetworks.research.mpower.MPowerApplication;
import org.sagebionetworks.research.mpower.researchstack.inject.MPowerResearchStackModule;
import org.sagebionetworks.research.mpower.sageresearch.inject.MPowerSageResearchModule;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Component(modules = {MPowerApplicationModule.class, TaskModule.class, MPowerResearchStackModule.class,
        MPowerSageResearchModule.class, AndroidInjectionModule.class},
        dependencies = {BridgeManagerProvider.class})
public interface MPowerApplicationComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        Builder bridgeManagerProvider(BridgeManagerProvider instance);

        MPowerApplicationComponent build();
    }

    void inject(MPowerApplication application);
}
