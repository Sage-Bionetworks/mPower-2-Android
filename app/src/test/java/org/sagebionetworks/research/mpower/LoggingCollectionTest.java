package org.sagebionetworks.research.mpower;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.junit.Test;
import org.sagebionetworks.research.domain.JsonAssetUtil;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.LoggingCollection;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SimpleTrackingItemLog;
import org.sagebionetworks.research.mpower.tracking.view_model.logs.SymptomLog;
import org.threeten.bp.Instant;

import java.net.URL;


public class LoggingCollectionTest {
    private static final Gson GSON = DaggerTrackingTestComponent.builder().build().gson();

    private static final String IDENTIFIER = "trackedItems";

    private static final Instant SYMPTOM_DATE = Instant.parse("2018-08-29T12:23:49.514-07:00");

    private static final Instant TRIGGER_DATE = Instant.parse("2018-08-29T12:24:20.767-07:00");

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
            .setTimestamp(Instant.parse("2018-08-29T12:23:58.435-07:00"))
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
            .setTimestamp(Instant.parse("2018-08-29T12:24:26.552-07:00"))
            .build();

    @Test
    public void test_serializeSymptom() {
        LoggingCollection<SymptomLog> loggingCollection = LoggingCollection.<SymptomLog>builder()
                .setStartDate(SYMPTOM_DATE)
                .setEndDate(SYMPTOM_DATE)
                .setIdentifier(IDENTIFIER)
                .setItems(ImmutableList.of(SYMPTOM_LOG_1, SYMPTOM_LOG_2, SYMPTOM_LOG_3))
                .build();

        String serialized = GSON.toJson(loggingCollection).replaceAll("\\s+", "");
        URL url = TriggersLogTest.class.getClassLoader()
                .getResource("logging_collection/LoggingCollection_Symptom.json");
        String expected = JsonAssetUtil.readJsonFileHelper(GSON, url);
        assertNotNull("Error loading expected resource file", expected);
        expected = expected.replaceAll("\\s+", "");
        assertEquals("LogCollection serialization produced an unexpected result", expected, serialized);
    }

    @Test
    public void test_serializeTrigger() {
        LoggingCollection<SimpleTrackingItemLog> loggingCollection
                = LoggingCollection.<SimpleTrackingItemLog>builder()
                .setStartDate(TRIGGER_DATE)
                .setEndDate(TRIGGER_DATE)
                .setIdentifier(IDENTIFIER)
                .setItems(ImmutableList.of(TRIGGER_LOG_1, TRIGGER_LOG_2, TRIGGER_LOG_3))
                .build();

        String serialized = GSON.toJson(loggingCollection).replaceAll("\\s+", "");
        URL url = TriggersLogTest.class.getClassLoader()
                .getResource("logging_collection/LoggingCollection_Trigger.json");
        String expected = JsonAssetUtil.readJsonFileHelper(GSON, url);
        assertNotNull("Error loading expected resource file", expected);
        expected = expected.replaceAll("\\s+", "");
        assertEquals("LogCollection serialization produced an unexpected result", expected, serialized);
    }

    @Test
    public void test_deserializeSymptom() {
        // Using com.google.gson.reflect.TypeToken to make getting a parameterized TypeToken easier.
        // noinspection unchecked
        com.google.gson.reflect.TypeToken<LoggingCollection<SymptomLog>> token =
                (com.google.gson.reflect.TypeToken<LoggingCollection<SymptomLog>>)
                        com.google.gson.reflect.TypeToken.getParameterized(LoggingCollection.class, SymptomLog.class);
        // noinspection unchecked
        TypeToken<LoggingCollection<SymptomLog>> convertedToken = (TypeToken<LoggingCollection<SymptomLog>>) TypeToken
                .of(token.getType());
        LoggingCollection<SymptomLog> loggingCollection = JsonAssetUtil.readJsonFile(GSON,
                "logging_collection/LoggingCollection_Symptom.json", convertedToken);
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
        // Using com.google.gson.reflect.TypeToken to make getting a parameterized TypeToken easier.
        // noinspection unchecked
        com.google.gson.reflect.TypeToken<LoggingCollection<SimpleTrackingItemLog>> token =
                (com.google.gson.reflect.TypeToken<LoggingCollection<SimpleTrackingItemLog>>)
                        com.google.gson.reflect.TypeToken.getParameterized(LoggingCollection.class, SimpleTrackingItemLog.class);
        // noinspection unchecked
        TypeToken<LoggingCollection<SimpleTrackingItemLog>> convertedToken = (TypeToken<LoggingCollection<SimpleTrackingItemLog>>) TypeToken
                .of(token.getType());
        LoggingCollection<SimpleTrackingItemLog> loggingCollection = JsonAssetUtil.readJsonFile(GSON,
                "logging_collection/LoggingCollection_Trigger.json", convertedToken);
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
