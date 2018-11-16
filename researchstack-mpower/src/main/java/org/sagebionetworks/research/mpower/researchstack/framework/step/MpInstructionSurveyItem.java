package org.sagebionetworks.research.mpower.researchstack.framework.step;

import com.google.gson.annotations.SerializedName;

import org.researchstack.backbone.model.survey.InstructionSurveyItem;

public class MpInstructionSurveyItem extends InstructionSurveyItem {

    /* Default constructor needed for serialization/deserialization of object */
    public MpInstructionSurveyItem() {
        super();
    }

    /**
     * When next button is clicked, step layout will perform a task ACTION_END
     */
    @SerializedName("actionEndOnNext")
    public Boolean actionEndOnNext;

    /**
     * When true, clicking the image view will advance to the next step
     */
    @SerializedName("advanceOnImageClick")
    public Boolean advanceOnImageClick;

    /**
     * A string representation of a color resource for the background
     */
    @SerializedName("backgroundColor")
    public String backgroundColorRes;

    /**
     * A string representation of a drawable resource for the background
     */
    @SerializedName("backgroundDrawable")
    public String backgroundDrawableRes;

    /**
     * Puts the image behind the toolbar
     */
    @SerializedName("behindToolbar")
    public boolean behindToolbar;

    /**
     * A string representation of a color resource for the text and button container background
     */
    @SerializedName("bottomContainerColor")
    public String bottomContainerColorRes;

    /**
     * A string representation of the color resource to use for the bottom link
     */
    @SerializedName("bottomLinkColor")
    public String bottomLinkColorRes;

    /**
     * The identifier of the step to go to when bottomLinkText is clicked
     */
    @SerializedName("bottomLinkStepId")
    public String bottomLinkStepId;

    /**
     * The identifier of the task to show when bottomLinkText is clicked
     */
    @SerializedName("bottomLinkTaskId")
    public String bottomLinkTaskId;

    /**
     * A string for the text of the optional link at the bottom. Hides link if no text is supplied
     */
    @SerializedName("bottomLinkText")
    public String bottomLinkText;

    /**
     * This will be used as the title on the button
     */
    @SerializedName("buttonText")
    public String buttonText;

    /**
     * When true, the text will be centered
     */
    @SerializedName("centerText")
    public Boolean centerText;

    /**
     * Hides the progress bar when this step is within a toolbar with progress
     */
    @SerializedName("hideProgress")
    public boolean hideProgress;

    /**
     * When true, there will be no toolbar
     */
    @SerializedName("hideToolbar")
    public boolean hideToolbar;

    /**
     * A string representation of a color resource for the image background
     */
    @SerializedName("imageColor")
    public String imageColorRes;

    /**
     * If true, volume buttons will control media, false it will go to default
     */
    @SerializedName("mediaVolume")
    public boolean mediaVolume;

    /**
     * A String representing a raw resource for a sound to play
     */
    @SerializedName("soundRes")
    public String soundRes;

    /**
     * A string representation of a color resource for the status bar
     */
    @SerializedName("statusBarColor")
    public String statusBarColorRes;

    /**
     * A string representation of a color resource for the submit bar
     */
    @SerializedName("submitBarColorRes")
    public String submitBarColorRes;

    /**
     * A String representing a color resource
     */
    @SerializedName("textColor")
    public String textColorRes;

    /**
     * A String representing a dimen resource
     */
    @SerializedName("textContainerHeightRes")
    public String textContainerHeightRes;

    /**
     * A string representation of a color resource for the toolbar tint
     */
    @SerializedName("tintColor")
    public String tintColorRes;

    /**
     * If true and scaletype matrix, will try to top crop the image instead of center cropping it.
     */
    @SerializedName("topCrop")
    public boolean topCrop;
}
