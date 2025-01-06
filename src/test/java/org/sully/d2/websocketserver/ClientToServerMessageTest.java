package org.sully.d2.websocketserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.sully.d2.websocketserver.requests.ClientRequest;
import org.sully.d2.websocketserver.requests.TopNItemsRequest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ClientToServerMessageTest {

    @Test
    public void test() throws IOException {
        TopNItemsRequest request = TopNItemsRequest.builder()
                .useCase("UseCase1")
                .n(100)
                .build();

        ObjectMapper jackson = new ObjectMapper();

        String message = jackson.writeValueAsString(request);
        System.out.println(message);

        ClientRequest deserialized = jackson.readValue(message, ClientRequest.class);

        assertTrue(deserialized instanceof TopNItemsRequest);
        assertEquals(request, (TopNItemsRequest) deserialized);
    }
}