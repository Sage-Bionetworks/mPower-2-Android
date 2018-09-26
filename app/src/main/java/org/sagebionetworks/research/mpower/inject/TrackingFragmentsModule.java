package org.sagebionetworks.research.mpower.inject;

import org.sagebionetworks.research.mpower.tracking.fragment.MedicationAddDetailsFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.MedicationSchedulingFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.MedicationReviewFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.MedicationSelectionFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.SymptomLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.SymptomSelectionFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TimePickerFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TriggersLoggingFragment;
import org.sagebionetworks.research.mpower.tracking.fragment.TriggersSelectionFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class TrackingFragmentsModule {
    @ContributesAndroidInjector
    abstract TriggersSelectionFragment contributeTriggersSelectionFragmentInjector();

    @ContributesAndroidInjector
    abstract TriggersLoggingFragment contributeTriggersLoggingFragmentInjector();

    @ContributesAndroidInjector
    abstract SymptomSelectionFragment contributeSymptomSelectionFragmentInjector();

    @ContributesAndroidInjector
    abstract SymptomLoggingFragment contributeSymptomLogginFragmentInjector();

    @ContributesAndroidInjector
    abstract MedicationSelectionFragment contributeMedicationSelectionFragmentInjector();

    @ContributesAndroidInjector
    abstract MedicationAddDetailsFragment contributeMedicationAddDetailsFragmentInjector();

    @ContributesAndroidInjector
    abstract MedicationSchedulingFragment contributeMedicationSchedulingFragmentInjector();

    @ContributesAndroidInjector
    abstract MedicationReviewFragment contributeMedicationReviewFragmentInjector();

    @ContributesAndroidInjector
    abstract TimePickerFragment contributeTimePickerFragmentInjector();
}
