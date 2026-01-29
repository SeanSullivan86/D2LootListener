package org.sully.d2.websocketserver.responses;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;
import org.sully.d2.itemtracking.ItemAndScore;
import org.sully.d2.websocketserver.requests.ScoreDistributionRequest;
import org.sully.d2.websocketserver.requests.TopNItemsRequest;

import java.util.List;

@Value
@Builder
public class TopNItemsResponse implements ServerResponse{
    /*
    String requestId;
    List<ItemAndScore<D2Item>> diffToFillTopN;
    int n;

     */
}

