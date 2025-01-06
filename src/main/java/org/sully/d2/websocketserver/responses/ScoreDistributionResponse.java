package org.sully.d2.websocketserver.responses;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.websocketserver.requests.ScoreDistributionRequest;

import java.util.List;

@Value
@Builder
public class ScoreDistributionResponse implements ServerResponse {
    String requestId;
    List<Integer> scores;
    List<Long> counts;
}
