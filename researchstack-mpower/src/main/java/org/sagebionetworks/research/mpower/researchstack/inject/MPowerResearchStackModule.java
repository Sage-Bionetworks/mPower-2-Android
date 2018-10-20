package org.sagebionetworks.research.mpower.researchstack.inject;

import android.app.Activity;
import android.content.Context;

import org.researchstack.backbone.ResearchStack;
import org.researchstack.backbone.StorageAccess;
import org.sagebionetworks.bridge.android.di.BridgeApplicationScope;
import org.sagebionetworks.research.mpower.researchstack.MpMainActivity;
import org.sagebionetworks.research.mpower.researchstack.framework.MpResearchStack;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {MpMainActivitySubcomponent.class})
public abstract class MPowerResearchStackModule {

    public static final String PIN_CODE = "1234";

    @Binds
    @IntoMap
    @ActivityKey(MpMainActivity.class)
    abstract AndroidInjector.Factory<? extends Activity> bindMpMainActivityInjectorFactory(
            MpMainActivitySubcomponent.Builder builder);

    @Provides
    @BridgeApplicationScope
    static MpTaskFactory provideMpTaskFactory() {
        return new MpTaskFactory();
    }

    @Provides
    @BridgeApplicationScope
    static ResearchStack provideResearchStack(Context context) {
        MpResearchStack researchStack = new MpResearchStack(context);
        ResearchStack.init(context, researchStack);
        mockAuthenticate(context);
        return researchStack;
    }

    /**
     * Call to mock authenticate to remove pin code auth screen for ResearchStack based activities
     * @param context can be app or activity
     */
    public static void mockAuthenticate(Context context) {
        // We don't use a pin code for MPower, so just plug one in for the app to always use
        if (StorageAccess.getInstance().hasPinCode(context)) {
            StorageAccess.getInstance().authenticate(context, PIN_CODE);
        } else {
            StorageAccess.getInstance().createPinCode(context, PIN_CODE);
        }
    }
}
