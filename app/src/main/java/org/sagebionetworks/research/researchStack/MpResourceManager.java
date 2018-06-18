package org.sagebionetworks.research.researchStack;

import android.text.TextUtils;
import org.researchstack.backbone.ResourceManager;
import org.researchstack.backbone.model.SchedulesAndTasksModel;
import org.researchstack.backbone.model.SectionModel;
import org.researchstack.backbone.model.TaskModel;

public class MpResourceManager extends ResourceManager {
    public static final int PEM     = 4;
    public static final int SURVEY  = 5;

    public static final String BASE_PATH_HTML          = "html";
    private static final String BASE_PATH_JSON          = "json";
    private static final String BASE_PATH_JSON_SURVEY   = "json/survey";
    private static final String BASE_PATH_PDF           = "pdf";
    private static final String BASE_PATH_VIDEO         = "mp4";

    public static final String SIGNUP_TASK_RESOURCE         = "signup";

    public MpResourceManager() {
        super();

        addTaskResource(SIGNUP_TASK_RESOURCE);
    }

    protected void addSurveyResource(String resourceName) {
        addResource(resourceName, new Resource(SURVEY, BASE_PATH_JSON_SURVEY, resourceName));
    }

    protected void addTaskResource(String resourceName) {
        addResource(resourceName, new Resource(Resource.TYPE_JSON, BASE_PATH_JSON, resourceName));
    }

    @Override
    public Resource getStudyOverview() {
        // TODO: Determine if this is still necessary
        return null;
    }

    @Override
    public Resource getConsentHtml() {
        return null;
    }

    @Override
    public Resource getConsentPDF() {
        return null;
    }

    @Override
    public Resource getConsentSections() {
        return new Resource(Resource.TYPE_JSON, BASE_PATH_JSON, "learn", SectionModel.class);
    }

    @Override
    public Resource getLearnSections() {
        return new Resource(Resource.TYPE_HTML, BASE_PATH_HTML, "app_privacy_policy");
    }

    @Override
    public Resource getPrivacyPolicy() {
        return new Resource(Resource.TYPE_HTML, BASE_PATH_HTML, "license");
    }

    @Override
    public Resource getSoftwareNotices() {
        return new Resource(Resource.TYPE_HTML, BASE_PATH_HTML, "software_notices");
    }

    @Override
    public Resource getTasksAndSchedules() {
        return new Resource(Resource.TYPE_JSON,
                BASE_PATH_JSON,
                "tasks_and_schedules",
                SchedulesAndTasksModel.class);
    }

    @Override
    public Resource getTask(String taskFileName) {
        return new Resource(Resource.TYPE_JSON,
                BASE_PATH_JSON_SURVEY,
                taskFileName,
                TaskModel.class);
    }

    @Override
    public Resource getInclusionCriteria() {
        return null;
    }

    @Override
    public Resource getOnboardingManager() {
        return null;
    }

    @Override
    public String generatePath(int type, String name) {
        String dir;
        switch (type) {
            default:
                dir = null;
                break;
            case Resource.TYPE_HTML:
                dir = BASE_PATH_HTML;
                break;
            case Resource.TYPE_JSON:
                dir = BASE_PATH_JSON;
                break;
            case Resource.TYPE_PDF:
                dir = BASE_PATH_PDF;
                break;
            case Resource.TYPE_MP4:
                dir = BASE_PATH_VIDEO;
                break;
            case SURVEY:
                dir = BASE_PATH_JSON_SURVEY;
                break;
        }

        StringBuilder path = new StringBuilder();
        if (!TextUtils.isEmpty(dir)) {
            path.append(dir).append("/");
        }

        return path.append(name).append(".").append(getFileExtension(type)).toString();
    }

    @Override
    public String getFileExtension(int type) {
        switch (type) {
            case PEM:
                return "pem";
            case SURVEY:
                return "json";
            default:
                return super.getFileExtension(type);
        }
    }
}
