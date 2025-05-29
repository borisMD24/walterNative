package com.walter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.util.Log;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;

import java.util.function.Consumer;

import com.walter.ContextMenuView;
import android.view.ViewParent;

/**
 * UnderlayView
 * 
 * A specialized view that creates a semi-transparent overlay behind the bubble.
 * This serves as an interaction layer for displaying context menus and capturing touch events
 * outside of the bubble to dismiss any active menus.
 * Starts click-transparent by default and can be made clickable via setClickable().
 */
public class UnderlayView {
    private static final String TAG = "UnderlayView";
    private static final int DEFAULT_BACKGROUND_COLOR = Color.argb(20, 0, 0, 0); // Very light black background
    private static final int CLICKABLE_BACKGROUND_COLOR = Color.argb(70, 255, 0, 0); // Semi-transparent red
    
    private final Context context;
    private final WindowManager windowManager;
    private FrameLayout rootLayout;
    
    private WindowManager.LayoutParams params;
    private OnUnderlayTouchListener touchListener;
    private int x;
    private int y;
    private static final int CIRCLE_RADIUS = 500;  // Circle radius in pixels
    private static final int CIRCLE_COLOR = Color.YELLOW;
    public boolean isVisible = false;
    private ContextMenuView contextMenu;
    public int bubbleSize;
    public boolean isFirstShow = true;
    public boolean isClickable = false;
    private Runnable onCloseCallback = null;
    private Consumer<Boolean> setClicked = (clicked) -> {
        Log.d("FloatingBubble", "Clicked = " + clicked);
    };
    
    /**
     * Interface for touch events on the underlay
     */
    public interface OnUnderlayTouchListener {
        void onUnderlayTouch();
    }
    
    public void onClose(Runnable cb){
        onCloseCallback = cb;
    }

    public void setSetClicked(Consumer<Boolean> setClicked) {
        this.setClicked = setClicked;
    }
    
    /**
     * Constructor initializes the underlay view system
     * Makes it visible but click-transparent by default
     * 
     * @param context The application context
     */
    public UnderlayView(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.x = 0;
        this.y = 0;
        
        // Show the view immediately on instantiation
        showClickTransparent();
        
        this.contextMenu = new ContextMenuView(context, this.rootLayout);
    }
    public void setExpandedBubbleSize(int size){
        this.contextMenu.menuContext.setExpandedBubbleSize(size);
    }
    /**
     * Shows the underlay with default settings, making it click-transparent
     */
    public void showClickTransparent() {
        showClickTransparent(DEFAULT_BACKGROUND_COLOR);
    }
    
    /**
     * Shows the underlay with a specific background color, making it click-transparent
     * 
     * @param backgroundColor The ARGB color for the background
     */
    public void showClickTransparent(int backgroundColor) {
        if (rootLayout != null) {
            // Already showing, update clickability
            updateClickability();
            return;
        }
        
        try {
            // Create a full-screen root layout
            rootLayout = new FrameLayout(context);
            rootLayout.setBackgroundColor(backgroundColor);
            // Add the text view to the center of the layout
            FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            );
            textParams.gravity = Gravity.CENTER;
            
            // Configure window layout parameters - initially click-transparent
            params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |  // Make it click-transparent
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            
            // Set touch listener with properly consuming events
            rootLayout.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN && isClickable) {
                    Log.d(TAG, "Touch event detected on underlay");
                    if (touchListener != null) {
                        touchListener.onUnderlayTouch();
                    }
                    if (setClicked != null) {
                        setClicked.accept(false);
                        // Make it unclickable after touch
                        // Use post to avoid modifying during event handling
                        rootLayout.post(() -> makeUnclickable());
                    }
                    return true; // Consume the event
                }
                return isClickable; // Only consume if clickable
            });
            
            // Add the view to window
            windowManager.addView(rootLayout, params);
            isVisible = true;
            Log.d(TAG, "Underlay view displayed (click-transparent)");
            this.contextMenu = new ContextMenuView(this.context, rootLayout);

        } catch (Exception e) {
            Log.e(TAG, "Error showing underlay view: " + e.getMessage(), e);
        }
    }
    
    /**
     * Shows the underlay with default settings and makes it clickable
     */
    public void show() {
        // First make sure view exists
        if (rootLayout == null) {
            showClickTransparent();
        }
        // Then make it clickable
        makeClickable();
    }
    
    /**
     * Shows the underlay with a specific background color and makes it clickable
     * 
     * @param backgroundColor The ARGB color for the background
     */
    public void show(int backgroundColor) {
        // First make sure view exists with right color
        if (rootLayout == null) {
            showClickTransparent(backgroundColor);
        } else {
            rootLayout.setBackgroundColor(backgroundColor);
        }
        // Then make it clickable
        makeClickable();
    }
    
    /**
     * Sets whether the underlay should be clickable or click-transparent
     * 
     * @param clickable true to make the underlay respond to clicks, false to make it click-transparent
     */
    public void setClickable(boolean clickable) {
        // Only proceed if there's a change in state or we need to force update
        if (this.isClickable == clickable && rootLayout != null && rootLayout.isAttachedToWindow()) {
            Log.d(TAG, "Skipping clickable update - no change needed. Current: " + this.isClickable);
            return;
        }
        
        Log.d(TAG, "Setting clickable from " + this.isClickable + " to " + clickable);
        this.isClickable = clickable;
        
        // Change background color based on clickability
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
            rootLayout.setClickable(clickable);
            rootLayout.setFocusable(clickable);
        }
        
        updateClickability();
    }
    
    public void makeClickable() {
        Log.d(TAG, "Making underlay clickable");
        setClickable(true);
        contextMenu.show();
    }
    
    public void makeUnclickable() {
        setClickable(false);
        onCloseCallback.run();
        contextMenu.hide();
    }

    /**
     * Updates the window parameters to match the current clickability state
     */
    private void updateClickability() {
        if (rootLayout == null || params == null) {
            Log.e(TAG, "Cannot update clickability: rootLayout or params is null");
            return;
        }
        
        try {
            // Only update flags if attached to window
            if (rootLayout.isAttachedToWindow()) {
                // Toggle the NOT_TOUCHABLE flag based on isClickable
                if (isClickable) {
                    // Remove FLAG_NOT_TOUCHABLE to make it clickable
                    params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    Log.d(TAG, "Removing FLAG_NOT_TOUCHABLE flag to make underlay clickable");
                } else {
                    // Add FLAG_NOT_TOUCHABLE to make it click-transparent
                    params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                    Log.d(TAG, "Adding FLAG_NOT_TOUCHABLE flag to make underlay click-transparent");
                }
                
                // Update the view
                windowManager.updateViewLayout(rootLayout, params);
                Log.d(TAG, "Updated window layout parameters, clickable: " + isClickable);
            } else {
                Log.d(TAG, "View not attached to window yet, flags will be applied when attached");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating clickability: " + e.getMessage(), e);
        }
    }
    
    public void setCoords(int x, int y) {
        this.x = x;
        this.y = y;
        
        // When first shown, use showAtPosition for animation
        // For subsequent updates, use regular setCoords
        contextMenu.setCoords(x, y);
    }
    
    
    ViewParent getParent() {
        return this.rootLayout != null ? this.rootLayout.getParent() : null;
    }
    
    public boolean hide() {
        try {
            if (this.contextMenu != null) {
                this.contextMenu.hide();
            }
            
            if (rootLayout != null) {
                if (rootLayout.isAttachedToWindow()) {
                    windowManager.removeView(rootLayout);
                    Log.d(TAG, "Underlay view removed from window");
                }
                rootLayout = null;
                Log.d(TAG, "Underlay view hidden");
                isVisible = false;
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding underlay view: " + e.getMessage(), e);
        }
        isVisible = false;
        return false;
    }
    
    /**
     * Sets the listener for touch events on the underlay
     * 
     * @param listener The callback to invoke when underlay is touched
     */
    public void setOnUnderlayTouchListener(OnUnderlayTouchListener listener) {
        this.touchListener = listener;
        Log.d(TAG, "Touch listener set: " + (listener != null ? "yes" : "no"));
    }
    
    /**
     * Bring the underlay to front
     */
    public void bringToFront() {
        try {
            if (rootLayout != null && rootLayout.isAttachedToWindow()) {
                windowManager.removeView(rootLayout);
                windowManager.addView(rootLayout, params);
                Log.d(TAG, "Brought underlay to front");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error bringing underlay to front: " + e.getMessage(), e);
        }
    }
    
    public void setBubbleSize(int size) {
        this.bubbleSize = size;
        if (this.contextMenu != null) {
            this.contextMenu.setBubbleSize(size);
        }
    }
    
    /**
     * Change the background color of the underlay
     * 
     * @param color The new ARGB color
     */
    public void setBackgroundColor(int color) {
        if (rootLayout != null) {
            rootLayout.setBackgroundColor(color);
        }
    }
    public void bubbleFixed(){
        this.contextMenu.bubbleFixed();
    }
}