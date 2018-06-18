package org.sagebionetworks.research.mpower.step;

public class MpPhoneInstructionStep extends MpInstructionStep {
    /**
     * Text to go in text view as hint text
     */
    public String phoneHint;

    /* Default constructor needed for serialization/deserialization of object */
    public MpPhoneInstructionStep() {
        super();
    }

    public MpPhoneInstructionStep(String identifier, String title, String detailText) {
        super(identifier, title, detailText);
    }

    @Override
    public Class getStepLayoutClass() {
        return MpPhoneInstructionStepLayout.class;
    }
}
