package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;

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
}
