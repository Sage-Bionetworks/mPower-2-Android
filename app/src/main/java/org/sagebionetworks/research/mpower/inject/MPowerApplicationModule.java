package org.sagebionetworks.research.mpower.inject;

import android.app.Application;
import com.google.common.collect.ImmutableList;

import android.content.Context;

import org.sagebionetworks.research.mobile_ui.inject.PerformTaskModule;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.sagebionetworks.research.motor_control_module.inject.MotorControlShowStepFragmentsModule;
import org.sagebionetworks.research.motor_control_module.inject.MotorControlStepModule;
import org.sagebionetworks.research.mpower.MainActivity;
import org.sagebionetworks.research.mpower.authentication.ExternalIdSignInActivity;
import org.sagebionetworks.research.mpower.history.HistoryFragment;
import org.sagebionetworks.research.mpower.insights.InsightsFragment;
import org.sagebionetworks.research.mpower.profile.ProfileFragment;
import org.sagebionetworks.research.mpower.sageresearch.archive.TappingResultArchiveFactory;
import org.sagebionetworks.research.mpower.tracking.TrackingFragment;
import org.sagebionetworks.research.mpower.tracking.TrackingMenuFragment;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.AbstractResultArchiveFactory.ResultArchiveFactory;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.AnswerResultArchiveFactory;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.BaseResultArchiveFactory;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.FileResultArchiveFactory;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module(includes = {PerformTaskModule.class, MotorControlStepModule.class})
public abstract class MPowerApplicationModule {
    @ContributesAndroidInjector
    abstract ExternalIdSignInActivity contributeExternalIdSignInActivityInjector();

    @ContributesAndroidInjector
    abstract HistoryFragment contributeHistoryFragmentInjector();

    @ContributesAndroidInjector
    abstract InsightsFragment contributeInsightsFragmentInjector();

    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivityInjector();    

    @ContributesAndroidInjector(modules = {ShowStepFragmentModule.class, MotorControlShowStepFragmentsModule.class})
    abstract PerformTaskFragment contributePerformTaskFragmentInjector();

    @ContributesAndroidInjector
    abstract ProfileFragment contributeProfileFragmentInjector();

    @ContributesAndroidInjector
    abstract TrackingFragment contributeTrackingFragmentInjector();

    @ContributesAndroidInjector
    abstract TrackingMenuFragment contributeTrackingMenuFragmentInjector();

    @Provides
    static ImmutableList<ResultArchiveFactory> provideAbstractResultArchiveFactory(
            TappingResultArchiveFactory tappingResultArchiveFactory,
            FileResultArchiveFactory fileResultArchiveFactory, AnswerResultArchiveFactory answerResultArchiveFactory,
            BaseResultArchiveFactory baseResultArchiveFactory) {
        return ImmutableList.of(tappingResultArchiveFactory, fileResultArchiveFactory, answerResultArchiveFactory,
                baseResultArchiveFactory);
    }
}
