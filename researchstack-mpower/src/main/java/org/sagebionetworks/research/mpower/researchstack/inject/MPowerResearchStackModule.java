package org.sagebionetworks.research.mpower.researchstack.inject;

import android.app.Activity;
import android.content.Context;

import org.researchstack.backbone.ResearchStack;
import org.sagebionetworks.research.mpower.researchstack.MpMainActivity;
import org.sagebionetworks.research.mpower.researchstack.framework.MpResearchStack;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ActivityKey;
import dagger.android.AndroidInjector;
import dagger.multibindings.IntoMap;

@Module(subcomponents = {MpMainActivitySubcomponent.class})
public abstract class MPowerResearchStackModule {

    // We don't use a pin code for MPower, so just plug in a useless one the app remembers
    public static final String PIN_CODE = "1234";

    @Binds
    @IntoMap
    @ActivityKey(MpMainActivity.class)
    abstract AndroidInjector.Factory<? extends Activity> bindMpMainActivityInjectorFactory(
            MpMainActivitySubcomponent.Builder builder);

    @Provides

    static MpTaskFactory provideMpTaskFactory() {
        return new MpTaskFactory();
    }

    @Provides
    static ResearchStack provideResearchStack(Context context) {
        MpResearchStack researchStack = new MpResearchStack(context);
        ResearchStack.init(context, researchStack);
        return researchStack;
    }
}
