package org.sagebionetworks.research.mpower.inject;

import com.google.gson.TypeAdapterFactory;
import com.ryanharter.auto.value.gson.GsonTypeAdapterFactory;

/**
 * Auto-generated TypeAdapterFactory for @AutoValue classes in the :domain module. Due to how the auto-value-gson
 * annotationProcessor works, a separate @GsonTypeAdapterFactory is needed for each gradle module.
 */
@GsonTypeAdapterFactory
public abstract class AppAutoValueTypeAdapterFactory implements TypeAdapterFactory {
    //     Static factory method to access the package
    //     private generated implementation
    public static TypeAdapterFactory create() {
        return new AutoValueGson_AppAutoValueTypeAdapterFactory();
    }
}