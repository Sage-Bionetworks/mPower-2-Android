package org.sagebionetworks.research.mpower;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.junit.Test;
import org.researchstack.backbone.onboarding.ReConsentInstructionStep;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.threeten.bp.Instant;

import java.lang.reflect.ParameterizedType;
import java.net.URL;

public class SymptomsLogTest {
    private static final Gson GSON = DaggerTrackingTestComponent.builder().build().gson();
    private static final String UNRECORDED_ITEM = "Drooling";
    private static final String RECORDED_ITEM = "Amnesia";
    private static final Instant RECORDED_TIMESTAMP = Instant.parse("2018-08-29T12:23:58.435Z");
    private static final String RECORDED_MEDICATION_TIMING = "post-medication";
    private static final String RECORDED_DURATION = "DURATION_CHOICE_AFTERNOON";
    private static final Integer RECORDED_SEVERITY = 0;

    private static final JsonObject RECORDED_JSON;
    static {
        RECORDED_JSON = new JsonObject();
        RECORDED_JSON.addProperty("identifier", RECORDED_ITEM);
        RECORDED_JSON.addProperty("text", RECORDED_ITEM);
        RECORDED_JSON.addProperty("severity", RECORDED_SEVERITY);
        RECORDED_JSON.addProperty("loggedDate",  RECORDED_TIMESTAMP.toString());
        RECORDED_JSON.addProperty("duration", RECORDED_DURATION);
        RECORDED_JSON.addProperty("medicationTiming", RECORDED_MEDICATION_TIMING);
    }

    private static final JsonObject UNRECORDED_JSON;
    static {
        UNRECORDED_JSON = new JsonObject();
        UNRECORDED_JSON.addProperty("identifier", UNRECORDED_ITEM);
        UNRECORDED_JSON.addProperty("text", UNRECORDED_ITEM);
    }

    @Test
    public void test_serializeUnrecorded() {
        SymptomLog log = SymptomLog.builder()
                .setIdentifier(UNRECORDED_ITEM)
                .setText(UNRECORDED_ITEM)
                .build();
        JsonElement serialized = GSON.toJsonTree(log);
        assertNotNull("Log serialization failed", serialized);
        assertEquals("Log serialization produced an unexpected result", UNRECORDED_JSON, serialized);
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
        JsonElement serialized = GSON.toJsonTree(log);
        assertNotNull("Json serialization failed", serialized);
        assertEquals("Log serialization produced an unexpected result", RECORDED_JSON, serialized);
    }

    @Test
    public void test_deserializeUnrecorded() {
        SymptomLog log = GSON.fromJson(UNRECORDED_JSON, SymptomLog.class);
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
        SymptomLog log = GSON.fromJson(RECORDED_JSON, SymptomLog.class);
        assertNotNull("Log failed to deserialize", log);
        assertEquals("Log had unexpected identifier", RECORDED_ITEM, log.getIdentifier());
        assertEquals("Log had unexpected text", RECORDED_ITEM, log.getText());
        assertEquals("Log had unexpected severity", RECORDED_SEVERITY, log.getSeverity());
        assertEquals("Log had unexpected timestamp", RECORDED_TIMESTAMP, log.getTimestamp());
        assertEquals("Log had unexpected duration", RECORDED_DURATION, log.getDuration());
        assertEquals("Log had unexpected medication timing", RECORDED_MEDICATION_TIMING, log.getMedicationTiming());
    }
}
