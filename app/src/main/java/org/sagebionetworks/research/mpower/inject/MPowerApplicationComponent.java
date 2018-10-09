package org.sagebionetworks.research.mpower.inject;

import android.app.Application;

import org.sagebionetworks.bridge.android.di.BridgeManagerProviderModule;
import org.sagebionetworks.bridge.android.di.BridgeStudyParticipantScope;
import org.sagebionetworks.bridge.android.di.BridgeStudyScope;
import org.sagebionetworks.research.domain.inject.TaskModule;
import org.sagebionetworks.research.mpower.MPowerApplication;
import org.sagebionetworks.research.mpower.researchstack.inject.MPowerResearchStackModule;
import org.sagebionetworks.research.mpower.sageresearch.inject.MPowerSageResearchModule;
import org.sagebionetworks.research.sageresearch_app_sdk.inject.SageResearchAppSDKModule;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

@Component(modules = {BridgeManagerProviderModule.class, MPowerApplicationModule.class, TaskModule.class,
        MPowerResearchStackModule.class, MPowerSageResearchModule.class, SageResearchAppSDKModule.class,
        AndroidInjectionModule.class})
@Singleton
@BridgeStudyScope
@BridgeStudyParticipantScope
public interface MPowerApplicationComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        MPowerApplicationComponent build();
    }

    void inject(MPowerApplication application);
}
