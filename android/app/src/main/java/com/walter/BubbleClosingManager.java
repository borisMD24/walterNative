package com.walter;

import android.content.Context;
import android.util.Log;

/**
 * BubbleClosingManager
 *
 * Manages the interaction between a draggable floating bubble and a closing zone.
 * It handles visibility and behavior of the closing zone during drag events.
 */
public class BubbleClosingManager {
    private static final String TAG = "BubbleClosingManager";
    private final BubbleClosingZone closingZone;
    private boolean isDragging = false;

    /**
     * Constructs a BubbleClosingManager instance.
     *
     * @param context       the application or service context
     * @param closeListener listener triggered when the bubble is released in the closing zone
     */
    public BubbleClosingManager(Context context, BubbleClosingZone.BubbleCloseListener closeListener) {
        this.closingZone = new BubbleClosingZone(context);
        this.closingZone.setCloseListener(closeListener);
    }

    /**
     * Should be called when the bubble starts being dragged.
     * Displays the closing zone.
     */
    public void onDragStart() {
        isDragging = true;
        closingZone.show();
        Log.d(TAG, "Drag started - showing closing zone");
    }

    /**
     * Should be called during the bubble drag to update its position.
     *
     * @param bubbleX    the X coordinate of the bubble
     * @param bubbleY    the Y coordinate of the bubble
     * @param bubbleSize the size (diameter) of the bubble
     * @return true if the bubble is over the closing zone, false otherwise
     */
    public boolean onDragMove(int bubbleX, int bubbleY, int bubbleSize) {
        return isDragging && closingZone.updateBubblePosition(bubbleX, bubbleY, bubbleSize);
    }

    /**
     * Should be called when the drag ends to determine whether the bubble should be closed.
     *
     * @param bubbleX    the final X coordinate
     * @param bubbleY    the final Y coordinate
     * @param bubbleSize the size of the bubble
     * @return true if the bubble was dropped inside the closing zone
     */
    public boolean onDragEnd(int bubbleX, int bubbleY, int bubbleSize) {
        boolean isInClosingZone = false;

        if (isDragging) {
            isInClosingZone = closingZone.updateBubblePosition(bubbleX, bubbleY, bubbleSize);
            Log.d(TAG, "Drag ended - bubble in closing zone: " + isInClosingZone);

            if (isInClosingZone) {
                // Let onBubbleReleased handle the hide after animation
                closingZone.onBubbleReleased();
            } else {
                // If not in closing zone, hide the zone
                closingZone.hide(true);
                Log.d(TAG, "Bubble not in zone - hiding closing zone");
            }

            isDragging = false;
        }
        
        return isInClosingZone;
    }

    /**
     * Hides the closing zone manually.
     */
    public void hide() {
        Log.d(TAG, "Manually hiding closing zone");
        closingZone.hide(true);
    }

    /**
     * Indicates whether a drag is currently in progress.
     *
     * @return true if dragging, false otherwise
     */
    public boolean isDragging() {
        return isDragging;
    }

    /**
     * Frees any resources held by the closing zone.
     */
    public void cleanup() {
        if (closingZone != null) {
            Log.d(TAG, "Cleaning up closing zone resources");
            closingZone.cleanup();
        }
    }
}