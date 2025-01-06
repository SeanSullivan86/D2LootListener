package org.sully.d2.websocketserver.responses;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.itemtracking.ItemGridCounts;

@Value
@Builder
public class ItemGridCountsResponse {
    String requestId;
    ItemGridCounts counts;
}
