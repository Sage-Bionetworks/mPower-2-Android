package org.sagebionetworks.research.mpower.inject;

import android.app.Application;
import android.content.Context;

import org.sagebionetworks.research.mpower.sageresearch.PerformTaskActivity;
import org.sagebionetworks.research.mpower.authentication.ExternalIdSignInActivity;
import org.sagebionetworks.research.mpower.history.HistoryFragment;
import org.sagebionetworks.research.mpower.insights.InsightsFragment;
import org.sagebionetworks.research.mpower.studyburst.StudyBurstActivity;
import org.sagebionetworks.research.mpower.tracking.TrackingFragment;
import org.sagebionetworks.research.mpower.MainActivity;
import org.sagebionetworks.research.mpower.profile.ProfileFragment;
import org.sagebionetworks.research.mpower.tracking.TrackingMenuFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public interface MPowerApplicationModule {
    @Binds
    Context provideApplicationContext(Application application);

    @ContributesAndroidInjector
    ExternalIdSignInActivity contributeExternalIdSignInActivityInjector();

    @ContributesAndroidInjector
    HistoryFragment contributeHistoryFragmentInjector();

    @ContributesAndroidInjector
    InsightsFragment contributeInsightsFragmentInjector();

    @ContributesAndroidInjector
    TrackingFragment contributeTrackingFragmentInjector();

    @ContributesAndroidInjector
    TrackingMenuFragment contributeTrackingMenuFragmentInjector();

    @ContributesAndroidInjector
    MainActivity contributeMainActivityInjector();

    @ContributesAndroidInjector
    ProfileFragment contributeProfileFragmentInjector();

    @ContributesAndroidInjector
    StudyBurstActivity contributeStudyBurstActivityInjector();
}
