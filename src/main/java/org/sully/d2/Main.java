package org.sully.d2;

import org.glassfish.tyrus.server.Server;
import org.sully.d2.websocketserver.LootEndpoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        Server server = new Server("localhost", 8025, "/ws", null, LootEndpoint.class);

        try {
            server.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Please press a key to stop the server.");
            reader.readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            server.stop();
        }
    }
}