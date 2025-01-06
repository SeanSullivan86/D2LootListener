package org.sully.d2.websocketserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.sully.d2.D2LootListener;
import org.sully.d2.websocketserver.requests.ClientRequest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@ServerEndpoint(value = "/loot")
public class LootEndpoint {

    static ObjectMapper jackson = new ObjectMapper();
    private final ClientRequest.Handler messageHandler;

    public LootEndpoint() {
        this.messageHandler = D2LootListener.getRequestHandler();
    }

    static List<Session> sessions = new CopyOnWriteArrayList<>();

    public static void broadcast(String message) {
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @OnMessage
    public void onMessage(String messageString, Session session) {
        System.out.println("Received message : " + messageString);

        try {
            ClientRequest message = jackson.readValue(messageString, ClientRequest.class);
            Consumer<String> messageResponder = response -> sendMessage(session, response);
            message.acceptHandler(messageHandler, messageResponder);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Session opened : " + session.getId());
        sessions.add(session);
        // D2ItemListener.sendItems(session);

    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("Session closed : " + session.getId());
        sessions.remove(session);
        System.out.println(sessions.size() + " sessions remain");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}