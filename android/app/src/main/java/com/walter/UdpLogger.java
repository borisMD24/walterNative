package com.walter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import android.util.Log;

public class UdpLogger {

    private final InetAddress serverAddress;
    private final int port;
    private final DatagramSocket socket;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UdpLogger(String ip, int port) throws Exception {
        this.serverAddress = InetAddress.getByName(ip);
        this.port = port;
        this.socket = new DatagramSocket(); // Optionnellement réutilisable
    }

    private void send(String level, String message) {
        new Thread(() -> {
            try {
                String timestamp = LocalDateTime.now().format(formatter);
                String fullMessage = String.format("[%s] [%s] %s", timestamp, level, message);
                byte[] buffer = fullMessage.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, port);
                socket.send(packet);
                Log.d("UdpLogger", "✅ Paquet UDP envoyé depuis le thread : " + Thread.currentThread().getName());
            } catch (Exception e) {
                Log.e("UdpLogger", "❌ Erreur envoi UDP : " + e.getMessage(), e);
            }
        }).start();
    }

    public void info(String message) {
        send("INFO", message);
    }

    public void warn(String message) {
        send("WARN", message);
    }

    public void error(String message) {
        send("ERROR", message);
    }

    public void close() {
        socket.close();
    }
}
