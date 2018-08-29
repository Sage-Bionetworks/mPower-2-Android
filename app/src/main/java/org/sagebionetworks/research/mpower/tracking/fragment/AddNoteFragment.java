package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.NoteLog;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.TrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.TrackingTaskViewModel;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;

/**
 * This fragment displays the add note screen to the user and allows the user to edit/add a new note. It is an
 * invariant in this fragment that there is already a log in the view model for the tracking item provided as an
 * argument and the client of this fragment is responsible for ensuring this is correct.
 * @param <ConfigType> The type of TrackingItemConfig
 * @param <LogType> The type of TrackingItemLog
 * @param <ViewModelType> The type of view model.
 */
public class AddNoteFragment
        <ConfigType extends TrackingItemConfig, LogType extends NoteLog, ViewModelType extends TrackingTaskViewModel<ConfigType, LogType>>
        extends TrackingFragment<ConfigType, LogType, ViewModelType> {
    public static final String ARGUMENT_TRACKING_ITEM = "trackingItem";
    private static final Logger LOGGER = LoggerFactory.getLogger(AddNoteFragment.class);

    @BindView(R.id.note_input)
    EditText noteInput;

    @BindView(R.id.rs2_step_navigation_action_backward)
    ActionButton backButton;

    @BindView(R.id.rs2_step_navigation_action_forward)
    ActionButton forwardButton;

    protected TrackingItem trackingItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                // noinspection unchecked
                this.trackingItem = arguments.getParcelable(ARGUMENT_TRACKING_ITEM);
            }
        } else {
            // noinspection unchecked
            this.trackingItem = savedInstanceState.getParcelable(ARGUMENT_STEP_VIEW);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = super.onCreateView(inflater, container, savedInstanceState);
        OnApplyWindowInsetsListener topListener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
        ViewCompat.setOnApplyWindowInsetsListener(this.backButton, topListener);
        LogType log = this.viewModel.getLog(this.trackingItem);
        String previousNote = log.getNote();
        if (previousNote != null) {
            this.noteInput.setText(previousNote);
        }

        // The forward button writes the note result to the log in the view model and then navigates back to the parent.
        this.forwardButton.setOnClickListener(view -> {
            String note = noteInput.getText().toString();
            NoteLog noteLog = log.copyWithNote(note);
            if (noteLog.getClass() != log.getClass()) {
                LOGGER.warn("Note log copyWithNote() returned a different class, most likely caused by forgetting to "
                        + "override the method in class " + log.getClass());
            } else {
                // noteLog.getClass() == log.getClass() and LogType is assignable from log.getClass() so this is safe.
                // noinspection unchecked
                this.viewModel.addLoggedElement((LogType) noteLog);
            }

            this.goToParentFragment();
        });

        // The back button goes back to the parent fragment without writing the note.
        this.backButton.setOnClickListener(view -> this.goToParentFragment());
        return result;
    }

    @Override
    public void onStart() {
        super.onStart();
        ViewCompat.requestApplyInsets(this.getView());
    }

    @Override
    public int getLayoutId() {
        return R.layout.mpower2_add_note;
    }

    /**
     * Returns to the parent fragment that added this fragment.
     */
    private void goToParentFragment() {
        // Pop the back stack once to go back to the parent fragment.
        this.getFragmentManager().popBackStackImmediate();
    }
}
