package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper;
import org.sagebionetworks.research.mobile_ui.show_step.view.SystemWindowHelper.Direction;
import org.sagebionetworks.research.mobile_ui.widget.ActionButton;
import org.sagebionetworks.research.mpower.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * This fragment displays the add note screen to the user and allows the user to edit/add a new note. In order to receive
 * updates when the user submits a note the fragment the creator of this fragment should provide an OnNoteChangeListener
 * via setOnNoteChangeListener.
 */
public class AddNoteFragment extends Fragment {
    public static final String ARGUMENT_TITLE = "title";
    public static final String ARGUMENT_DETAIL = "detail";
    public static final String ARGUMENT_PREVIOUS_NOTE = "previousNote";
    private static final Logger LOGGER = LoggerFactory.getLogger(AddNoteFragment.class);

    /**
     * Interface for receiving a callback from this fragment when the user submits a new note.
     */
    public interface OnNoteChangeListener {
        void noteChanged(String note);
    }

    @BindView(R.id.note_input)
    EditText noteInput;
    @BindView(R.id.rs2_title)
    TextView titleLabel;
    @BindView(R.id.rs2_detail)
    TextView detailLabel;
    @BindView(R.id.rs2_step_navigation_action_backward)
    ActionButton backButton;
    @BindView(R.id.rs2_step_navigation_action_forward)
    ActionButton forwardButton;

    @Nullable
    private OnNoteChangeListener onNoteChangeListener;
    private String previousNote;
    private String title;
    private String detail;
    private Unbinder unbinder;

    public static AddNoteFragment newInstance(@Nullable String title, @Nullable String detail, @Nullable String previousNote) {
        AddNoteFragment addNoteFragment = new AddNoteFragment();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_TITLE, title);
        args.putString(ARGUMENT_DETAIL, detail);
        args.putString(ARGUMENT_PREVIOUS_NOTE, previousNote);
        addNoteFragment.setArguments(args);
        return addNoteFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                this.title = arguments.getString(ARGUMENT_TITLE);
                this.detail = arguments.getString(ARGUMENT_DETAIL);
                this.previousNote = arguments.getString(ARGUMENT_PREVIOUS_NOTE);
            }
        } else {
            this.title = savedInstanceState.getString(ARGUMENT_TITLE);
            this.detail = savedInstanceState.getString(ARGUMENT_DETAIL);
            this.previousNote = savedInstanceState.getString(ARGUMENT_PREVIOUS_NOTE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.mpower2_add_note,  container, false);
        this.unbinder = ButterKnife.bind(this, result);
        OnApplyWindowInsetsListener topListener = SystemWindowHelper.getOnApplyWindowInsetsListener(Direction.TOP);
        ViewCompat.setOnApplyWindowInsetsListener(this.backButton, topListener);
        if (this.previousNote != null) {
            this.noteInput.setText(this.previousNote);
        } else {
            this.noteInput.setText("");
        }

        if (this.title != null) {
            this.titleLabel.setText(this.title);
        } else {
            this.titleLabel.setVisibility(View.GONE);
        }

        if (this.detail != null) {
            this.detailLabel.setText(this.detail);
        } else {
            this.detailLabel.setVisibility(View.GONE);
        }

        // The forward button sends a note to its listener.
        this.forwardButton.setOnClickListener(view -> {
            if (this.onNoteChangeListener != null) {
                String note = noteInput.getText().toString();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Note entered by user: " + note);
                }

                this.onNoteChangeListener.noteChanged(note);
            } else {
                LOGGER.warn("AddNoteFragment could not submit note because listener was null");
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
    public void onDestroyView() {
        super.onDestroyView();
        this.unbinder.unbind();
    }

    /**
     * Sets the OnNoteChangeListener for this fragment.
     * @param onNoteChangeListener the OnNoteChangeListener to call when the user submits a note.
     */
    public void setOnNoteChangeListener(@Nullable OnNoteChangeListener onNoteChangeListener) {
        this.onNoteChangeListener = onNoteChangeListener;
    }

    /**
     * Returns to the parent fragment that added this fragment.
     */
    private void goToParentFragment() {
        // Pop the back stack once to go back to the parent fragment.
        FragmentManager fragmentManager = this.getFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.popBackStackImmediate();
        } else {
            LOGGER.warn("FragmentManager is null cannot navigate back to parent fragment.");
        }
    }
}
