package org.sagebionetworks.research.mpower.researchstack.inject;

import android.content.Context;

import org.researchstack.backbone.ResearchStack;
import org.sagebionetworks.research.mpower.researchstack.framework.MpResearchStack;

import dagger.Module;
import dagger.Provides;

@Module
public abstract class MPowerResearchStackModule {


    // We don't use a pin code for MPower, so just plug in a useless one the app remembers
    public static final String PIN_CODE = "1234";

    @Provides
    static ResearchStack provideResearchStack(Context context) {
        MpResearchStack researchStack = new MpResearchStack(context);
        ResearchStack.init(context, researchStack);
        return researchStack;
    }
//    MpResearchStack researchStack;
    //
//    public static void mockAuthenticate(Context context) {
//        if (StorageAccess.getInstance().hasPinCode(context)) {
//            StorageAccess.getInstance().authenticate(context, PIN_CODE);
//        } else {
//            StorageAccess.getInstance().createPinCode(context, PIN_CODE);
//        }
//    }
//
}
