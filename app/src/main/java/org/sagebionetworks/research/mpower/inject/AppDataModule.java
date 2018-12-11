package org.sagebionetworks.research.mpower.inject;

import android.content.Context;

import com.google.gson.Gson;

import org.sagebionetworks.bridge.android.di.BridgeApplicationScope;
import org.sagebionetworks.bridge.android.manager.AuthenticationManager;
import org.sagebionetworks.research.domain.repository.TaskRepository;
import org.sagebionetworks.research.mpower.data.AppResourceTaskRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class AppDataModule {
    @Provides
    @BridgeApplicationScope
    TaskRepository provideTaskRepository(Context context, Gson gson, AuthenticationManager authManager) {
        return new AppResourceTaskRepository(context, gson, authManager);
    }
}
