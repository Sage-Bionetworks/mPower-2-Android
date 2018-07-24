package org.sagebionetworks.research.mpower.inject;

import android.app.Application;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public interface MPowerApplicationModule {
    @Provides
    static Context getApplicationContext(Application application) {
        return application.getApplicationContext();
    }
}
