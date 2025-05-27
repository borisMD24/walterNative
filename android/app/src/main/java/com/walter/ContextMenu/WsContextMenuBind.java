package com.walter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import android.util.Log;
import org.json.JSONObject;
import org.json.JSONException;
import java.util.function.Consumer;


public class WsContextMenuBind extends WebSocketListener {
    private static final String TAG = "WsContextMenuBind";
    private OkHttpClient client;
    private WebSocket webSocket;
    private boolean isConnected = false;
    protected String room = "floatingBubble";
    private ContextMenuContext menuContext;
    private Consumer<String> msgCallback;
    public WsContextMenuBind() {
        initWebSocket();
    }

    private void initWebSocket() {
        client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("ws://192.168.1.18:3000/")
                .build();

        webSocket = client.newWebSocket(request, this);
        Log.d(TAG, "WebSocket connection initiated");
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        isConnected = true;
        Log.d(TAG, "WebSocket connection opened successfully");
        sendMessage("{\"action\": \"join\", \"room\":\"" + room + "\"}");
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        Log.d(TAG, "Received message: " + text);

        // Handle incoming messages here
        handleMessage(text);
    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
        Log.d(TAG, "Received bytes: " + bytes.hex());

        // Handle binary messages if needed
    }

    public void onMessage(Consumer<String> cb){
        this.msgCallback = cb;
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        Log.d(TAG, "WebSocket closing: " + code + " / " + reason);
        webSocket.close(1000, null);
        isConnected = false;
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        Log.d(TAG, "WebSocket closed: " + code + " / " + reason);
        isConnected = false;
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, Response response) {
        super.onFailure(webSocket, t, response);
        Log.e(TAG, "WebSocket failure: " + t.getMessage(), t);
        isConnected = false;

        // Optional: Implement reconnection logic
        // scheduleReconnect();
    }

    // Send text message to server
    public boolean sendMessage(String message) {
        if (webSocket != null && isConnected) {
            boolean sent = webSocket.send(message);
            if (sent) {
                Log.d(TAG, "Message sent: " + message);
            } else {
                Log.e(TAG, "Failed to send message: " + message);
            }
            return sent;
        } else {
            Log.e(TAG, "WebSocket is not connected. Cannot send message: " + message);
            return false;
        }
    }

    public void sendToRoom(String msg) {
        try {
            JSONObject json = new JSONObject();
            json.put("action", "message");
            json.put("room", room);
            json.put("data", msg);
            sendMessage(json.toString());
        } catch (JSONException e) {
            e.printStackTrace(); // ou log propre
            // Optionnel : envoyer un fallback / log vers ton serveur / show toast
        }
    }

    public void sendToRoom(JSONObject msg) {
        try {
            JSONObject json = new JSONObject();
            json.put("action", "message");
            json.put("room", room);
            json.put("data", msg);
            sendMessage(json.toString());
        } catch (JSONException e) {
            e.printStackTrace(); // ou log propre
            // Optionnel : envoyer un fallback / log vers ton serveur / show toast
        }
    }

    // Send binary message to server
    public boolean sendMessage(ByteString bytes) {
        if (webSocket != null && isConnected) {
            boolean sent = webSocket.send(bytes);
            if (sent) {
                Log.d(TAG, "Binary message sent");
            } else {
                Log.e(TAG, "Failed to send binary message");
            }
            return sent;
        } else {
            Log.e(TAG, "WebSocket is not connected. Cannot send binary message");
            return false;
        }
    }

    // Handle incoming messages - override this method for custom message handling
    protected void handleMessage(String message) {
        // Default implementation - just log the message
        // Override this in subclass or modify as needed for your context menu logic
        Log.d(TAG, "Handling message: " + message);
        if (this.msgCallback != null) {
            this.msgCallback.accept(message);
        }
    }

    // Check connection status
    public boolean isConnected() {
        return isConnected;
    }

    // Close the WebSocket connection
    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Client disconnecting");
            Log.d(TAG, "WebSocket disconnection initiated");
        }
    }

    // Reconnect to WebSocket server
    public void reconnect() {
        disconnect();
        try {
            Thread.sleep(1000); // Wait a bit before reconnecting
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        initWebSocket();
    }

    // Clean up resources
    public void cleanup() {
        disconnect();
        if (client != null) {
            client.dispatcher().executorService().shutdown();
        }
    }
}