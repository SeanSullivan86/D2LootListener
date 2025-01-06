package org.sully.d2.util;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class StreamD2SocketDataToFileForTesting {

    public static void main(String[] args) throws IOException {
        int port = 5492;

        try (
            ServerSocket serverSocket = new ServerSocket(port);
            Socket clientSocket = serverSocket.accept();
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            InputStream in = new BufferedInputStream(clientSocket.getInputStream());
            OutputStream fileOutput = new BufferedOutputStream(new FileOutputStream("d2SocketData.bin"))
        ) {

            for (int i = 0; i < 1_000_000_000; i++) {
                int x = in.read();
                if (x == -1) {
                    throw new RuntimeException("End of stream");
                }
                fileOutput.write(x);
            }
        }
    }
}
