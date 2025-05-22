package com.walter;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Path;

public class RoomSelectionComponent {
    private static final int DEFAULT_THUMBNAIL_SIZE = 100;
    private static final int DEFAULT_SPACING = 20;
    private final Context context;
    private final ViewGroup container;
    private final List<RoomThumbnail> thumbnails = new ArrayList<>();
    private int thumbnailRadius = DEFAULT_THUMBNAIL_SIZE / 2;
    private FlexboxLayout thumbnailContainer;
    private boolean isDisplayed = false;
    private ContextMenuContext menuContext;
    private int maxHeight;
    private int maxWidth;
    
    // ClipPathManager integration
    private ClipPathManager clipPathManager;

    public RoomSelectionComponent(Context context, ViewGroup container, ContextMenuContext menuContext) {
        this.context = context;
        this.container = container;
        this.thumbnailContainer = new FlexboxLayout(context);
        this.menuContext = menuContext;
        this.maxHeight = menuContext.screenHeight/3;
        this.maxWidth = menuContext.screenWidth - 2 * menuContext.bubbleSize;
        
        // Initialize ClipPathManager
        this.clipPathManager = new ClipPathManager(thumbnailContainer);
        
        setupLayout();
        this.updateThumbnailContainerDimensions(maxWidth, maxHeight);
        
        // Apply default rounded rectangle clipping
        applyDefaultClipping();
        applyCircularClip();
    }

    private void setupLayout() {
        thumbnailContainer.setFlexDirection(FlexDirection.ROW);
        thumbnailContainer.setFlexWrap(FlexWrap.WRAP);
        thumbnailContainer.setJustifyContent(JustifyContent.CENTER);
        thumbnailContainer.setBackgroundColor(Color.WHITE);
        
        // Note: We'll handle corner radius through ClipPathManager instead of GradientDrawable
        // to have more flexibility and consistency with clipping operations
    }
    
    /**
     * Applies default clipping (rounded rectangle) to the thumbnails container
     */
    private void applyDefaultClipping() {
        float cornerRadius = menuContext.bubbleSize / 8f;
        clipPathManager.applyRoundedRectangle(cornerRadius);
    }

    public void display() {
        if (!isDisplayed) {
            if (thumbnailContainer.getParent() == null) {
                container.addView(thumbnailContainer, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            }
            this.updateThumbnailContainerDimensions(maxWidth, maxHeight);

            clearThumbnails();
            List<Room> rooms = parseRoomsFromJson();
            addThumbnails(rooms);
            isDisplayed = true;
            
            // Update clipping after display to ensure proper dimensions
            clipPathManager.updateClipping();
        }
    }

    public void undisplay() {
        if (isDisplayed) {
            clearThumbnails();
            container.removeView(thumbnailContainer);
            isDisplayed = false;
        }
    }

    private List<Room> parseRoomsFromJson() {
        List<Room> rooms = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(GetJson.get());
            JSONArray jsonRooms = jsonObject.getJSONArray("rooms");
            for (int i = 0; i < jsonRooms.length(); i++) {
                JSONObject room = jsonRooms.getJSONObject(i);
                rooms.add(new Room(
                        room.getString("name"),
                        room.getInt("id"),
                        room.getString("img")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    private void addThumbnails(List<Room> rooms) {
        int thumbnailSize = thumbnailRadius * 2;
        int spacing = DEFAULT_SPACING;
        for (Room room : rooms) {
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(thumbnailSize, thumbnailSize);
            params.setMargins(spacing / 2, spacing / 2, spacing / 2, spacing / 2);
            RoomThumbnail thumbnail = new RoomThumbnail(
                    context, room.id, thumbnailRadius / 2, room.imageUrl, thumbnailContainer, params);
            thumbnail.name = room.name;
            thumbnail.updateNameLabel();
            thumbnails.add(thumbnail);
        }
    }

    private void clearThumbnails() {
        for (RoomThumbnail thumbnail : thumbnails) {
            thumbnail.destroy();
        }
        thumbnails.clear();
        thumbnailContainer.removeAllViews();
    }

    private static class Room {
        String name;
        int id;
        String imageUrl;

        Room(String name, int id, String imageUrl) {
            this.name = name;
            this.id = id;
            this.imageUrl = imageUrl;
        }
    }
    
    /**
     * Updates the dimensions of the thumbnail container.
     * 
     * @param width The new width of the container (in pixels or MATCH_PARENT/WRAP_CONTENT)
     * @param height The new height of the container (in pixels or MATCH_PARENT/WRAP_CONTENT)
     */
    public void updateThumbnailContainerDimensions(int width, int height) {
        ViewGroup.LayoutParams layoutParams = thumbnailContainer.getLayoutParams();
        
        if (layoutParams != null) {
            layoutParams.width = width;
            layoutParams.height = height;
            thumbnailContainer.setLayoutParams(layoutParams);
            
            // Update max height if needed
            if (height != ViewGroup.LayoutParams.MATCH_PARENT && 
                height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                this.maxHeight = height;
            }
        } else {
            // Create new layout params if none exist
            ViewGroup.LayoutParams newParams = new ViewGroup.LayoutParams(width, height);
            thumbnailContainer.setLayoutParams(newParams);
            
            if (height != ViewGroup.LayoutParams.MATCH_PARENT && 
                height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                this.maxHeight = height;
            }
        }
        
        // Request layout update and update clipping
        thumbnailContainer.requestLayout();
        
        // Update clipping after dimensions change
        thumbnailContainer.post(new Runnable() {
            @Override
            public void run() {
                clipPathManager.updateClipping();
            }
        });
    }

    /**
     * Updates the coordinates (margins) of the thumbnail container.
     * 
     * @param left Left margin in pixels
     * @param top Top margin in pixels
     * @param right Right margin in pixels
     * @param bottom Bottom margin in pixels
     */
    public void updateThumbnailContainerMargins(int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams currentParams = thumbnailContainer.getLayoutParams();
        
        if (currentParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) currentParams;
            params.setMargins(left, top, right, bottom);
            thumbnailContainer.setLayoutParams(params);
        } else {
            // Create new margin layout params
            FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(
                    currentParams.width, 
                    currentParams.height);
            newParams.setMargins(left, top, right, bottom);
            thumbnailContainer.setLayoutParams(newParams);
        }
        
        // Request layout update
        thumbnailContainer.requestLayout();
    }

    /**
     * Updates the position of the thumbnail container using X and Y coordinates.
     * Note: This will only work if the parent container uses absolute positioning.
     * 
     * @param x X coordinate in pixels
     * @param y Y coordinate in pixels
     */
    public void updateThumbnailContainerPosition(int x, int y) {
        thumbnailContainer.setX(x);
        thumbnailContainer.setY(y);
    }

    /**
     * Updates both dimensions and position of the thumbnail container.
     * 
     * @param x X coordinate in pixels
     * @param y Y coordinate in pixels
     * @param width The new width in pixels
     * @param height The new height in pixels
     */
    public void updateThumbnailContainerBounds(int x, int y, int width, int height) {
        // Update dimensions
        updateThumbnailContainerDimensions(width, height);
        
        // Update position
        updateThumbnailContainerPosition(x, y);
    }

    /**
     * Dynamically adjusts the height of the thumbnailContainer based on content
     * without exceeding the maxHeight value.
     */
    public void adjustThumbnailContainerHeight() {
        int contentHeight = calculateContentHeight();
        int newHeight = Math.min(contentHeight, maxHeight);
        
        ViewGroup.LayoutParams layoutParams = thumbnailContainer.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = newHeight;
            thumbnailContainer.setLayoutParams(layoutParams);
        }
        
        thumbnailContainer.requestLayout();
        
        // Update clipping after height adjustment
        thumbnailContainer.post(new Runnable() {
            @Override
            public void run() {
                clipPathManager.updateClipping();
            }
        });
    }

    /**
     * Calculates the total height needed to display all thumbnails.
     * 
     * @return Total height needed in pixels
     */
    private int calculateContentHeight() {
        // This is a simplified calculation and may need to be adjusted
        // based on your specific layout requirements
        int thumbnailSize = thumbnailRadius * 2;
        int spacing = DEFAULT_SPACING;
        int itemsPerRow = Math.max(1, thumbnailContainer.getWidth() / (thumbnailSize + spacing));
        int rowCount = (int) Math.ceil((double) thumbnails.size() / itemsPerRow);
        
        return rowCount * (thumbnailSize + spacing);
    }

    /**
     * Updates the maximum height for the thumbnail container.
     * 
     * @param maxHeight The new maximum height in pixels
     */
    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        // If currently displayed, adjust the height accordingly
        if (isDisplayed) {
            adjustThumbnailContainerHeight();
        }
    }
    
    private int computeMoveXOffset(){
        return(int)(
            - menuContext.normalizedBubbleX * maxWidth
            - (menuContext.normalizedBubbleX * 2 - 1) * menuContext.bubbleSize
        );
    }
    
    private int computeMoveYOffset(){
        return(int)(
            - menuContext.bubbleSize / 2
        );
    }
    
    public void onIconMove(int x, int y){
        updateThumbnailContainerPosition(
            x + computeMoveXOffset(),
            y + computeMoveYOffset()
         );
    }
    
    // ClipPathManager integration methods
    
    /**
     * Applies a rounded rectangle clip to the thumbnails container
     * @param cornerRadius Corner radius in pixels
     */
    public void applyRoundedRectangleClip(float cornerRadius) {
        clipPathManager.applyRoundedRectangle(cornerRadius);
    }
    
    /**
     * Applies a circular clip to the thumbnails container
     */
    public void applyCircularClip() {
        clipPathManager.applyCircle();
    }
    
    /**
     * Applies an oval clip to the thumbnails container
     */
    public void applyOvalClip() {
        clipPathManager.applyOval();
    }
    
    /**
     * Applies rounded corners only to the top of the thumbnails container
     * @param cornerRadius Corner radius in pixels
     */
    public void applyRoundedTopCornersClip(float cornerRadius) {
        clipPathManager.applyRoundedTopCorners(cornerRadius);
    }
    
    /**
     * Applies rounded corners only to the bottom of the thumbnails container
     * @param cornerRadius Corner radius in pixels
     */
    public void applyRoundedBottomCornersClip(float cornerRadius) {
        clipPathManager.applyRoundedBottomCorners(cornerRadius);
    }
    
    /**
     * Applies a hexagonal clip to the thumbnails container
     */
    public void applyHexagonClip() {
        clipPathManager.applyHexagon();
    }
    
    /**
     * Applies a triangular clip to the thumbnails container
     */
    public void applyTriangleClip() {
        clipPathManager.applyTriangle();
    }
    
    /**
     * Applies a diamond-shaped clip to the thumbnails container
     */
    public void applyDiamondClip() {
        clipPathManager.applyDiamond();
    }
    
    /**
     * Applies a custom path clip to the thumbnails container
     * @param path Custom Path object defining the clip shape
     */
    public void applyCustomPathClip(Path path) {
        clipPathManager.applyCustomPath(path);
    }
    
    /**
     * Removes all clipping from the thumbnails container
     */
    public void removeClipping() {
        clipPathManager.removeClipping();
    }
    
    /**
     * Gets the current clip type applied to the thumbnails container
     * @return Current ClipType
     */
    public ClipPathManager.ClipType getCurrentClipType() {
        return clipPathManager.getCurrentClipType();
    }
    
    /**
     * Gets the ClipPathManager instance for advanced operations
     * @return ClipPathManager instance
     */
    public ClipPathManager getClipPathManager() {
        return clipPathManager;
    }
}