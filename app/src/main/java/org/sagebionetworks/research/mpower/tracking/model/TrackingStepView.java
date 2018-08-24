package org.sagebionetworks.research.mpower.tracking.model;

import android.support.annotation.NonNull;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import org.sagebionetworks.research.domain.async.AsyncActionConfiguration;
import org.sagebionetworks.research.domain.step.interfaces.Step;
import org.sagebionetworks.research.presentation.mapper.DrawableMapper;
import org.sagebionetworks.research.presentation.model.interfaces.StepView;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@AutoValue
public abstract class TrackingStepView implements StepView {
    public static final String TYPE = "tracking";

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract TrackingStepView build();

        @NonNull
        public abstract Builder setIdentifier(@NonNull String identifier);

        @NonNull
        public abstract Builder setSelectionInfo(@NonNull TrackingSubstepInfo selectionInfo);

        @NonNull
        public abstract Builder setLoggingInfo(@NonNull TrackingSubstepInfo loggingInfo);

        @NonNull
        public abstract Builder setItems(@NonNull Set<TrackingItem> items);

        @NonNull
        public abstract Builder setSections(@NonNull Set<TrackingSection> sections);
    }

    @Override
    public int getNavDirection() {
        return NavDirection.SHIFT_LEFT;
    }

    @NonNull
    public abstract ImmutableSet<TrackingItem> getItems();

    @NonNull
    public abstract ImmutableSet<TrackingSection> getSections();

    @NonNull
    public abstract TrackingSubstepInfo getSelectionInfo();

    @NonNull
    public abstract TrackingSubstepInfo getLoggingInfo();

    /**
     * Returns a Map that maps from TrackingSection to a set of TrackingItems in that section. The map orders both
     * sections and items alphabetically.
     * @return a Map that maps from TrackingSection to a set of TrackingItems in that section.
     */
    @NonNull
    public Map<TrackingSection, Set<TrackingItem>> getSelectionItems() {
        Map<TrackingSection, Set<TrackingItem>> result = new TreeMap<>((o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier()));
        for (TrackingSection section : this.getSections()) {
            result.put(section, new TreeSet<>((o1, o2) -> o1.getIdentifier().compareTo(o2.getIdentifier())));
            Set<TrackingItem> itemSet = result.get(section);
            for (TrackingItem item : this.getItems()) {
                if (section.getIdentifier().equals(item.getSectionIdentifier())) {
                    itemSet.add(item);
                }
            }
        }

        return result;
    }

    @Override
    @NonNull
    public String getType() {
        return TYPE;
    }

    @NonNull
    public static Builder builder() {
        return new AutoValue_TrackingStepView.Builder()
                .setItems(ImmutableSet.of())
                .setSections(ImmutableSet.of());
    }

    @NonNull
    public abstract Builder toBuilder();

    @NonNull
    public static TrackingStepView fromTrackingStep(@NonNull Step step, @NonNull DrawableMapper drawableMapper) {
        if (!(step instanceof TrackingStep)) {
            throw new IllegalArgumentException("Provided step " + step + " is not a TrackingStep.");
        }

        TrackingStep trackingStep = (TrackingStep)step;
        Set<TrackingSection> sections = !trackingStep.getSections().isEmpty() ? trackingStep.getSections() :
                TrackingStepView.getSectionsFromItems(trackingStep.getItems());
        return TrackingStepView.builder()
                .setIdentifier(trackingStep.getIdentifier())
                .setItems(trackingStep.getItems())
                .setLoggingInfo(trackingStep.getLoggingInfo())
                .setSections(sections)
                .setSelectionInfo(trackingStep.getSelectionInfo())
                .build();
    }

    private static Set<TrackingSection> getSectionsFromItems(ImmutableSet<TrackingItem> trackingItems) {
        Set<TrackingSection> sections = new HashSet<>();
        for (TrackingItem item : trackingItems) {
            if (!TrackingStepView.containsSectionWithIdentifier(sections, item.getSectionIdentifier())) {
                sections.add(TrackingSection.builder().setIdentifier(item.getSectionIdentifier()).build());
            }
        }

        return sections;
    }

    private static boolean containsSectionWithIdentifier(Set<TrackingSection> sections, String identifier) {
        for (TrackingSection section : sections) {
            if (section.getIdentifier().equals(identifier)) {
                return true;
            }
        }

        return false;
    }
}
