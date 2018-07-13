package org.sagebionetworks.research.motor_control_module;

import org.sagebionetworks.research.domain.inject.TaskModule;
import org.sagebionetworks.research.domain.task.navigation.StepNavigatorFactory;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = TaskModule.class)
public interface NavigationTestComponent {
    StepNavigatorFactory stepNavigatorFactory();
}
