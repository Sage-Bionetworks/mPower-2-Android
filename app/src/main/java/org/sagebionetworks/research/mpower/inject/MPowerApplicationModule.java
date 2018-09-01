package org.sagebionetworks.research.mpower.inject;

import com.google.common.collect.ImmutableList;

import org.sagebionetworks.research.mobile_ui.inject.PerformTaskModule;
import org.sagebionetworks.research.mobile_ui.inject.ShowStepFragmentModule;
import org.sagebionetworks.research.mobile_ui.perform_task.PerformTaskFragment;
import org.sagebionetworks.research.motor_control_module.inject.MotorControlShowStepFragmentsModule;
import org.sagebionetworks.research.motor_control_module.inject.MotorControlStepModule;
import org.sagebionetworks.research.mpower.MainActivity;
import org.sagebionetworks.research.mpower.authentication.ExternalIdSignInActivity;
import org.sagebionetworks.research.mpower.history.HistoryFragment;
import org.sagebionetworks.research.mpower.insights.InsightsFragment;
import org.sagebionetworks.research.mpower.tracking.TrackingTabFragment;
import org.sagebionetworks.research.mpower.profile.ProfileFragment;
import org.sagebionetworks.research.mpower.sageresearch.archive.TappingResultArchiveFactory;
import org.sagebionetworks.research.mpower.studyburst.StudyBurstActivity;
import org.sagebionetworks.research.mpower.tracking.TrackingMenuFragment;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.AbstractResultArchiveFactory.ResultArchiveFactory;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.AnswerResultArchiveFactory;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.BaseResultArchiveFactory;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.FileResultArchiveFactory;
import org.sagebionetworks.research.mpower.tracking.fragment.DurationFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.SymptomLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.SymptomSelectionFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TimePickerFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TriggersLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TriggersSelectionFragment;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module(includes = {PerformTaskModule.class, MotorControlStepModule.class, TrackingStepModule.class, AppDataModule.class})
public abstract class MPowerApplicationModule {
    @ContributesAndroidInjector
    abstract TriggersSelectionFragment contributeTriggersSelectionFragmentInjector();

    @ContributesAndroidInjector
    abstract TriggersLoggingFragment contributeTriggersLoggingFragmentInjector();

    @ContributesAndroidInjector
    abstract SymptomSelectionFragment contributeSymptomSelectionFragmentInjector();

    @ContributesAndroidInjector
    abstract SymptomLoggingFragment contributeSymptomLogginFragmentInjector();

    @ContributesAndroidInjector
    abstract ExternalIdSignInActivity contributeExternalIdSignInActivityInjector();

    @ContributesAndroidInjector
    abstract HistoryFragment contributeHistoryFragmentInjector();

    @ContributesAndroidInjector
    abstract InsightsFragment contributeInsightsFragmentInjector();

    // these modules contain an aggregate of ShowStepFragment subcomponents, so they are scoped under the PerformTaskFragment
    @ContributesAndroidInjector(modules = {ShowStepFragmentModule.class, MotorControlShowStepFragmentsModule.class})
    abstract PerformTaskFragment contributePerformTaskFragmentInjector();

    @ContributesAndroidInjector
    abstract TrackingTabFragment contributeTrackingTabFragmentInjector();

    @ContributesAndroidInjector
    abstract DurationFragment contributeDurationFragmentInjector();

    @ContributesAndroidInjector
    abstract TimePickerFragment contributeTimePickerFragmentInjector();

    @ContributesAndroidInjector
    abstract StudyBurstActivity contributeStudyBurstActivityInjector();

    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivityInjector();

    @ContributesAndroidInjector
    abstract ProfileFragment contributeProfileFragmentInjector();

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
