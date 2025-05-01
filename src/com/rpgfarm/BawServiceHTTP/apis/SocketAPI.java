package com.rpgfarm.BawServiceHTTP.apis;

import com.rpgfarm.BawServiceHTTP.Main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

public class SocketAPI {
    static private Thread serverThread;
    static private ServerSocket serverSocket;
    
    static public void startSocketThread() {
        serverThread = new Thread(() -> {
            int port = Integer.parseInt(Main.singleInstance.config.getString("setting.port"));
            Main.singleInstance.getLogger().info("Minehub Monetize API Listener 서버를 시작합니다. ("+port+")");
            try (ServerSocket server = new ServerSocket(port)) {
                serverSocket = server;
                serverSocket.setReuseAddress(true);
                while (serverSocket.isBound()) {
                    Socket client;
                    try { client = server.accept(); } catch (IOException e) { continue; }
                    OutputStreamWriter osr = new OutputStreamWriter(client.getOutputStream());
                    BufferedWriter bw = new BufferedWriter(osr);
                    PrintWriter pw = new PrintWriter(bw);
                    pw.println("OK");
                    pw.flush();
                    client.close();
                    HTTPAPI.runWebCheck();
                }
            } catch (IOException e) {
                Main.singleInstance.getLogger().log(Level.SEVERE, "Minehub Monetize API Listener에 오류가 있습니다.");
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    static public void stopSocketThread() {
        Main.singleInstance.getLogger().info("Minehub Monetize API Listener 서버를 종료합니다.");
        serverThread.interrupt();
        try {serverSocket.close(); } catch (IOException ignored) {}
    }
}
