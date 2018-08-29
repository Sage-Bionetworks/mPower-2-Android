package org.sagebionetworks.research.mpower.tracking.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;

import org.sagebionetworks.research.mpower.tracking.model.TrackingItem;
import org.sagebionetworks.research.mpower.tracking.view_model.SymptomTrackingTaskViewModel;
import org.sagebionetworks.research.mpower.tracking.view_model.configs.SimpleTrackingItemConfig;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

/**
 * A subtype of the AddNoteFragment that uses the Config, Log and ViewModel types required for the symptoms logging.
 */
public class SymptomAddNoteFragment extends AddNoteFragment<SimpleTrackingItemConfig, SymptomLog, SymptomTrackingTaskViewModel> {
    /**
     * Creates a new SymptomAddNoteFragment from the given StepView and TrackingItem.
     * @param stepView The TrackingStepView that the fragment should be created with.
     * @param trackingItem The TrackingItem that the note for this fragment is for.
     * @return a new SymptomAddNoteFragment from the given StepView and TrackingItem.
     */
    @NonNull
    public static SymptomAddNoteFragment newInstance(@NonNull StepView stepView,
            @NonNull TrackingItem trackingItem) {
        SymptomAddNoteFragment addNoteFragment = new SymptomAddNoteFragment();
        Bundle args = TrackingFragment.createArguments(stepView);
        args.putParcelable(ARGUMENT_TRACKING_ITEM, trackingItem);
        addNoteFragment.setArguments(args);
        return addNoteFragment;
    }
}
