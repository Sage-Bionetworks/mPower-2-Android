package org.sagebionetworks.research.researchStack;

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

    private Gson gson;

    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    // Field Vars
    //-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=
    private final SharedPreferences prefs;
    public SharedPreferences getPrefs() {
        return prefs;
    }
//    protected DailyActivitiesDAO dailyActivities;
//    protected OnceActivitiesDAO onceActivities;
//    protected ActivityListDAO needUpdatedOnBridgeActivities;
    private final SharedPreferences mpRecorderPrefs;

    public static final DateTimeFormatter FORMATTER = ISODateTimeFormat.dateTime().withOffsetParsed();


    @VisibleForTesting
    MpPrefs(Context context) {
        gson = new Gson();
        prefs = createPrefs(context);
        mpRecorderPrefs = PreferenceManager.getDefaultSharedPreferences(context);
//        dailyActivities = new DailyActivitiesDAO(context);
//        onceActivities = new OnceActivitiesDAO(context);
//        needUpdatedOnBridgeActivities = new NeedUpdatedOnBridgeActivitesDAO(context);
    }

    @VisibleForTesting
    SharedPreferences createPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        instance = new MpPrefs(context);
    }

    public static MpPrefs getInstance() {
        if(instance == null) {
            throw new RuntimeException(
                    "BpPrefs instance is null. Make sure it is initialized in ResearchStack before calling.");
        }
        return instance;
    }
}
