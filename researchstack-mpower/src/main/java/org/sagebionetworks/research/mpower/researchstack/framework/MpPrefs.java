package org.sagebionetworks.research.mpower.researchstack.framework;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.VisibleForTesting;

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
