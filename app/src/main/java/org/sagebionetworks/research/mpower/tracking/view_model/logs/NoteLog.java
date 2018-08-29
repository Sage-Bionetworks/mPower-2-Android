package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface NoteLog extends TrackingItemLog {
    /**
     * Returns the note for this log.
     * @return the note for this log.
     */
    @Nullable
    String getNote();

    /**
     * Returns a new NoteLog with the given note replacing the old note.
     * @param note the note to replace the old note for.
     * @return a new NoteLog with the given note replacing the old one.
     */
    @NonNull
    NoteLog copyWithNote(@Nullable String note);
}
