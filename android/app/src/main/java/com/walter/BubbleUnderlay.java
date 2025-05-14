package com.walter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.util.Log;

/**
 * BubbleUnderlay
 * 
 * A class that manages the semi-transparent underlay that appears when the floating bubble
 * is clicked. It serves as a backdrop for the context menu and handles touch events
 * outside the bubble area.
 */
public class BubbleUnderlay {
    private static final String TAG = "BubbleUnderlay";
    
    private final Context context;
    private final WindowManager windowManager;
    private View underlayView;
    private BubbleUnderlayListener listener;
    
    /**
     * Interface for listening to underlay events
     */
    public interface BubbleUnderlayListener {
        void onUnderlayTouched();
    }
    
    /**
     * Constructor
     * 
     * @param context The application context
     */
    public BubbleUnderlay(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }
    
    /**
     * Sets a listener for underlay touch events
     * 
     * @param listener The listener to set
     */
    public void setListener(BubbleUnderlayListener listener) {
        this.listener = listener;
    }
    
    /**
     * Shows the underlay on screen
     * 
     * @return true if the underlay was shown successfully
     */
    public boolean show() {
        try {
            // If underlay is already shown, return
            if (underlayView != null) {
                return false;
            }
            
            // Create a full-screen semi-transparent view
            underlayView = new View(context);
            underlayView.setBackgroundColor(Color.argb(50, 0, 0, 0)); // Semi-transparent black
            
            // Configure layout parameters for the underlay
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | 
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;

            // Add touch listener to detect taps outside the bubble
            underlayView.setOnTouchListener((v, event) -> {
                // Notify listener of underlay touch
                if (listener != null) {
                    listener.onUnderlayTouched();
                }
                return true; // Consume the event
            });

            // Add the underlay view
            windowManager.addView(underlayView, params);
            
            Log.d(TAG, "Underlay view shown");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error showing underlay: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Hides the underlay from screen
     * 
     * @return true if the underlay was hidden successfully
     */
    public boolean hide() {
        try {
            if (underlayView != null && underlayView.isAttachedToWindow()) {
                windowManager.removeView(underlayView);
                underlayView = null;
                Log.d(TAG, "Underlay view hidden");
                return true;
            }
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Error hiding underlay: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Checks if the underlay is currently displayed
     * 
     * @return true if the underlay is displayed
     */
    public boolean isShowing() {
        return underlayView != null && underlayView.isAttachedToWindow();
    }
    
    /**
     * Updates the color of the underlay
     * 
     * @param color ARGB color value
     */
    public void setColor(int color) {
        if (underlayView != null) {
            underlayView.setBackgroundColor(color);
        }
    }
    
    /**
     * Sets the touch interceptor flag on the underlay
     * 
     * @param interceptTouches true to intercept all touches, false to allow touches through
     */
    public void setInterceptTouches(boolean interceptTouches) {
        if (underlayView != null && underlayView.isAttachedToWindow()) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) underlayView.getLayoutParams();
            
            if (interceptTouches) {
                params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            } else {
                params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            }
            
            windowManager.updateViewLayout(underlayView, params);
        }
    }
    
    /**
     * Release resources and clean up
     */
    public void release() {
        hide();
        listener = null;
    }
}