package org.sully.d2.websocketserver.requests;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

@Value
@Builder
public class ScoreDistributionRequest implements ClientRequest {
    String useCase;
    String category;
    String requestId;

    @Override
    public void acceptHandler(Handler handler, Consumer<String> responseSender) {
        handler.handle(this, responseSender);
    }
}
