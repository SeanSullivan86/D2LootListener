package org.sully.d2.websocketserver.requests;

import lombok.Builder;
import lombok.Value;

import java.util.function.Consumer;

@Value
@Builder
public class UseCaseAndCategoryNamesRequest implements ClientRequest {

    String requestId;

    @Override
    public void acceptHandler(ClientRequest.Handler handler, Consumer<String> responseSender) {
        handler.handle(this, responseSender);
    }
}

