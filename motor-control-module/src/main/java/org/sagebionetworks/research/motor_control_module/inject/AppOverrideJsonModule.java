package org.sagebionetworks.research.motor_control_module.inject;

import com.google.gson.JsonDeserializer;

import org.sagebionetworks.research.domain.inject.DependencyInjectionType;
import org.sagebionetworks.research.domain.inject.GsonModule;
import org.sagebionetworks.research.domain.step.interfaces.ActiveUIStep;
import org.sagebionetworks.research.motor_control_module.step.MPowerActiveUIStep;

import java.util.Map;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;

import static org.sagebionetworks.research.domain.inject.GsonModule.createPassThroughDeserializer;

@Module(includes = GsonModule.class)
public abstract class AppOverrideJsonModule {
    @Provides
    @IntoMap
    @DependencyInjectionType.Override
    @GsonModule.ClassKey(ActiveUIStep.class)
    static JsonDeserializer<?> provideActiveUIStepOverrideDeserializer() {
        return createPassThroughDeserializer(MPowerActiveUIStep.class);
    }

    @Binds
    @IntoMap
    @DependencyInjectionType.DependencyInjectionTypeKey(DependencyInjectionType.OVERRIDE)
    abstract Map<Class<?>, JsonDeserializer<?>>
    provideOverrideJsonDeserializerMap(@DependencyInjectionType.Override Map<Class<?>, JsonDeserializer<?>> overrideJsonDeserializerMap);
}
