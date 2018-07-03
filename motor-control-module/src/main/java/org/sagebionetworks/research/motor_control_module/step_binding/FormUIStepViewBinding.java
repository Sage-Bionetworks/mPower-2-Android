package org.sagebionetworks.research.motor_control_module.step_binding;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.sagebionetworks.research.mobile_ui.show_step.view.view_binding.UIStepViewBinding;
import org.sagebionetworks.research.presentation.model.interfaces.FormUIStepView;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FormUIStepViewBinding<S extends FormUIStepView> extends UIStepViewBinding<S> {
    private final Unbinder unbinder;
    protected final FormUIStepViewHolder formUIViewHolder;

    public FormUIStepViewBinding(View view) {
        super(view);
        this.formUIViewHolder = new FormUIStepViewHolder();
        this.unbinder = ButterKnife.bind(this.formUIViewHolder, view);
        RecyclerView recyclerView = this.getRecyclerView();
        if (recyclerView != null) {
            // Optimization since the recycler view size will never change.
            recyclerView.setHasFixedSize(true);
        }
    }

    @Override
    public void unbind() {
        super.unbind();
        this.unbinder.unbind();
    }

    @Override
    public void update(S stepView) {
        super.update(stepView);
        RecyclerView recyclerView = this.getRecyclerView();
        if (recyclerView != null) {
            recyclerView.
        }
    }

    public RecyclerView getRecyclerView() {
        return this.formUIViewHolder.recyclerView;
    }

    protected static final class FormUIStepViewHolder {
        RecyclerView recyclerView;
    }
}
