package org.sagebionetworks.research.mpower.step;

import com.google.gson.annotations.SerializedName;
import org.researchstack.backbone.model.survey.InstructionSurveyItem;

public class MpInstructionSurveyItem extends InstructionSurveyItem {

    /**
     * This will be used as the title on the button
     */
    @SerializedName("buttonText")
    public String buttonText;

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
     * A string representation of a color resource for the image background
     */
    @SerializedName("imageColor")
    public String imageColorRes;

    /**
     * A string representation of a color resource for the toolbar tint
     */
    @SerializedName("tintColor")
    public String tintColorRes;

    /**
     * A string representation of a color resource for the status bar
     */
    @SerializedName("statusBarColor")
    public String statusBarColorRes;

    /**
     * Hides the progress bar when this step is within a toolbar with progress
     */
    @SerializedName("hideProgress")
    public boolean hideProgress;

    /**
     * Puts the image behind the toolbar
     */
    @SerializedName("behindToolbar")
    public boolean behindToolbar;

    /**
     * When true, there will be no toolbar
     */
    @SerializedName("hideToolbar")
    public boolean hideToolbar;

    /**
     * If true, volume buttons will control media, false it will go to default
     */
    @SerializedName("mediaVolume")
    public boolean mediaVolume;

    /**
     * A String representing a color resource
     */
    @SerializedName("textColor")
    public String textColorRes;

    /**
     * A string for the text of the optional link at the bottom. Hides link if
     * no text is supplied
     */
    @SerializedName("bottomLinkText")
    public String bottomLinkText;

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
     * If true and scaletype matrix, will try to top crop the image instead of center
     * cropping it.
     */
    @SerializedName("topCrop")
    public boolean topCrop;

    /**
     * A String representing a dimen resource
     */
    @SerializedName("textContainerHeightRes")
    public String textContainerHeightRes;

    /**
     * When true, the text will be centered
     */
    @SerializedName("centerText")
    public Boolean centerText;

    /**
     * A String representing a raw resource for a sound to play
     */
    @SerializedName("soundRes")
    public String soundRes;

    /**
     * A string representation of a color resource for the submit bar
     */
    @SerializedName("submitBarColorRes")
    public String submitBarColorRes;

    /**
     * When true, clicking the image view will advance to the next step
     */
    @SerializedName("advanceOnImageClick")
    public Boolean advanceOnImageClick;

    /**
     * When next button is clicked, step layout will perform a task ACTION_END
     */
    @SerializedName("actionEndOnNext")
    public Boolean actionEndOnNext;

    /**
     * A string representation of a color resource for the text and button container background
     */
    @SerializedName("bottomContainerColor")
    public String bottomContainerColorRes;
}
