package com.walter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import com.walter.GetJson;
import com.walter.ContextMenuContext;

/**
 * Component that displays a grid of room thumbnails based on JSON data
 */
public class RoomSelectionComponent {
    // Constants
    private static final int DEFAULT_THUMBNAIL_SIZE = 100;
    private static final int DEFAULT_SPACING = 20;
    private static final int BACKGROUND_PADDING = 30;
    private static final int BACKGROUND_CORNER_RADIUS = 20;
    private static final int BACKGROUND_COLOR = Color.WHITE;
    
    // Member variables
    private final Context context;
    private final ViewGroup container;
    private final List<RoomThumbnail> thumbnails = new ArrayList<>();
    private int thumbnailRadius = DEFAULT_THUMBNAIL_SIZE / 2;
    private int screenWidth;
    private OnRoomSelectedListener roomSelectedListener;
    private BackgroundView backgroundView;
    private boolean isDisplayed = false;
    private int marginLeft = 200;
    private int bubbleY = 0;
    ContextMenuContext menuContext;
    // Room data
    private static final String ROOMS_JSON = GetJson.get();
    List<Room> rooms; 
    /**
     * Interface for room selection events
     */
    public interface OnRoomSelectedListener {
        void onRoomSelected(String roomName, int roomId);
    }

    /**
     * Model class to represent a room from JSON
     */
    private static class Room {
        private final String name;
        private final int id;
        private final String imageUrl;

        public Room(String name, int id, String imageUrl) {
            this.name = name;
            this.id = id;
            this.imageUrl = imageUrl;
            
        }
    }
    
    /**
     * Background view that draws a rounded rectangle behind the thumbnails
     */
    private class BackgroundView extends View {
        private final Paint paint;
        private final RectF rect;
        
        public BackgroundView(Context context) {
            super(context);
            
            paint = new Paint();
            paint.setColor(BACKGROUND_COLOR);
            paint.setAntiAlias(true);
            
            rect = new RectF();
        }
        
        public void setBackgroundBounds(int left, int top, int right, int bottom) {
            rect.set(
                left - BACKGROUND_PADDING, 
                top - BACKGROUND_PADDING, 
                right + BACKGROUND_PADDING, 
                bottom + BACKGROUND_PADDING
            );
            invalidate();
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRoundRect(rect, BACKGROUND_CORNER_RADIUS, BACKGROUND_CORNER_RADIUS, paint);
        }
    }

    /**
     * Constructor for RoomSelectionComponent
     * @param context Android context
     * @param container ViewGroup to add thumbnails to
     */
public RoomSelectionComponent(Context context, ViewGroup container, ContextMenuContext menuContext) {
    this.context = context;
    this.container = container;
    this.screenWidth = getScreenWidth();
    this.menuContext = menuContext;
    this.menuContext.onBubbleResize(()->{
        this.onBubbleResize();
    });
    // Création de la BackgroundView
    this.backgroundView = new BackgroundView(context);

    // LayoutParams en match_parent pour l’affichage complet du fond
    ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    );
    // Ajout au container en index 0 pour être derrière les miniatures
    container.addView(backgroundView, 0, lp);

    initializeRooms();
}

    /**
     * Parse room data from JSON and create thumbnails
     */
    private void initializeRooms() {
        // Clean up any existing thumbnails
        clearThumbnails();
        
        // Get rooms and display them
        this.rooms = parseRoomsFromJson();
    }
    
    /**
     * Clean up existing thumbnails
     */
    private void clearThumbnails() {
        for (RoomThumbnail thumbnail : thumbnails) {
            thumbnail.destroy();
        }
        thumbnails.clear();
    }

    /**
     * Parse room data from JSON
     * @return List of Room objects
     */
    private List<Room> parseRoomsFromJson() {
        List<Room> rooms = new ArrayList<>();
        
        try {
            JSONObject jsonObject = new JSONObject(ROOMS_JSON);
            JSONArray jsonRooms = jsonObject.getJSONArray("rooms");
            
            for (int i = 0; i < jsonRooms.length(); i++) {
                JSONObject room = jsonRooms.getJSONObject(i);
                String name = room.getString("name");
                int id = room.getInt("id");
                String imageUrl = room.getString("img");
                
                rooms.add(new Room(name, id, imageUrl));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return rooms;
    }

    /**
     * Create and display thumbnails in a grid layout
     * @param rooms List of Room objects
     */
private void displayRoomThumbnails(List<Room> rooms) {
    // Calculate layout parameters
    int thumbnailSize = thumbnailRadius * 2;
    int horizontalSpacing = DEFAULT_SPACING;
    int verticalSpacing = DEFAULT_SPACING;
    
    // Calculate available width considering marginLeft
    int availableWidth = screenWidth - marginLeft; // Subtract marginLeft from screenWidth
    
    // Calculate how many thumbnails can fit in a row
    int thumbnailsPerRow = Math.max(1, (availableWidth + horizontalSpacing) / 
                                    (thumbnailSize + horizontalSpacing));
    
    // Calculate horizontal offset to center the grid within the available width after marginLeft
    int totalRowWidth = (thumbnailsPerRow * thumbnailSize) + 
                        ((thumbnailsPerRow - 1) * horizontalSpacing);
    int startX = marginLeft + (availableWidth - totalRowWidth) / 2; // Apply marginLeft and center
    
    // Variables to track the bounds of all thumbnails (for the background)
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    
    // Create thumbnails
    for (int i = 0; i < rooms.size(); i++) {
        final Room room = rooms.get(i);
        
        // Calculate grid position
        int row = i / thumbnailsPerRow;
        int col = i % thumbnailsPerRow;
        
        // Calculate x and y positions with proper spacing
        int x = startX + col * (thumbnailSize + horizontalSpacing);
        int y = verticalSpacing + row * (thumbnailSize + verticalSpacing);
        
        // Update bounds for background
        minX = Math.min(minX, x - thumbnailRadius);
        minY = Math.min(minY, y - thumbnailRadius);
        maxX = Math.max(maxX, x + thumbnailRadius);
        maxY = Math.max(maxY, y + thumbnailRadius);
        
        // Create thumbnail
        RoomThumbnail thumbnail = new RoomThumbnail(
            context,
            x,
            y,
            room.name,
            thumbnailRadius/2,
            room.imageUrl,
            container
        );
        
        // Set click listener
        thumbnail.setOnRoomSelectedListener(roomName -> {
            if (roomSelectedListener != null) {
                roomSelectedListener.onRoomSelected(roomName, room.id);
            }
        });
        
        thumbnails.add(thumbnail);
    }
    
    // Adjust background bounds for single thumbnail case
    if (rooms.size() <= 1) {
        int expansion = thumbnailSize;
        minX -= expansion;
        minY -= expansion;
        maxX += expansion;
        maxY += expansion;
    }
    
    backgroundView.setBackgroundBounds(minX, minY, maxX, maxY);
}
     

    /**
     * Get the screen width in pixels
     * @return Width of the screen in pixels
     */
    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

    /**
     * Set listener for room selection events
     * 
     * @param listener The listener to call when a room is selected
     */
    public void setOnRoomSelectedListener(OnRoomSelectedListener listener) {
        this.roomSelectedListener = listener;
    }

    /**
     * Update the size of all thumbnails
     * @param diameter New diameter for the thumbnails (radius will be diameter/2)
     */
    public void setThumbnailSize(int diameter) {
        if (diameter <= 0) {
            throw new IllegalArgumentException("Diameter must be positive");
        }
        
        this.thumbnailRadius = diameter / 2;
        
        // Redraw with new size
        initializeRooms();
    }
    
    /**
     * Clean up resources
     */
    public void destroy() {
        clearThumbnails();
        if (backgroundView != null && backgroundView.getParent() != null) {
            ((ViewGroup) backgroundView.getParent()).removeView(backgroundView);
        }
    }
public void display() {
    if (!isDisplayed) {
        if (backgroundView.getParent() == null) {
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            );
            container.addView(backgroundView, 0, lp);
        }
        
        // Ajoute les miniatures si elles ne sont pas là
        if (thumbnails.isEmpty()) {
            displayRoomThumbnails(rooms);
        } else {
            for (RoomThumbnail thumbnail : thumbnails) {
                if (thumbnail.getParent() == null) {
                    container.addView(thumbnail);
                }
            }
        }
        
        isDisplayed = true;
    }
}

public void undisplay() {
    if (isDisplayed) {
        // Supprime les miniatures
        for (RoomThumbnail thumbnail : thumbnails) {
            View v = thumbnail;
            if (v.getParent() != null) {
                ((ViewGroup) v.getParent()).removeView(v);
            }
        }
        
        // Supprime le backgroundView
        if (backgroundView.getParent() != null) {
            ((ViewGroup) backgroundView.getParent()).removeView(backgroundView);
        }
        
        isDisplayed = false;
    }
}
    public void onBubbleResize(){
        thumbnailRadius = (int)(menuContext.bubbleSize/4);
        marginLeft = 3 * menuContext.bubbleSize;
    }
}
