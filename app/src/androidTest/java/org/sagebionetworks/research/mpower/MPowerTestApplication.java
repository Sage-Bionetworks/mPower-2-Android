package org.sagebionetworks.research.mpower;

import static org.mockito.Mockito.mock;

import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider;

public class MPowerTestApplication extends MPowerApplication {
    private final BridgeManagerProvider bridgeManagerProvider = mock(BridgeManagerProvider.class);

    public BridgeManagerProvider getMockBridgeManagerProvider() {
        return bridgeManagerProvider;
    }
// TODO: use this one mocking is fixed on Android
//    @Override
//    @VisibleForTesting
//    protected MPowerApplicationComponent initAppComponent() {
//        return DaggerMPowerApplicationComponent
//                .builder()
//                .bridgeManagerProvider(bridgeManagerProvider)
//                .application(this)
//                .build();
//    }
}
