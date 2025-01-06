package org.sully.d2.websocketserver.responses;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class LiveItemStatsResponse implements ServerResponse{
    long[] countDiffsByQuality;
    long[] totalCountsByQuality;
    double millisecondsSinceLastSnapshot;
}
