package org.sully.d2.websocketserver.responses;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;

import java.util.Map;

@Value
@Builder
public class HomePageResponse implements ServerResponse {
    String requestId;
    long timeInMilliseconds;
    D2Item[] recentItemsByQuality;
    long[] itemCountsByQuality;
}
