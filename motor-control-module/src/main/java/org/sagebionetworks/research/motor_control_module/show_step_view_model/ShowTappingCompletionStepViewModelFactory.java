package org.sagebionetworks.research.motor_control_module.show_step_view_model;

import org.sagebionetworks.research.motor_control_module.inject.TappingCompletionStepModule;
import org.sagebionetworks.research.motor_control_module.step_view.TappingCompletionStepView;
import org.sagebionetworks.research.presentation.perform_task.PerformTaskViewModel;
import org.sagebionetworks.research.presentation.show_step.show_step_view_model_factories.AbstractShowStepViewModelFactory;
import org.sagebionetworks.research.presentation.show_step.show_step_view_models.ShowStepViewModel;

public class ShowTappingCompletionStepViewModelFactory implements
        AbstractShowStepViewModelFactory<ShowTappingCompletionStepViewModel, TappingCompletionStepView> {
    @Override
    public ShowTappingCompletionStepViewModel create(
            final PerformTaskViewModel performTaskViewModel, final TappingCompletionStepView stepView) {
        return new ShowTappingCompletionStepViewModel(performTaskViewModel, stepView);
    }

    @Override
    public Class<? extends ShowStepViewModel> getViewModelClass() {
        return ShowTappingCompletionStepViewModel.class;
    }
}
