package org.sagebionetworks.research.mpower.tracking.view_model.logs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.sagebionetworks.research.mpower.tracking.view_model.configs.TrackingItemConfig;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;

@AutoValue
public abstract class MedicationLog implements TrackingItemLog, TrackingItemConfig {
    @AutoValue.Builder
    public abstract static class Builder {
        public abstract MedicationLog build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setText(@Nullable String text);

        @NonNull
        public abstract Builder setDosageItems(@NonNull List<DosageItem> dosageItems);

        @NonNull
        public abstract Builder setLoggedDate(@Nullable Instant loggedDate);

    }

    @Nullable
    @Override
    public abstract String getText();

    @NonNull
    public abstract List<DosageItem> getDosageItems();


    @Nullable
    @Override
    public abstract Instant getLoggedDate();

    @Override
    public boolean isConfigured() {
        return getDosageItems() != null && getDosageItems().size() > 0 &&
                getDosageItems().get(0).getDosage() != null && !getDosageItems().get(0).getDosage().isEmpty();
    }

    public MedicationLog copy(boolean clearLoggedDate) {
        List<DosageItem> dosages = new ArrayList<>();
        for(DosageItem dosage: getDosageItems()) {
            dosages.add(dosage.copy(clearLoggedDate));
        }
        return builder().setDosageItems(dosages)
                .setIdentifier(getIdentifier())
                .setText(getText()).build();
    }

    @NonNull
    public static Builder builder() {
        return new AutoValue_MedicationLog.Builder();
    }

    @NonNull
    public static TypeAdapter<MedicationLog> typeAdapter(Gson gson) {
        return new AutoValue_MedicationLog.GsonTypeAdapter(gson);
    }

    @NonNull
    public abstract Builder toBuilder();

}
