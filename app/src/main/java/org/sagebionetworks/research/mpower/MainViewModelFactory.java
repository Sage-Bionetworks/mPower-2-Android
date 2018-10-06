package org.sagebionetworks.research.mpower;

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import org.sagebionetworks.bridge.android.manager.AuthenticationManager;

import javax.inject.Inject;

public class MainViewModelFactory {
    private final AuthenticationManager authenticationManager;

    @Inject
    public MainViewModelFactory(@NonNull AuthenticationManager authenticationManager) {
        this.authenticationManager = checkNotNull(authenticationManager);
    }

    public ViewModelProvider.Factory create() {
        return new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull final Class<T> modelClass) {
                if (modelClass.isAssignableFrom(MainViewModel.class)) {
                    // noinspection unchecked
                    return (T) new MainViewModel(authenticationManager);
                }
                throw new IllegalArgumentException("Unknown ViewModel class");
            }
        };
    }
}
