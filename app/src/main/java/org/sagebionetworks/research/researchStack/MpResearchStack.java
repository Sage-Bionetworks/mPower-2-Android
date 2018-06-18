package org.sagebionetworks.research.researchStack;

import android.content.Context;
import org.researchstack.backbone.*;
import org.researchstack.backbone.notification.NotificationConfig;
import org.researchstack.backbone.notification.SimpleNotificationConfig;
import org.researchstack.backbone.onboarding.OnboardingManager;
import org.researchstack.backbone.storage.database.AppDatabase;
import org.researchstack.backbone.storage.file.EncryptionProvider;
import org.researchstack.backbone.storage.file.FileAccess;
import org.researchstack.backbone.storage.file.PinCodeConfig;
import org.researchstack.backbone.storage.file.SimpleFileAccess;
import org.researchstack.backbone.storage.file.aes.AesProvider;

public class MpResearchStack extends ResearchStack {

    MpEmptyAppDatabase mEmptyDb;
    AesProvider mEncryptionProvider;

    MpUiManager mUiManager;
    MpDataProvider mDataProvider;
    MpResourceManager mResourceManager;

    SimpleFileAccess mFileAccess;
    PinCodeConfig mPinCodeConfig;

    TaskProvider mTaskProvider;

    SimpleNotificationConfig mNotificationConfig;

    MpPermissionRequestManager mPermissionManager;

    public MpResearchStack(Context context) {

        MpPrefs.init(context);

        mFileAccess = new SimpleFileAccess();

        mEncryptionProvider = new AesProvider();

        mResourceManager = new MpResourceManager();

        mNotificationConfig = new SimpleNotificationConfig();

        mPermissionManager = new MpPermissionRequestManager();
    }

    @Override
    protected AppDatabase createAppDatabaseImplementation(Context context) {
        if (mEmptyDb == null) {
            mEmptyDb = new MpEmptyAppDatabase();
        }
        return mEmptyDb;
    }

    @Override
    protected PinCodeConfig getPinCodeConfig(Context context) {
        if (mPinCodeConfig == null) {
            long autoLockTime = AppPrefs.getInstance(context).getAutoLockTime();
            mPinCodeConfig = new PinCodeConfig(autoLockTime);
        }
        return mPinCodeConfig;
    }

    @Override
    protected EncryptionProvider getEncryptionProvider(Context context) {
        return mEncryptionProvider;
    }

    @Override
    public OnboardingManager getOnboardingManager() {
        // TODO: determine if a manager is still appropriate
        return null;
    }

    @Override
    public void createOnboardingManager(Context context) {
        // TODO: determine if a manager is still appropriate;
    }

    @Override
    protected FileAccess createFileAccessImplementation(Context context) {
        return mFileAccess;
    }

    @Override
    protected ResourceManager createResourceManagerImplementation(Context context) {
        return mResourceManager;
    }

    @Override
    protected UiManager createUiManagerImplementation(Context context) {
        if (mUiManager == null) {
            mUiManager = new MpUiManager();
        }
        return mUiManager;
    }

    @Override
    protected DataProvider createDataProviderImplementation(Context context) {
        if (mDataProvider == null) {
            mDataProvider = new MpDataProvider();
        }
        return mDataProvider;
    }

    @Override
    protected TaskProvider createTaskProviderImplementation(Context context) {
        if (mTaskProvider == null) {
            mTaskProvider = new MpTaskProvider(context);
        }
        return mTaskProvider;
    }

    @Override
    protected NotificationConfig createNotificationConfigImplementation(Context context) {
        return mNotificationConfig;
    }

    @Override
    protected PermissionRequestManager createPermissionRequestManagerImplementation(Context context) {
        return mPermissionManager;
    }
}
