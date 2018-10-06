package org.sagebionetworks.research.mpower;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.junit.Test;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.threeten.bp.Instant;

public class TriggersLogTest {
    private static final Gson GSON = DaggerTrackingTestComponent.builder().build().gson();

    private static final String UNRECORDED_ITEM = "Hot";
    private static final String RECORDED_ITEM = "Bedtime, late";
    private static final Instant RECORDED_TIMESTAMP = Instant.parse("2018-08-29T12:24:26.552Z");

    private static final JsonObject RECORDED_JSON;
    static {
        RECORDED_JSON = new JsonObject();
        RECORDED_JSON.addProperty("identifier", RECORDED_ITEM);
        RECORDED_JSON.addProperty("text", RECORDED_ITEM);
        RECORDED_JSON.addProperty("loggedDate", RECORDED_TIMESTAMP.toString());
    }

    private static final JsonObject UNRECORDED_JSON;
    static {
        UNRECORDED_JSON = new JsonObject();
        UNRECORDED_JSON.addProperty("identifier", UNRECORDED_ITEM);
        UNRECORDED_JSON.addProperty("text", UNRECORDED_ITEM);
    }

    @Test
    public void test_serializeUnrecorded() {
        SimpleTrackingItemLog log = SimpleTrackingItemLog.builder()
                .setIdentifier(UNRECORDED_ITEM)
                .setText(UNRECORDED_ITEM)
                .build();
        JsonElement serialized = GSON.toJsonTree(log);
        assertNotNull("Log serialization failed", serialized);
        assertEquals("Log serialization produced an unexpected result", UNRECORDED_JSON, serialized);
    }

    @Test
    public void test_serializeRecorded() {
        SimpleTrackingItemLog log = SimpleTrackingItemLog.builder()
                .setIdentifier(RECORDED_ITEM)
                .setText(RECORDED_ITEM)
                .setTimestamp(RECORDED_TIMESTAMP)
                .build();
        JsonElement serialized = GSON.toJsonTree(log);
        assertNotNull("Log serialization failed", serialized);
        assertEquals("Log serialization produced an unexpected result", RECORDED_JSON, serialized);
    }

    @Test
    public void test_deserializeUnrecorded() {
        SimpleTrackingItemLog log = GSON.fromJson(UNRECORDED_JSON, SimpleTrackingItemLog.class);
        assertNotNull(log);
        assertEquals("Log had unexpected identifier", UNRECORDED_ITEM, log.getIdentifier());
        assertEquals("Log had unexpected text", UNRECORDED_ITEM, log.getText());
        assertNull("Log had non-null timestamp", log.getTimestamp());
    }

    @Test
    public void test_deserializeRecorded() {
        SimpleTrackingItemLog log = GSON.fromJson(RECORDED_JSON, SimpleTrackingItemLog.class);
        assertNotNull(log);
        assertEquals("Log had unexpected identifier", RECORDED_ITEM, log.getIdentifier());
        assertEquals("Log had unexpected text", RECORDED_ITEM, log.getText());
        assertEquals("Log had unexpected timestamp", RECORDED_TIMESTAMP, log.getTimestamp());
    }
}
