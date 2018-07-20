package org.sagebionetworks.research.mpower;

import android.content.Context;
import android.content.res.Configuration;
import android.support.multidex.MultiDex;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import org.sagebionetworks.bridge.android.BridgeApplication;
import org.sagebionetworks.research.mpower.inject.DaggerMPowerApplicationComponent;

public class MpMainApplication extends BridgeApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerMPowerApplicationComponent
                .builder()
                .application(this)
                .build();
    }

    @Override
    protected void attachBaseContext(Context base) {
        // This is needed for android versions < 5.0 or you can extend MultiDexApplication
        super.attachBaseContext(base);
        MultiDex.install(this);
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
