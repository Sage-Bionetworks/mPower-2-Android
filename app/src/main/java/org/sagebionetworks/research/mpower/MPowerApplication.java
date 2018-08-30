package org.sagebionetworks.research.mpower;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.VisibleForTesting;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.researchstack.backbone.ResearchStack;
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider;
import org.sagebionetworks.research.mpower.inject.DaggerMPowerApplicationComponent;
import org.sagebionetworks.research.mpower.inject.MPowerApplicationComponent;
import org.sagebionetworks.research.sageresearch.BridgeSageResearchApp;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
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

    @Override
    public void onCreate() {
        super.onCreate();
        initAppComponent().inject(this);
    }

    @VisibleForTesting
    protected MPowerApplicationComponent initAppComponent() {
        return DaggerMPowerApplicationComponent
                .builder()
                .bridgeManagerProvider(BridgeManagerProvider.getInstance())
                .application(this)
                .build();
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

    @Override
    protected void attachBaseContext(Context base) {
        // This is needed for android versions < 5.0 or you can extend MultiDexApplication
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingSupportFragmentInjector;
    }
}
