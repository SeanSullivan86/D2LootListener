package org.sully.d2.itemtracking;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;

@Value
@Builder
public class BasicStats {
    D2Item[] mostRecentItemsByQuality;
    long[] countsByQuality;
    long runtimeMs;
    long dropCount;
}
