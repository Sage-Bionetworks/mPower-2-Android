package org.sagebionetworks.research.mpower.tracking.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.constraintlayout.widget.ConstraintLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import org.sagebionetworks.research.mpower.R


class DosageDetailsWidget : ConstraintLayout {

    @BindView(R.id.dosage_edit_text)
    @JvmField
    var editText: EditText? = null

    @BindView(R.id.radio_button_any_time)
    @JvmField
    var anytimeRadioButton: RadioButton? = null;

    @BindView(R.id.radio_button_schedule)
    @JvmField
    var scheduleRadioButton: RadioButton? = null;

    @BindView(R.id.select_days)
    @JvmField
    var selectDays: MedicationAddDetailsWidget? = null;

    @BindView(R.id.select_times)
    @JvmField
    var selectTimes: MedicationAddDetailsWidget? = null;

    constructor(context: Context) : super(context) {
        commonInit()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        commonInit()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        commonInit()
    }

    private fun commonInit() {
        View.inflate(context, R.layout.mpower2_dosage_details, this)
        ButterKnife.bind(this)
    }


}
