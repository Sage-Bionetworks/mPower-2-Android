package org.sagebionetworks.research.mpower.researchstack.inject;

import org.sagebionetworks.research.mpower.researchstack.MpMainActivity;

import dagger.Subcomponent;
import dagger.android.AndroidInjector;

@Subcomponent
public interface MpMainActivitySubcomponent extends AndroidInjector<MpMainActivity>{
    @Subcomponent.Builder
    abstract class Builder extends AndroidInjector.Builder<MpMainActivity>{
    }
}
