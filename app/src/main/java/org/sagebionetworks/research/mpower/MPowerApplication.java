package org.sagebionetworks.research.mpower;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.researchstack.backbone.ResearchStack;
import org.sagebionetworks.bridge.android.di.BridgeStudyComponent;
import org.sagebionetworks.research.mpower.inject.DaggerMPowerApplicationComponent;
import org.sagebionetworks.research.mpower.inject.DaggerMPowerUserScopeComponent;
import org.sagebionetworks.research.mpower.inject.MPowerUserScopeComponent;
import org.sagebionetworks.research.sageresearch.BridgeSageResearchApp;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import dagger.android.support.DaggerApplication;
import dagger.android.support.HasSupportFragmentInjector;

public class MPowerApplication extends BridgeSageResearchApp implements HasSupportFragmentInjector,
        HasActivityInjector, HasServiceInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingSupportFragmentInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    // this causes ResearchStack provider method, which also initializes RS, to be called during onCreate
    @Inject
    ResearchStack researchStack;

    @VisibleForTesting
    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        return DaggerMPowerApplicationComponent
                .builder()
                .mPowerUserScopeComponent((MPowerUserScopeComponent) getOrInitBridgeManagerProvider())
                .application(this)
                .build();
    }

    @Override
    protected MPowerUserScopeComponent initBridgeManagerScopedComponent(BridgeStudyComponent bridgeStudyComponent) {
        MPowerUserScopeComponent bridgeManagerProvider = DaggerMPowerUserScopeComponent.builder()
                .applicationContext(this.getApplicationContext())
                .bridgeStudyComponent(bridgeStudyComponent)
                .build();
        return bridgeManagerProvider;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // In some cases modifying newConfig leads to unexpected behavior,
        // so it's better to edit new instance.
        Configuration configuration = new Configuration(newConfig);
        if (configuration.fontScale > 1.3) {
            configuration.fontScale = 1.3f;
            Context context = getApplicationContext();
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                wm.getDefaultDisplay().getMetrics(metrics);
            }

            metrics.scaledDensity = configuration.fontScale * metrics.density;
            context.getResources().updateConfiguration(configuration, metrics);
        }
    }
}
