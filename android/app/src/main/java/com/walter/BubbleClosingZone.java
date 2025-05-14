package com.walter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * BubbleClosingZone
 * 
 * A class that displays a zone at the bottom of the screen where users can drop
 * the floating bubble to close it. This zone appears when the bubble is being dragged
 * and provides visual feedback when the bubble enters the zone.
 */
public class BubbleClosingZone {
    private static final String TAG = "BubbleClosingZone";
    
    // UI elements
    private Context context;
    private WindowManager windowManager;
    private View closingZoneView;
    private WindowManager.LayoutParams params;
    private ImageView closeIcon;
    private TextView closeText;
    private LinearLayout closeContainer;
    
    // Animation properties
    private static final int ANIMATION_DURATION = 300;
    private static final int NORMAL_ALPHA = 180; // Semi-transparent
    private static final int ACTIVE_ALPHA = 255; // Fully opaque
    
    // Zone metrics
    private int zoneHeight;
    private boolean isVisible = false;
    private boolean isBubbleInZone = false;
    
    // Callback for closing
    private BubbleCloseListener closeListener;
    
    /**
     * Interface for notifying when the bubble should be closed
     */
    public interface BubbleCloseListener {
        void onBubbleClose();
    }
    
    /**
     * Constructor
     * 
     * @param context The application context
     */
    public BubbleClosingZone(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        
        // Calculate zone height based on screen density
        float density = context.getResources().getDisplayMetrics().density;
        zoneHeight = (int) (72 * density); // 72dp height
        
        initializeClosingZone();
    }
    
    /**
     * Sets the listener for bubble close events
     * 
     * @param listener The listener to be notified
     */
    public void setCloseListener(BubbleCloseListener listener) {
        this.closeListener = listener;
    }
    
    /**
     * Initializes the closing zone view and its parameters
     */
    private void initializeClosingZone() {
        try {
            // Create the closing zone view from layout
            // Note: Create a layout file named closing_zone_layout.xml
            closingZoneView = LayoutInflater.from(context).inflate(R.layout.closing_zone_layout, null);
            closeIcon = closingZoneView.findViewById(R.id.close_icon);
            closeText = closingZoneView.findViewById(R.id.close_text);
            closeContainer = closingZoneView.findViewById(R.id.close_container);
            
            // Create background with rounded corners
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.RECTANGLE);
            background.setCornerRadii(new float[]{20, 20, 20, 20, 0, 0, 0, 0}); // Rounded top corners
            background.setColor(Color.argb(NORMAL_ALPHA, 255, 59, 48)); // Red color
            closeContainer.setBackground(background);
            
            // Configure the layout parameters for the closing zone
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    zoneHeight,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);
            
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.y = 0; // At the bottom of the screen
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing closing zone: " + e.getMessage(), e);
        }
    }
    
    /**
     * Shows the closing zone with animation
     */
    public void show() {
        if (!isVisible) {
            try {
                windowManager.addView(closingZoneView, params);
                isVisible = true;
                
                // Animate the view appearance
                Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setDuration(ANIMATION_DURATION);
                closingZoneView.startAnimation(fadeIn);
                
                Log.d(TAG, "Closing zone shown");
            } catch (Exception e) {
                Log.e(TAG, "Error showing closing zone: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * Hides the closing zone with animation
     */
public void hide(boolean forceImmediate) {
    if (isVisible) {
        try {
            if (forceImmediate) {
                // Skip animation and remove immediately
                if (closingZoneView != null && closingZoneView.isAttachedToWindow()) {
                    try {
                        windowManager.removeView(closingZoneView);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "View already removed: " + e.getMessage());
                    }
                }
                isVisible = false;
                isBubbleInZone = false;
                Log.d(TAG, "Closing zone force hidden");
            } else {
                // Use animation as before
                Animation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                fadeOut.setDuration(ANIMATION_DURATION);
                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {}
                    
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        try {
                            if (closingZoneView != null && closingZoneView.isAttachedToWindow()) {
                                windowManager.removeView(closingZoneView);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error removing view after animation: " + e.getMessage());
                        } finally {
                            // Always update flags even if exception occurs
                            isVisible = false;
                            isBubbleInZone = false;
                        }
                    }
                    
                    @Override
                    public void onAnimationRepeat(Animation animation) {}
                });
                
                closingZoneView.startAnimation(fadeOut);
                Log.d(TAG, "Closing zone hiding with animation");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding closing zone: " + e.getMessage(), e);
            
            // As a fallback, try to force remove the view and update state
            try {
                if (closingZoneView != null && closingZoneView.isAttachedToWindow()) {
                    windowManager.removeView(closingZoneView);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Final attempt to remove view failed: " + ex.getMessage());
            }
            
            isVisible = false;
            isBubbleInZone = false;
        }
    }
}
    /**
     * Updates the closing zone based on the bubble's position
     * 
     * @param bubbleX The X position of the bubble
     * @param bubbleY The Y position of the bubble
     * @param bubbleSize The size of the bubble
     * @return true if the bubble is in the closing zone, false otherwise
     */
    public boolean updateBubblePosition(int bubbleX, int bubbleY, int bubbleSize) {
        if (!isVisible) return false;
        
        try {
            // Get the screen height
            int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
            
            // Calculate bubble center y position
            int bubbleCenterY = bubbleY + (bubbleSize / 2);
            
            // Check if bubble is in closing zone (bottom area of screen)
            boolean isInZone = bubbleCenterY > (screenHeight - zoneHeight);
            
            // If zone status changed, update UI
            if (isInZone != isBubbleInZone) {
                isBubbleInZone = isInZone;
                updateClosingZoneAppearance(isInZone);
            }
            
            return isInZone;
        } catch (Exception e) {
            Log.e(TAG, "Error updating bubble position: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Updates the appearance of the closing zone based on whether the bubble is in it
     * 
     * @param isActive true if the bubble is in the zone, false otherwise
     */
    private void updateClosingZoneAppearance(boolean isActive) {
        try {
            // Get background drawable
            GradientDrawable background = (GradientDrawable) closeContainer.getBackground();
            
            if (isActive) {
                // Bubble entered zone - make more opaque and update text
                background.setColor(Color.argb(ACTIVE_ALPHA, 255, 59, 48));
                closeText.setText("Release to close");
                
                // Optional: add scale or pulse animation to icon
                closeIcon.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(ANIMATION_DURATION)
                        .start();
            } else {
                // Bubble left zone - restore normal appearance
                background.setColor(Color.argb(NORMAL_ALPHA, 255, 59, 48));
                closeText.setText("Drop here to close");
                
                closeIcon.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(ANIMATION_DURATION)
                        .start();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating closing zone appearance: " + e.getMessage(), e);
        }
    }
    
    /**
     * Called when bubble is released inside the closing zone
     */
    public void onBubbleReleased() {
    if (isBubbleInZone && closeListener != null) {
        // Display a success animation
        showSuccessAnimation();
        
        // Notify listener after a short delay to allow animation to play
        new Handler().postDelayed(() -> {
            // First notify the listener
            closeListener.onBubbleClose();
            
            // Then ensure the zone is hidden
            // Use force hide to make sure it's removed
            hide(true);
            
            // Add a log to debug
            Log.d(TAG, "Bubble released in zone - closing zone hidden");
        }, ANIMATION_DURATION);
    } else {
        // If for some reason we're not in the zone, hide anyway
        hide(false);
    }
}
    
    /**
     * Shows an animation when the bubble is successfully closed
     */
    private void showSuccessAnimation() {
        try {
            // Update text
            closeText.setText("Closing...");
            
            // Create a simple "success" animation
            closeIcon.animate()
                    .scaleX(0.0f)
                    .scaleY(0.0f)
                    .setDuration(ANIMATION_DURATION)
                    .start();
            
            // Pulse animation for the container
            closeContainer.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(ANIMATION_DURATION / 2)
                    .withEndAction(() -> {
                        closeContainer.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(ANIMATION_DURATION / 2)
                                .start();
                    })
                    .start();
        } catch (Exception e) {
            Log.e(TAG, "Error showing success animation: " + e.getMessage(), e);
        }
    }
    
    /**
     * Cleans up resources
     */
    public void cleanup() {
        if (isVisible && closingZoneView != null && closingZoneView.isAttachedToWindow()) {
            try {
                windowManager.removeView(closingZoneView);
                isVisible = false;
            } catch (Exception e) {
                Log.e(TAG, "Error cleaning up closing zone: " + e.getMessage(), e);
            }
        }
    }
}