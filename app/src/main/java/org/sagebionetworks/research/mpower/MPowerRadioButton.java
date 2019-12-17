package org.sagebionetworks.research.mpower;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MPowerRadioButton extends ConstraintLayout {
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.button)
    ImageView button;
    private boolean selected;

    public MPowerRadioButton(final Context context) {
        super(context);
        this.commonInit(null, 0);
    }

    public MPowerRadioButton(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.commonInit(attrs, 0);
    }

    public MPowerRadioButton(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.commonInit(attrs, defStyleAttr);
    }

    private void commonInit(@Nullable AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(this.getContext()).inflate(R.layout.mpower2_radio_button, this);
        ButterKnife.bind(this);
        this.button.setImageResource(R.drawable.mpower2_radio_button_unselected);
        String title = "";
        if (attrs != null) {
            TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.MPowerRadioButton, defStyleAttr, 0);
            title = a.getString(R.styleable.MPowerRadioButton_text);
            a.recycle();
        }

        this.setTitle(title);
    }

    public void setTitle(@NonNull String text) {
        this.title.setText(text);
    }

    public String getTitle() {
        return this.title.getText().toString();
    }

    @Override
    public boolean isSelected() {
        return this.selected;
    }

    @Override
    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            if (this.selected) {
                this.button.setImageResource(R.drawable.mpower2_radio_button_selected);
            } else {
                this.button.setImageResource(R.drawable.mpower2_radio_button_unselected);
            }
        }
    }
}
