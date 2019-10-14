package org.sagebionetworks.research.mpower.inject;

import android.app.Application;

import org.sagebionetworks.bridge.android.di.BridgeApplicationScope;
import org.sagebionetworks.research.domain.inject.TaskModule;
import org.sagebionetworks.research.mobile_ui.inject.PerformTaskModule;
import org.sagebionetworks.research.mpower.MPowerApplication;
import org.sagebionetworks.research.mpower.researchstack.inject.MPowerResearchStackModule;
import org.sagebionetworks.research.sageresearch.dao.room.AppConfigRepository;
import org.sagebionetworks.research.sageresearch.dao.room.ReportRepository;
import org.sagebionetworks.research.sageresearch.dao.room.SurveyRepository;
import org.sagebionetworks.research.sageresearch.repos.BridgeRepositoryManager;
import org.sagebionetworks.research.sageresearch_app_sdk.inject.SageResearchAppSDKModule;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;

@BridgeApplicationScope
@Component(modules = {PerformTaskModule.class, SageResearchAppSDKModule.class, TaskModule.class,
        MPowerResearchStackModule.class, AppDataModule.class, AndroidSupportInjectionModule.class,
        MPowerSageResearchModule.class, MPowerAppModule.class, MPowerUserModule.class,
        TrackingStepModule.class, TrackingFragmentsModule.class},
        dependencies = {MPowerUserScopeComponent.class})
public interface MPowerApplicationComponent extends AndroidInjector<MPowerApplication> {


    void inject(ReportRepository instance);

    void inject(AppConfigRepository instance);

    void inject(SurveyRepository instance);

    void inject(BridgeRepositoryManager instance);

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        Builder mPowerUserScopeComponent(MPowerUserScopeComponent mPowerUserScopeComponent);

        MPowerApplicationComponent build();
    }
}
