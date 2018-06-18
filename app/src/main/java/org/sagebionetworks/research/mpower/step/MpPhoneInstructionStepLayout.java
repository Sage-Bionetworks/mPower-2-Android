package org.sagebionetworks.research.mpower.step;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import org.researchstack.backbone.result.StepResult;
import org.researchstack.backbone.step.Step;
import org.researchstack.backbone.utils.ObservableUtils;
import org.sagebionetworks.bridge.rest.model.Phone;
import org.sagebionetworks.research.mpower.R;
import org.sagebionetworks.research.researchStack.MpDataProvider;
import rx.Subscription;

public class MpPhoneInstructionStepLayout extends MpInstructionStepLayout {

    private static final String LOG_TAG = MpPhoneInstructionStepLayout.class.getCanonicalName();

    private MpPhoneInstructionStep mpPhoneInstructionStep;
    protected EditText phoneEntryField;

    public MpPhoneInstructionStepLayout(Context context) {
        super(context);
    }

    public MpPhoneInstructionStepLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MpPhoneInstructionStepLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MpPhoneInstructionStepLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int getContentResourceId() {
        return R.layout.mp_step_layout_phone_entry;
    }

    @Override
    public void initialize(Step step, StepResult result) {
        validateAndSetMpStartTaskStep(step);
        super.initialize(step, result);
    }

    protected void validateAndSetMpStartTaskStep(Step step) {
        if (!(step instanceof MpPhoneInstructionStep)) {
            throw new IllegalStateException("MpPhoneInstructionStepLayout only works with MpPhoneInstructionStep");
        }
        this.mpPhoneInstructionStep = (MpPhoneInstructionStep) step;
    }

    @Override
    public void connectStepUi(int titleRId, int textRId, int imageRId, int detailRId) {
        super.connectStepUi(titleRId, textRId, imageRId, detailRId);
        phoneEntryField = findViewById(R.id.mp_entry_field);
    }


    @Override
    public void refreshStep() {
        super.refreshStep();

        phoneEntryField.setHint("Enter your mobile number");
        // TODO figure out back button layout issue
//        backButton.setVisibility(VISIBLE);

        nextButton.setOnClickListener(this::goForwardClicked);
    }

    public void goForwardClicked(View v) {
        MpDataProvider provider = MpDataProvider.getInstance();
        String phoneNumber = phoneEntryField.getText().toString();
        Phone phone = new Phone().number(phoneNumber).regionCode("US");

        Subscription response =  provider.signUp(phone)
                .compose(ObservableUtils.applyDefault())
                .subscribe(dataResponse -> {
                    if (dataResponse.isSuccess()) {
                        Log.d("Sign Up", "Successfully signed up!");
                    } else {
                        Log.d("Sign Up", "Error, message: " + dataResponse.getMessage());
                    }
                }, throwable -> Log.e("Sign Up", "Throwable " + throwable.getMessage()));
        super.goForwardClicked(v);
    }
}
