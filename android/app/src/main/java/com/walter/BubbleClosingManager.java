package com.walter;

/**
 * BubbleClosingManager
 * 
 * A helper class to integrate the BubbleClosingZone with the FloatingBubbleService.
 * This class manages the closing zone visibility and interactions based on bubble drag events.
 */
public class BubbleClosingManager {
    private BubbleClosingZone closingZone;
    private boolean isDragging = false;
    
    /**
     * Constructor
     * 
     * @param context The service or application context
     * @param closeListener Listener for close events
     */
    public BubbleClosingManager(android.content.Context context, BubbleClosingZone.BubbleCloseListener closeListener) {
        // Initialize the closing zone
        closingZone = new BubbleClosingZone(context);
        closingZone.setCloseListener(closeListener);
    }
    
    /**
     * Called when bubble drag starts
     */
    public void onDragStart() {
        isDragging = true;
        closingZone.show();
    }
    
    /**
     * Called during bubble drag to update position
     * 
     * @param bubbleX X coordinate of bubble
     * @param bubbleY Y coordinate of bubble
     * @param bubbleSize Size of the bubble
     * @return true if bubble is in closing zone, false otherwise
     */
    public boolean onDragMove(int bubbleX, int bubbleY, int bubbleSize) {
        if (isDragging) {
            return closingZone.updateBubblePosition(bubbleX, bubbleY, bubbleSize);
        }
        return false;
    }
    
    /**
     * Called when bubble drag ends
     * 
     * @param bubbleX Final X coordinate of bubble
     * @param bubbleY Final Y coordinate of bubble
     * @param bubbleSize Size of the bubble
     * @return true if bubble was released in closing zone, false otherwise
     */
    public boolean onDragEnd(int bubbleX, int bubbleY, int bubbleSize) {
        boolean wasInClosingZone = false;
        
        if (isDragging) {
            wasInClosingZone = closingZone.updateBubblePosition(bubbleX, bubbleY, bubbleSize);
            
            if (wasInClosingZone) {
                closingZone.onBubbleReleased();
            } else {
                closingZone.hide();
            }
            
            isDragging = false;
        }
        
        return wasInClosingZone;
    }
    
    /**
     * Checks if drag is in progress
     * 
     * @return true if dragging, false otherwise
     */
    public boolean isDragging() {
        return isDragging;
    }
    
    /**
     * Cleans up resources
     */
    public void cleanup() {
        if (closingZone != null) {
            closingZone.cleanup();
        }
    }
}