package org.sully.d2.websocketserver.responses;

import lombok.Builder;
import lombok.Value;
import org.sully.d2.gamemodel.D2Item;

import java.util.List;
import java.util.Map;

@Value
@Builder
public class UseCaseAndCategoryNamesResponse implements ServerResponse {
    String requestId;
    Map<String, List<String>> categoryNamesByUseCase;
}


