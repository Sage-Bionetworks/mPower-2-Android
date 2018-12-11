package org.sagebionetworks.research.mpower.researchstack.inject;

import android.content.Context;

import org.researchstack.backbone.ResearchStack;
import org.researchstack.backbone.StorageAccess;
import org.sagebionetworks.bridge.android.di.BridgeApplicationScope;
import org.sagebionetworks.research.mpower.researchstack.framework.MpDataProvider;
import org.sagebionetworks.research.mpower.researchstack.framework.MpResearchStack;
import org.sagebionetworks.research.mpower.researchstack.framework.MpTaskFactory;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class MPowerResearchStackModule {

    public static final String PIN_CODE = "1234";

    @Provides
    @BridgeApplicationScope
    static MpTaskFactory provideMpTaskFactory() {
        return new MpTaskFactory();
    }

    @Provides
    @BridgeApplicationScope
    static MpDataProvider provideMpDataProvider() {
        return MpDataProvider.getInstance();
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
     *
     * @param context
     *         can be app or activity
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
