package org.sagebionetworks.research.mpower;

import static com.google.common.base.Preconditions.checkState;

import static junit.framework.Assert.assertEquals;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.io.CharStreams;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;

public class JsonAssetUtil {
    public static <T> void assertJsonFileEqualRef(T expected, Gson gson, String filename, Class<T> klass) {
        T result = readJsonFile(gson, filename, TypeToken.of(klass));
        assertEquals((expected), (result));
    }

    @NonNull
    public static <T> T readJsonFile(Gson gson, String filename, TypeToken<T> tt) {
        ClassLoader loader = JsonAssetUtil.class.getClassLoader();
        URL url = loader.getResource(filename);
        checkState(url != null, "invalid URL for filename: %s", filename);
        String json = readJsonFileHelper(gson, url);
        checkState(json != null, "Failed to read file: %s", filename);
        T result = gson.fromJson(json, tt.getType());
        checkState(result != null, "Failed to deserialize json: %s", json);
        return result;
    }

    @Nullable
    public static String readJsonFileHelper(Gson gson, URL url) {
        try (BufferedReader reader = Files.newBufferedReader(new File(url.getFile()).toPath(), UTF_8)) {
            return CharStreams.toString(reader);
        } catch (IOException e) {
            return null;
        }
    }

    @NonNull
    public static <T> T readJsonFile(Gson gson, String filename, Class<T> klass) {
        return readJsonFile(gson, filename, TypeToken.of(klass));
    }
}
