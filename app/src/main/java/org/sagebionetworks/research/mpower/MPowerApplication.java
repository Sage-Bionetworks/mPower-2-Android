package org.sagebionetworks.research.mpower;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.multidex.MultiDex;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import org.researchstack.backbone.ResearchStack;
import org.sagebionetworks.bridge.android.BridgeApplication;
import org.sagebionetworks.research.mpower.inject.DaggerMPowerApplicationComponent;
import org.sagebionetworks.research.mpower.researchstack.framework.MpDataProvider;
import org.sagebionetworks.research.mpower.researchstack.framework.MpResearchStack;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.support.HasSupportFragmentInjector;

public class MPowerApplication extends BridgeApplication implements HasSupportFragmentInjector,
        HasActivityInjector {
    @Inject
    DispatchingAndroidInjector<Activity> dispatchingActivityInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingSupportFragmentInjector;

    MpResearchStack researchStack;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerMPowerApplicationComponent
                .builder()
                .application(this)
                .build()
                .inject(this);

        researchStack = new MpResearchStack(this);
        ResearchStack.init(this, researchStack);
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingActivityInjector;
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
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingSupportFragmentInjector;
    }

    @Override
    protected void attachBaseContext(Context base) {
        // This is needed for android versions < 5.0 or you can extend MultiDexApplication
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
