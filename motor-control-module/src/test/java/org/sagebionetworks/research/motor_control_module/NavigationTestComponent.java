package org.sagebionetworks.research.motor_control_module;

import org.sagebionetworks.research.domain.inject.TaskModule;
import org.sagebionetworks.research.domain.task.navigation.StepNavigatorFactory;

import dagger.Component;

@Component(modules = TaskModule.class)
public interface NavigationTestComponent {
    StepNavigatorFactory stepNavigatorFactory();
}
