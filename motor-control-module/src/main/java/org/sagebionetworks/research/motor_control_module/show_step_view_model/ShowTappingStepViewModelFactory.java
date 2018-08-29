package org.sagebionetworks.research.motor_control_module.show_step_view_model;

import org.sagebionetworks.research.motor_control_module.step_view.TappingStepView;
import org.sagebionetworks.research.presentation.perform_task.PerformTaskViewModel;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.AbstractShowStepViewModelFactory;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowStepViewModel;

public class ShowTappingStepViewModelFactory implements
        AbstractShowStepViewModelFactory<ShowTappingStepViewModel, TappingStepView> {
    @Override
    public ShowTappingStepViewModel create(final PerformTaskViewModel performTaskViewModel,
            final TappingStepView stepView) {
        return new ShowTappingStepViewModel(performTaskViewModel, stepView);
    }

    @Override
    public Class<? extends ShowStepViewModel> getViewModelClass() {
        return ShowTappingStepViewModel.class;
    }
}
