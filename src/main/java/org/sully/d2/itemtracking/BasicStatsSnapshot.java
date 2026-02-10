package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.server.ConsumerSummary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Value
@Builder
public class BasicStatsSnapshot implements TCDropConsumerSnapshot {
    long[] countsByQuality;
    Long[] mostRecentItemIdsByQuality;
    long totalIterations;

    String name;

    @Override
    public Set<Long> getReferencedItemIds() {
        return Arrays.stream(mostRecentItemIdsByQuality).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    @Override
    public ConsumerSummary toSummaryObject() {
        return ConsumerSummary.builder()
                .consumerName(name)
                .consumerType(this.getClass().getSimpleName())
                .additionalInfo(BasicStatsSummary.builder()
                        .countsByQuality(countsByQuality)
                        .totalIterations(totalIterations)
                        .build())
                .build();
    }


}

