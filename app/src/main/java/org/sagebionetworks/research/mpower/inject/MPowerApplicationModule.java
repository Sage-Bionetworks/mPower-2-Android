package org.sagebionetworks.research.mpower.inject;

import android.app.Application;
import android.content.Context;

import org.sagebionetworks.research.mpower.authentication.ExternalIdSignInActivity;
import org.sagebionetworks.research.mpower.history.HistoryFragment;
import org.sagebionetworks.research.mpower.insights.InsightsFragment;
import org.sagebionetworks.research.mpower.tracking.TrackingFragment;
import org.sagebionetworks.research.mpower.MainActivity;
import org.sagebionetworks.research.mpower.profile.ProfileFragment;
import org.sagebionetworks.research.mpower.tracking.TrackingMenuFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TriggersLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TriggersSelectionFragment;

import dagger.Binds;
import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module(includes = {TrackingStepModule.class, AppDataModule.class})
public interface MPowerApplicationModule {
    @Binds
    Context provideApplicationContext(Application application);

    @ContributesAndroidInjector
    TriggersSelectionFragment contributeTriggersSelectionFragmentInjector();

    @ContributesAndroidInjector
    TriggersLoggingFragment contributeTriggersLoggingFragmentInjector();

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
}
