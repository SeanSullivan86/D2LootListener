package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BasicStatsSummary {
    long totalIterations;
    long[] countsByQuality;
}
