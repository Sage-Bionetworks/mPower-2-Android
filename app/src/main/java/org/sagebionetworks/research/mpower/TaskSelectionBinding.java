package org.sagebionetworks.research.mpower;

import android.widget.TextView;
import butterknife.BindViews;

import java.util.List;

public class TaskSelectionBinding {
    @BindViews({R.id.task1, R.id.task2, R.id.task3})
    public List<TextView> taskTextViews;
}
