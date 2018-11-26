package org.sagebionetworks.research.mpower.inject;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.bridge.android.di.BridgeApplicationScope;
import org.sagebionetworks.research.mobile_ui.inject.PerformTaskFragmentScope;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.sagebionetworks.research.mpower.EntryActivity;
import org.sagebionetworks.research.mpower.EntryFragment;
import org.sagebionetworks.research.mpower.MainFragment;
import org.sagebionetworks.research.mpower.authentication.ExternalIdSignInActivity;
import org.sagebionetworks.research.mpower.history.HistoryFragment;
import org.sagebionetworks.research.mpower.insights.InsightsFragment;
import org.sagebionetworks.research.mpower.profile.ProfileFragment;
import org.sagebionetworks.research.mpower.reminders.StudyBurstReminderActivity;
import org.sagebionetworks.research.mpower.sageresearch.archive.TappingResultArchiveFactory;
import org.sagebionetworks.research.mpower.studyburst.StudyBurstActivity;
import org.sagebionetworks.research.mpower.tracking.TrackingMenuFragment;
import org.sagebionetworks.research.mpower.tracking.TrackingTabFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.DurationFragment;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.AbstractResultArchiveFactory.ResultArchiveFactory;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.AnswerResultArchiveFactory;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.BaseResultArchiveFactory;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.FileResultArchiveFactory;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module(includes = {})
public abstract class MPowerUserModule {
    @ContributesAndroidInjector
    abstract ExternalIdSignInActivity contributeExternalIdSignInActivityInjector();

    @ContributesAndroidInjector
    abstract HistoryFragment contributeHistoryFragmentInjector();

    @ContributesAndroidInjector
    abstract InsightsFragment contributeInsightsFragmentInjector();

    // these modules contain an aggregate of ShowStepFragment subcomponents, so they are scoped under the PerformTaskFragment
    @ContributesAndroidInjector(modules = {ShowStepFragmentModule.class, MpMotorControlShowStepFragmentsModule.class})
    @PerformTaskFragmentScope
    abstract PerformTaskFragment contributePerformTaskFragmentInjector();

    @ContributesAndroidInjector
    abstract TrackingTabFragment contributeTrackingTabFragmentInjector();

    @ContributesAndroidInjector
    abstract DurationFragment contributeDurationFragmentInjector();

    @ContributesAndroidInjector
    abstract StudyBurstActivity contributeStudyBurstActivityInjector();

    @ContributesAndroidInjector
    abstract StudyBurstReminderActivity contributeReminderActivityInjector();

    @ContributesAndroidInjector
    abstract ProfileFragment contributeProfileFragmentInjector();

    @ContributesAndroidInjector
    abstract TrackingMenuFragment contributeTrackingMenuFragmentInjector();

    @ContributesAndroidInjector
    abstract EntryActivity contributeEntryActivityInjector();

    @ContributesAndroidInjector
    abstract EntryFragment contributeEntryFragmentInjector();

    @ContributesAndroidInjector
    abstract MainFragment contributeMainFragmentInjector();

    @Provides
    @BridgeApplicationScope
    static ImmutableList<ResultArchiveFactory> provideAbstractResultArchiveFactory(
            TappingResultArchiveFactory tappingResultArchiveFactory,
            FileResultArchiveFactory fileResultArchiveFactory, AnswerResultArchiveFactory answerResultArchiveFactory,
            BaseResultArchiveFactory baseResultArchiveFactory) {
        return ImmutableList.of(tappingResultArchiveFactory, fileResultArchiveFactory, answerResultArchiveFactory,
                baseResultArchiveFactory);
    }
}
