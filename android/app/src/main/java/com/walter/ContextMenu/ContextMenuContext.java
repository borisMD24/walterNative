package com.walter;

import java.util.function.Consumer;

import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.content.Context;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import com.walter.WsContextMenuBind;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.walter.UdpLogger;
import com.walter.json.Room;
import com.walter.json.Theme;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ContextMenuContext {
    public int nthOppened = -1;
    private Consumer<Integer> onCloseCallback = null;
    private List<Runnable> onMoveCallbacks = new ArrayList<>();
    private List<Runnable> onBubbleResizeCallbacks = new ArrayList<>();
    private List<Runnable> onBubbleFixedCallbacks = new ArrayList<>();
    private List<Runnable> onBrightnessChangeCallbacks = new ArrayList<>();
    private List<Runnable> onRoomsUpdateCallbacks = new ArrayList<>();
    private List<Runnable> onThemesUpdateCallbacks = new ArrayList<>();
    private List<Runnable> onCurrentRoomUpdateCallbacks = new ArrayList<>();
    private List<Runnable> onCurrentThemeUpdateCallbacks = new ArrayList<>();
    protected int bubbleY = 0;
    protected float normalizedBubbleY = 0;
    protected int bubbleX = 0;
    protected float normalizedBubbleX = 0;
    protected int screenHeight;
    protected int screenWidth;
    protected int bubbleSize = 200;
    protected int roomSelectionX;
    protected int roomSelectionY;
    protected int themeSelectionX;
    protected int themeSelectionY;
    protected float brightness;
    protected boolean isSettingBrightnessFromServer = false;
    protected WsContextMenuBind ws;
    private Context ctx;
    protected int currentRoomId = 0;
    private UdpLogger logger;
    protected List<Room> roomList;
    protected List<Theme> themeList;
    private final String configFile = "contextMenuContext.json";
    protected int currentThemeId = 0;
    ContextMenuContext(Context ctx) {
        this.ctx = ctx;
        setScreenHeight();
        setScreenWidth();
        initializeLogger();
        ws = new WsContextMenuBind();
        ws.onMessage((msg) -> {
            this.handleMessage(msg);
        });
        String data = ""; // ← initialisation sûre
        try (FileInputStream fis = ctx.openFileInput(configFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
        } catch (IOException e) {
            logInfo("Erreur lecture config : " + e.getMessage());
        }
        handleMessage(data, true);

    }

    private void initializeLogger() {
        try {
            logger = new UdpLogger("192.168.1.18", 9999);
        } catch (Exception e) {
            // Log.e(TAG, "Failed to initialize UDP logger: " + e.getMessage(), e);
        }
    }

    public void logInfo(String message) {
        if (logger != null) {
            logger.info(message);
        }
    }

    public void oppened(int nth, Consumer<Integer> onClose) {
        // if oppened, close current and open new
        if (isOpen()) {
            // Store the old nth value before changing it
            int oldNth = this.nthOppened;
            // Execute the current callback if it exists
            if (this.onCloseCallback != null) {
                this.onCloseCallback.accept(oldNth);
            }
        }

        // Open the new one
        this.nthOppened = nth;
        // set on close lambda
        this.onCloseCallback = onClose;
    }

    public void onMove(Runnable callback) {
        onMoveCallbacks.add(callback);
    }

    public void onBubbleResize(Runnable callback) {
        onBubbleResizeCallbacks.add(callback);
    }

    public void onBubbleFix(Runnable callback) {
        onBubbleFixedCallbacks.add(callback);
    }

    public void onBrightnessChange(Runnable callback) {
        onBrightnessChangeCallbacks.add(callback);
    }

    public void onRoomsUpdate(Runnable callback) {
        onRoomsUpdateCallbacks.add(callback);
    }

    public void onThemesUpdate(Runnable callback) {
        onThemesUpdateCallbacks.add(callback);
    }

    public void onCurrentThemeUpdate(Runnable callback) {
        onCurrentThemeUpdateCallbacks.add(callback);
    }

    private void setRooms(List<Room> rooms) {
        roomList = rooms;
        onRoomsUpdateCallbacks.forEach(Runnable::run);
    }

    private void setThemes(List<Theme> themes) {
        themeList = themes;
        onThemesUpdateCallbacks.forEach(Runnable::run);
    }

    public void setCurrentTheme(int id){
        currentThemeId = id;
        onCurrentThemeUpdateCallbacks.forEach(Runnable::run);
    }

    public void close(int nth) {
        if (this.nthOppened == nth) {
            // run on close lambda
            if (this.onCloseCallback != null) {
                this.onCloseCallback.accept(nth);
            }

            this.nthOppened = -1;
            // remove on close lambda
            this.onCloseCallback = null;
        }
    }

    // Optional helper method to check if menu is currently open
    public boolean isOpen() {
        return this.nthOppened != -1;
    }

    public void forceClose() {
        // Force close the currently open menu regardless of its nth value
        if (isOpen()) {
            // Execute the callback if it exists
            if (this.onCloseCallback != null) {
                this.onCloseCallback.accept(this.nthOppened);
            }

            // Reset the state
            this.nthOppened = -1;
            this.onCloseCallback = null;
        }
    }

    public void setScreenWidth() {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
    }

    public void setScreenHeight() {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
    }

    public void setBubbleY(int y) {
        bubbleY = y;
        float minY = bubbleSize;
        float maxY = screenHeight - bubbleSize;
        float clampedY = Math.max(minY, Math.min(bubbleY, maxY));
        normalizedBubbleY = (clampedY - minY) / (maxY - minY);
    }

    public void setBubbleSize(int size) {
        bubbleSize = size;
        onBubbleResizeCallbacks.forEach(Runnable::run);
    }

    public void setBubbleX(int x) {
        bubbleX = x;
        float minX = bubbleSize;
        float maxX = screenWidth - bubbleSize;
        float clampedX = Math.max(minX, Math.min(bubbleX, maxX));
        normalizedBubbleX = (clampedX - minX) / (maxX - minX);
    }

    public void setBrightness(float b) {
        brightness = b;
        onBrightnessChangeCallbacks.forEach(Runnable::run);
    }

    public void setBubbleCoords(int x, int y) {
        setBubbleX(x);
        setBubbleY(y);
        onMoveCallbacks.forEach(Runnable::run);
    }

    public void setBubbleFixed() {
        onBubbleFixedCallbacks.forEach(Runnable::run);
    }

    public void setRoomSelectionCoords(int x, int y) {
        this.roomSelectionX = x;
        this.roomSelectionY = y;
    }

    public void setThemeSelectionCoords(int x, int y) {
        this.themeSelectionX = x;
        this.themeSelectionY = y;
    }

    public void onCurrentRoomUpdate(Runnable cb){
        onCurrentRoomUpdateCallbacks.add(cb);
    }
    public void postToServer(JSONObject data) {
        try {
            JSONObject json = new JSONObject();
            json.put("payload", data);
            json.put("roomID", currentRoomId);
            ws.sendToRoom(json);
        } catch (JSONException e) {
            e.printStackTrace(); // ou log propre
            // Optionnel : envoyer un fallback / log vers ton serveur / show toast
        }
    }

    public void setRoomId(int id) {
        this.currentRoomId = id;
        onCurrentRoomUpdateCallbacks.forEach(Runnable::run);
    }

    private void handleWelcome(JSONObject json) {
        try {
            // Check if this is a welcome message
            if (json.has("welcome") && json.getBoolean("welcome")) {
                System.out.println("Welcome message received!");

                // Parse rooms
                if (json.has("rooms")) {
                    JSONArray roomsArray = json.getJSONArray("rooms");
                    List<Room> rooms = parseRooms(roomsArray);

                    for (Room room : rooms) {
                        logInfo(room.toString());
                    }

                    setRooms(rooms);
                }

                // Parse themes
                if (json.has("themes")) {
                    JSONArray themesArray = json.getJSONArray("themes");
                    List<Theme> themes = parseThemes(themesArray);

                    for (Theme theme : themes) {
                        logInfo(theme.toString());
                    }

                    setThemes(themes);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing welcome message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Room> parseRooms(JSONArray roomsArray) {
        List<Room> rooms = new ArrayList<>();

        for (int i = 0; i < roomsArray.length(); i++) {
            try {
                JSONObject roomObj = roomsArray.getJSONObject(i);
                String name = roomObj.getString("name");
                int id = roomObj.getInt("id");
                String img = roomObj.getString("img");

                rooms.add(new Room(name, id, img));
            } catch (Exception e) {
                System.err.println("Error parsing room at index " + i + ": " + e.getMessage());
            }
        }

        return rooms;
    }

    private List<Theme> parseThemes(JSONArray themesArray) {
        List<Theme> themes = new ArrayList<>();

        for (int i = 0; i < themesArray.length(); i++) {
            try {
                JSONObject themeObj = themesArray.getJSONObject(i);
                String name = themeObj.getString("name");
                int id = themeObj.getInt("id");
                String img = themeObj.getString("img");
                int roomID = themeObj.getInt("roomID");

                themes.add(new Theme(name, id, img, roomID));

            } catch (Exception e) {
                System.err.println("Error parsing theme at index " + i + ": " + e.getMessage());
            }
        }

        return themes;
    }

    private void handleMessage(String msg) {
        handleMessage(msg, false);
    }

    private void handleMessage(String msg, boolean force) {
        String data = "";
        // Lecture sécurisée du fichier de config
        try (FileInputStream fis = ctx.openFileInput(configFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
        } catch (IOException e) {
            logInfo("Erreur lecture config : " + e.getMessage());
            // On peut choisir de continuer ou non
        }

        // Comparaison par contenu
        if (msg.equals(data) && !force) {
            return;
        }

        try {
            JSONObject jsonMsg = new JSONObject(msg);
            if (jsonMsg.has("welcome")) {
                // Écriture sécurisée
                try (FileOutputStream fos = ctx.openFileOutput(configFile, Context.MODE_PRIVATE)) {
                    fos.write(msg.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    logInfo("Erreur écriture config : " + e.getMessage());
                }
                logInfo("It's a welcome");
                handleWelcome(jsonMsg);
                return;
            }

            JSONObject message = jsonMsg.getJSONObject("message");
            JSONObject payload = message.getJSONObject("payload");

            if (message.has("roomID")) {
                int roomId = message.getInt("roomID");
                if (this.currentRoomId != roomId) {
                    return;
                }
            } else {
                return;
            }

            if (payload.has("brightness")) {
                int brightness = payload.getInt("brightness");
                logInfo("Brightness should be set to: " + brightness);
                isSettingBrightnessFromServer = true;
                setBrightness(Math.abs((1 - normalizedBubbleX) - (brightness / 255f)));
                isSettingBrightnessFromServer = false;
            }
        } catch (JSONException e) {
            logInfo("Failed to parse message: " + e.getMessage());
        }
    }

}