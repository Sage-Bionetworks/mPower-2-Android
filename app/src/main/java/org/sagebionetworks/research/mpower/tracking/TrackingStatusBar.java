package org.sagebionetworks.research.mpower.tracking;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.mpower.studyburst.StudyBurstStatusWheel;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class TrackingStatusBar extends ConstraintLayout {
//    @BindView(R.id.study_burst_progress_bar_day)
//    TextView dayLabel;
//
//    @BindView(R.id.study_burst_progress_bar_day_count)
//    TextView dayCount;
//
//    @BindView(R.id.study_burst_progress_bar_dial)
//    ProgressBar progressBar;
    @BindView(R.id.studyBurstStatusWheel)
    StudyBurstStatusWheel statusWheel;

    @BindView(R.id.study_burst_progress_bar_title)
    TextView title;

    @BindView(R.id.study_burst_progress_bar_text)
    TextView text;

    @BindView(R.id.study_burst_loading_progress_bar)
    ProgressBar progressBar;

    @BindView(R.id.title_text_background)
    View titleTextBackground;

    @BindView(R.id.carrot_image_button)
    View carrotImageButton;

    private Unbinder unbinder;

    public TrackingStatusBar(final Context context) {
        super(context);
        this.commonInit();
    }

    public TrackingStatusBar(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        this.commonInit();
    }

    public TrackingStatusBar(final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.commonInit();
    }

    private void commonInit() {
        inflate(this.getContext(), R.layout.mpower2_study_burst_status_bar, this);
        this.unbinder = ButterKnife.bind(this);
    }

    public void setDayLabel(String text) {
        this.statusWheel.setLabel(text);
    }

    public void setDayCount(int count) {
        this.statusWheel.setDayCount(count);
    }

    public void setProgress(int progress) {
        this.statusWheel.setProgress(progress);
    }

    public void setMax(int max) {
        this.statusWheel.setMaxProgress(max);
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    public void setTitleTextBackgroundVisibility(int visibility) {
        titleTextBackground.setVisibility(visibility);
    }

    public void setProgressBarVisibility(int visibility) {
        progressBar.setVisibility(visibility);
    }
}
