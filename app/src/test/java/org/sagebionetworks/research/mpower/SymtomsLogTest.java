package org.sagebionetworks.research.mpower;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import com.google.gson.Gson;

import org.junit.Test;
import org.sagebionetworks.research.domain.JsonAssetUtil;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.threeten.bp.Instant;

import java.net.URL;

public class SymtomsLogTest {
    private static final Gson GSON = DaggerTrackingTestComponent.builder().build().gson();
    private static final String UNRECORDED_ITEM = "Drooling";
    private static final String RECORDED_ITEM = "Amnesia";
    private static final Instant RECORDED_TIMESTAMP = Instant.parse("2018-08-29T12:23:58.435-07:00");
    private static final String RECORDED_MEDICATION_TIMING = "post-medication";
    private static final String RECORDED_DURATION = "DURATION_CHOICE_AFTERNOON";
    private static final Integer RECORDED_SEVERITY = 0;

    @Test
    public void test_serializeUnrecorded() {
        SymptomLog log = SymptomLog.builder()
                .setIdentifier(UNRECORDED_ITEM)
                .setText(UNRECORDED_ITEM)
                .build();
        String serialized = GSON.toJson(log).replaceAll("\\s+", "");
        URL url = TriggersLogTest.class.getClassLoader().getResource("symptoms/SymptomsLog_Unrecorded.json");
        String expected = JsonAssetUtil.readJsonFileHelper(GSON, url);
        assertNotNull("Error loading expected resource file", expected);
        expected = expected.replaceAll("\\s+", "");
        assertEquals("Log serialization produced an unexpected result", expected, serialized);
    }

    @Test
    public void test_serializeRecorded() {
        SymptomLog log = SymptomLog.builder()
                .setIdentifier(RECORDED_ITEM)
                .setText(RECORDED_ITEM)
                .setTimestamp(RECORDED_TIMESTAMP)
                .setMedicationTiming(RECORDED_MEDICATION_TIMING)
                .setDuration(RECORDED_DURATION)
                .setSeverity(RECORDED_SEVERITY)
                .build();
        String serialized = GSON.toJson(log).replaceAll("\\s+", "");
        URL url = TriggersLogTest.class.getClassLoader().getResource("symptoms/SymptomsLog_Unrecorded.json");
        String expected = JsonAssetUtil.readJsonFileHelper(GSON, url);
        assertNotNull("Error loading expected resource file", expected);
        expected = expected.replaceAll("\\s+", "");
        assertEquals("Log serialization produced an unexpected result", expected, serialized);
    }

    @Test
    public void test_deserializeUnrecorded() {
        SymptomLog log =
                JsonAssetUtil.readJsonFile(GSON, "symptoms/SymptomLog_Unrecorded.json", SymptomLog.class);
        assertNotNull("Log failed to deserialize", log);
        assertEquals("Log had unexpected identifier", UNRECORDED_ITEM, log.getIdentifier());
        assertEquals("Log had unexpected text", UNRECORDED_ITEM, log.getText());
        assertNull("Log had non-null severity", log.getSeverity());
        assertNull("Log had non-null duration", log.getDuration());
        assertNull("Log had non-null medication timing", log.getMedicationTiming());
        assertNull("Log had non-null timestamp", log.getTimestamp());
    }

    @Test
    public void test_deserializeRecorded() {
        SymptomLog log =
                JsonAssetUtil.readJsonFile(GSON, "symptoms/SymptomLog_Recorded.json", SymptomLog.class);
        assertNotNull("Log failed to deserialize", log);
        assertEquals("Log had unexpected identifier", RECORDED_ITEM, log.getIdentifier());
        assertEquals("Log had unexpected text", RECORDED_ITEM, log.getText());
        assertEquals("Log had unexpected severity", RECORDED_SEVERITY, log.getSeverity());
        assertEquals("Log had unexpected timestamp", RECORDED_TIMESTAMP, log.getTimestamp());
        assertEquals("Log had unexpected duration", RECORDED_DURATION, log.getDuration());
        assertEquals("Log had unexpected medication timing", RECORDED_MEDICATION_TIMING, log.getMedicationTiming());
    }
}
