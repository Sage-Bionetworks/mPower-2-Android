package org.sagebionetworks.research.mpower.researchstack.framework;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.VisibleForTesting;

import com.google.gson.Gson;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class MpPrefs {

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Statics
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

    private static MpPrefs instance;

    public static final DateTimeFormatter FORMATTER = ISODateTimeFormat.dateTime().withOffsetParsed();

    private Gson gson;

    public static final String KEY_REMINDER_FREQUENCY_BODY_AND_MIND = "reminder_frequency_body_and_mind";
    public static final String KEY_REMINDER_FREQUENCY_MORNING = "reminder_frequency_morning";
    public static final String KEY_REMINDER_FREQUENCY_EVENING = "reminder_frequency_evening";
    public static final String KEY_REMINDER_TIME_BODY_AND_MIND = "reminder_time_body_and_mind";
    public static final String KEY_REMINDER_TIME_MORNING = "reminder_time_morning";
    public static final String KEY_REMINDER_TIME_EVENING = "reminder_time_evening";
    public static final String KEY_REMINDERS_NOTIFICATIONS_GLOBAL = "reminder_notifications_global";

    //    protected DailyActivitiesDAO dailyActivities;
//    protected OnceActivitiesDAO onceActivities;
//    protected ActivityListDAO needUpdatedOnBridgeActivities;
    private final SharedPreferences mpRecorderPrefs;

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Field Vars
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private final SharedPreferences prefs;

    public static MpPrefs getInstance() {
        if (instance == null) {
            throw new RuntimeException(
                    "BpPrefs instance is null. Make sure it is initialized in ResearchStack before calling.");
        }
        return instance;
    }

    public static void init(Context context) {
        instance = new MpPrefs(context);
    }

    @VisibleForTesting
    MpPrefs(Context context) {
        gson = new Gson();
        prefs = createPrefs(context);
        mpRecorderPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        dailyActivities = new DailyActivitiesDAO(context);
//        onceActivities = new OnceActivitiesDAO(context);
//        needUpdatedOnBridgeActivities = new NeedUpdatedOnBridgeActivitesDAO(context);
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    @VisibleForTesting
    SharedPreferences createPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
