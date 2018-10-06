package org.sagebionetworks.research.mpower;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.junit.Test;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.threeten.bp.Instant;

import java.lang.reflect.Type;


public class LoggingCollectionTest {
    private static final Gson GSON = DaggerTrackingTestComponent.builder().build().gson();

    private static final String IDENTIFIER = "trackedItems";
    private static final Instant SYMPTOM_DATE = Instant.parse("2018-08-29T12:23:49.514Z");
    private static final Instant TRIGGER_DATE = Instant.parse("2018-08-29T12:24:20.767Z");

    private static final SymptomLog SYMPTOM_LOG_1 = SymptomLog.builder()
            .setIdentifier("Hallucinations")
            .setText("Hallucinations")
            .build();

    private static final SymptomLog SYMPTOM_LOG_2 = SymptomLog.builder()
            .setIdentifier("Drooling")
            .setText("Drooling")
            .build();

    private static final SymptomLog SYMPTOM_LOG_3 = SymptomLog.builder()
            .setIdentifier("Amnesia")
            .setText("Amnesia")
            .setSeverity(0)
            .setDuration("DURATION_CHOICE_AFTERNOON")
            .setMedicationTiming("post-medication")
            .setTimestamp(Instant.parse("2018-08-29T12:23:58.435Z"))
            .build();

    private static final SimpleTrackingItemLog TRIGGER_LOG_1 = SimpleTrackingItemLog.builder()
            .setIdentifier("Hot")
            .setText("Hot")
            .build();

    private static final SimpleTrackingItemLog TRIGGER_LOG_2 = SimpleTrackingItemLog.builder()
            .setIdentifier("Sitting still")
            .setText("Sitting still")
            .build();

    private static final SimpleTrackingItemLog TRIGGER_LOG_3 = SimpleTrackingItemLog.builder()
            .setIdentifier("Bedtime, late")
            .setText("Bedtime, late")
            .setTimestamp(Instant.parse("2018-08-29T12:24:26.552Z"))
            .build();

    private static final JsonObject TRIGGERS_JSON;
    static {
        TRIGGERS_JSON = new JsonObject();
        TRIGGERS_JSON.addProperty("identifier", IDENTIFIER);
        TRIGGERS_JSON.addProperty("startDate", TRIGGER_DATE.toString());
        TRIGGERS_JSON.addProperty("endDate", TRIGGER_DATE.toString());
        TRIGGERS_JSON.addProperty("type", "loggingCollection");
        JsonArray array = new JsonArray();
        // We add the serialized trigger logs to the expected to make this test independent of trigger log serialization.
        array.add(GSON.toJsonTree(TRIGGER_LOG_1));
        array.add(GSON.toJsonTree(TRIGGER_LOG_2));
        array.add(GSON.toJsonTree(TRIGGER_LOG_3));
        TRIGGERS_JSON.add("items", array);
    }

    private static final JsonObject SYMPTOMS_JSON;
    static {
        SYMPTOMS_JSON = new JsonObject();
        SYMPTOMS_JSON.addProperty("identifier", IDENTIFIER);
        SYMPTOMS_JSON.addProperty("startDate", SYMPTOM_DATE.toString());
        SYMPTOMS_JSON.addProperty("endDate", SYMPTOM_DATE.toString());
        SYMPTOMS_JSON.addProperty("type", "loggingCollection");
        JsonArray array = new JsonArray();
        // We add the serialized symptom logs to the expected to make this test independent of symptom log serialization.
        array.add(GSON.toJsonTree(SYMPTOM_LOG_1));
        array.add(GSON.toJsonTree(SYMPTOM_LOG_2));
        array.add(GSON.toJsonTree(SYMPTOM_LOG_3));
        SYMPTOMS_JSON.add("items", array);
    }

    @Test
    public void test_serializeSymptom() {
        LoggingCollection<SymptomLog> loggingCollection = LoggingCollection.<SymptomLog>builder()
                .setStartDate(SYMPTOM_DATE)
                .setEndDate(SYMPTOM_DATE)
                .setIdentifier(IDENTIFIER)
                .setType("loggingCollection")
                .setItems(ImmutableList.of(SYMPTOM_LOG_1, SYMPTOM_LOG_2, SYMPTOM_LOG_3))
                .build();

        JsonElement serialized = GSON.toJsonTree(loggingCollection, new TypeToken<LoggingCollection<SymptomLog>>() {}.getType());
        assertNotNull("LoggingCollection serialization failed", serialized);
        assertEquals("LoggingCollection serialization produced an unexpected result", SYMPTOMS_JSON, serialized);
    }

    @Test
    public void test_serializeTrigger() {
        LoggingCollection<SimpleTrackingItemLog> loggingCollection
                = LoggingCollection.<SimpleTrackingItemLog>builder()
                .setStartDate(TRIGGER_DATE)
                .setEndDate(TRIGGER_DATE)
                .setIdentifier(IDENTIFIER)
                .setType("loggingCollection")
                .setItems(ImmutableList.of(TRIGGER_LOG_1, TRIGGER_LOG_2, TRIGGER_LOG_3))
                .build();

        JsonElement serialized = GSON.toJsonTree(loggingCollection, new TypeToken<LoggingCollection<SimpleTrackingItemLog>>() {}.getType());
        assertNotNull("LoggingCollection serialization failed", serialized);
        assertEquals("LoggingCollection serialization produced an unexpected result", TRIGGERS_JSON, serialized);
    }

    @Test
    public void test_deserializeSymptom() {
        LoggingCollection<SymptomLog> loggingCollection =
                GSON.fromJson(SYMPTOMS_JSON, new TypeToken<LoggingCollection<SimpleTrackingItemLog>>() {}.getType());
        assertNotNull("Failed to deserialize logging collection", loggingCollection);
        assertEquals("Logging collection had unexpected start date", SYMPTOM_DATE, loggingCollection.getStartDate());
        assertEquals("Logging collection had unexpected end date", SYMPTOM_DATE, loggingCollection.getEndDate());
        assertEquals("Logging collection had unexpected identifier", IDENTIFIER, loggingCollection.getIdentifier());
        // Making sure the items deserialize correctly is the job of a different test, for this test it is enough that there
        // are the correct number of items
        assertEquals("Logging collection had a different number of items than was expected", 3,
                loggingCollection.getItems().size());
    }

    @Test
    public void test_deserializeTrigger() {
        Type type = new TypeToken<LoggingCollection<SimpleTrackingItemLog>>() {}.getType();
        LoggingCollection<SimpleTrackingItemLog> loggingCollection =
                GSON.fromJson(TRIGGERS_JSON, type);
        assertNotNull("Failed to deserialize logging collection", loggingCollection);
        assertEquals("Logging collection had unexpected start date", TRIGGER_DATE, loggingCollection.getStartDate());
        assertEquals("Logging collection had unexpected end date", TRIGGER_DATE, loggingCollection.getEndDate());
        assertEquals("Logging collection had unexpected identifier", IDENTIFIER, loggingCollection.getIdentifier());
        // Making sure the items deserialize correctly is the job of a different test, for this test it is enough that there
        // are the correct number of items
        assertEquals("Logging collection had a different number of items than was expected", 3,
                loggingCollection.getItems().size());
    }
}
