package org.sagebionetworks.research.mpower.sageresearch.archive;

import android.support.annotation.NonNull;

import com.google.common.collect.ImmutableSet;

import org.joda.time.DateTime;
import org.sagebionetworks.bridge.data.ArchiveFile;
import org.sagebionetworks.bridge.data.JsonArchiveFile;
import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.motor_control_module.result.TappingResult;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.AbstractResultArchiveFactory;

import javax.inject.Inject;

public class TappingResultArchiveFactory implements AbstractResultArchiveFactory.ResultArchiveFactory {
    @Inject
    public TappingResultArchiveFactory() {

    }

    @Override
    public boolean isSupported(@NonNull final Result result) {
        return result instanceof TappingResult;
    }

    @NonNull
    @Override
    public ImmutableSet<? extends ArchiveFile> toArchiveFiles(@NonNull final Result result) {
        return ImmutableSet.of(
                new JsonArchiveFile(
                        result.getIdentifier(),
                        new DateTime(result.getEndTime().toEpochMilli()),
                        ((TappingResult) result).getSamples()
                )
        );
    }
}
