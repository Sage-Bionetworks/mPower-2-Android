package org.sagebionetworks.research.mpower.step;

import org.researchstack.backbone.result.TaskResult;
import org.researchstack.backbone.step.InstructionStep;
import org.researchstack.backbone.utils.StepResultHelper;

import java.util.List;

public class MpInstructionStep extends InstructionStep {

    public static final String SKIP_RESULT_ID = "MpInstructionStepSkipped";

    /**
     * This will be used as the title on the button
     */
    public String buttonText;

    /**
     * A string representation of a color resource for the view background
     */
    public String backgroundColorRes;

    /**
     * A string representation of a drawable resource for the background
     */
    public String backgroundDrawableRes;

    /**
     * A string representation of a color resource for the image background
     */
    public String imageBackgroundColorRes;

    /**
     * A string representation of a color resource for the text and button container background
     */
    public String bottomContainerColorRes;

    /**
     * A string representation of a color resource for the toolbar tint
     */
    public String tintColorRes;

    /**
     * A string representation of a color resource for the status bar
     */
    public String statusBarColorRes;

    /**
     * Hides the progress bar when this step is within a toolbar with progress
     */
    public boolean hideProgress;

    /**
     * Puts the image behind the toolbar
     */
    public boolean behindToolbar;

    /**
     * When true, there will be no toolbar
     */
    public boolean hideToolbar;

    /**
     * If true, volume buttons will control media, false it will go to default
     */
    public boolean mediaVolume;

    /**
     * A String representing a color resource
     */
    public String textColorRes;

    /**
     * Text to populate the optional bottom button. If null, button will be hidden
     */
    public String bottomLinkText;

    /**
     * Bool, if true and scaletype is matrix will try to top crop the image
     */
    public boolean topCrop;

    /**
     * A String representing a dimen resource
     */
    public String textContainerHeightRes;

    /**
     * The identifier of the step to go to when the bottom link is clicked
     */
    public String bottomLinkStepId;

    /**
     * The identifier of the task to show when bottomLinkText is clicked
     */
    public String bottomLinkTaskId;

    /**
     * A string representation of the color resource to use for the bottom link
     */
    public String bottomLinkColorRes;

    /**
     * When true, the text will be centered
     */
    public Boolean centerText;

    /**
     * A String representing a raw resource for a sound to play
     */
    public String soundRes;

    /**
     * A string representation of a color resource for the submit bar
     */
    public String submitBarColorRes;

    /**
     * When true, clicking the image view will advance to the next step
     */
    public boolean advanceOnImageClick;

    /**
     * When next button is clicked, step layout will perform a task ACTION_END
     */
    public Boolean actionEndOnNext;

    /* Default constructor needed for serialization/deserialization of object */
    public MpInstructionStep() {
        super();
    }

    public MpInstructionStep(String identifier, String title, String detailText) {
        super(identifier, title, detailText);
    }

    @Override
    public Class getStepLayoutClass() {
        return MpInstructionStepLayout.class;
    }

    @Override
    public String nextStepIdentifier(TaskResult result, List<TaskResult> additionalTaskResults) {
        if (StepResultHelper.findBooleanResult(SKIP_RESULT_ID, result) != null) {
            return bottomLinkStepId;
        }
        return super.nextStepIdentifier(result, additionalTaskResults);
    }
}
