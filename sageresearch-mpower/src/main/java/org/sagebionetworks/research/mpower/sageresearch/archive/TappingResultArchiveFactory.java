package org.sagebionetworks.research.mpower.sageresearch.archive;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.sagebionetworks.bridge.data.ArchiveFile;
import org.sagebionetworks.bridge.data.JsonArchiveFile;
import org.sagebionetworks.research.domain.result.AnswerResultType;
import org.sagebionetworks.research.domain.result.implementations.AnswerResultBase;
import org.sagebionetworks.research.domain.result.interfaces.Result;
import org.sagebionetworks.research.modules.motor_control.result.TappingResult;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.AbstractResultArchiveFactory;
import org.sagebionetworks.research.sageresearch_app_sdk.archive.AnswerResultArchiveFactory;
import org.threeten.bp.Instant;
import org.threeten.bp.ZonedDateTime;

import java.util.Set;

import javax.inject.Inject;

public class TappingResultArchiveFactory implements AbstractResultArchiveFactory.ResultArchiveFactory {
    private static final String RESULT_POSTFIX_TAPPING_SAMPLES = ".samples";

    private final AnswerResultArchiveFactory answerResultArchiveFactory;

    @Inject
    public TappingResultArchiveFactory(AnswerResultArchiveFactory answerResultArchiveFactory, Gson gson) {

        this.answerResultArchiveFactory = answerResultArchiveFactory;
    }

    @Override
    public boolean isSupported(@NonNull final Result result) {
        return result instanceof TappingResult;
    }

    @NonNull
    @Override
    public ImmutableSet<? extends ArchiveFile> toArchiveFiles(@NonNull final Result result) {

        TappingResult tappingResult = (TappingResult) result;

        DateTime endDateTime = new DateTime(tappingResult.getEndTime().toEpochMilli());

        ImmutableSet.Builder<ArchiveFile> builder = ImmutableSet.builder();
        builder.add(new JsonArchiveFile(
                result.getIdentifier() + RESULT_POSTFIX_TAPPING_SAMPLES,
                endDateTime,
                tappingResult.getSamples()));
        builder.add(createButtonBounds(result.getIdentifier() + ".buttonRectLeft", tappingResult.getButtonBoundLeft()));
        builder.add(createButtonBounds(result.getIdentifier() + ".buttonRectRight", tappingResult.getButtonBoundRight()));
        builder.add(createViewSize(result.getIdentifier() + ".viewSize", tappingResult.getStepViewSize()));
        builder.addAll(createZonedDateTimeArchives(tappingResult.getIdentifier() + ".startDate", endDateTime,
                tappingResult.getZonedStartTime()));
        builder.addAll(createZonedDateTimeArchives(tappingResult.getIdentifier() + ".endDate", endDateTime,
                tappingResult.getZonedEndTime()));
        return builder.build();
    }

    @NonNull
    ArchiveFile createButtonBounds(@NonNull String identifier, @NonNull @Size(4) int[] buttonBounds) {
        String buttonBound =
                "{{" + buttonBounds[0] + "," + buttonBounds[1] + "},{" + buttonBounds[2] + "," + buttonBounds[3]
                        + "}}";
        return answerResultArchiveFactory.toArchiveFiles(
                new AnswerResultBase<>(identifier, Instant.now(), Instant.now(), buttonBound,
                        AnswerResultType.STRING)).asList().get(0);
    }

    @NonNull
    ArchiveFile createViewSize(@NonNull String identifier, @NonNull @Size(2) int[] viewSize) {
        return answerResultArchiveFactory.toArchiveFiles(
                new AnswerResultBase<>(identifier, Instant.now(), Instant.now(),
                        "{" + viewSize[0] + "," + viewSize[1] + "}",
                        AnswerResultType.STRING)).asList().get(0);
    }

    @NonNull
    Set<ArchiveFile> createZonedDateTimeArchives(@NonNull String identifier, DateTime endTime,
            @NonNull ZonedDateTime zonedDateTime) {

        DateTime dateTime = new DateTime(
                zonedDateTime.toInstant().toEpochMilli(),
                DateTimeZone.forID(zonedDateTime.getZone().getId()));
        return ImmutableSet.of(
                new JsonArchiveFile(identifier + "", endTime, dateTime),
                new JsonArchiveFile(identifier + ".timeZone", endTime, dateTime));
    }
}
