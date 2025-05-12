package com.walter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.util.Log;
import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;

/**
 * UnderlayView
 * 
 * A specialized view that creates a semi-transparent overlay behind the bubble.
 * This serves as an interaction layer for displaying context menus and capturing touch events
 * outside of the bubble to dismiss any active menus.
 */
public class UnderlayView {
    private static final String TAG = "UnderlayView";
    private static final int DEFAULT_BACKGROUND_COLOR = Color.argb(20, 0, 0, 0); // Very light black background
    
    private final Context context;
    private final WindowManager windowManager;
    private FrameLayout rootLayout;
    private TextView coordinateIndicator;
    private CircleView circleView;
    private WindowManager.LayoutParams params;
    private OnUnderlayTouchListener touchListener;
    private int x;
    private int y;
    private static final int CIRCLE_RADIUS = 20;  // Circle radius in pixels
    private static final int CIRCLE_COLOR = Color.YELLOW;
    
    /**
     * Interface for touch events on the underlay
     */
    public interface OnUnderlayTouchListener {
        void onUnderlayTouch();
    }
    
    /**
     * Constructor initializes the underlay view system
     * 
     * @param context The application context
     */
    public UnderlayView(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.x = 0;
        this.y = 0;
    }
    
    /**
     * Shows the underlay with default settings
     */
    public void show() {
        show(DEFAULT_BACKGROUND_COLOR);
    }
    
    /**
     * Shows the underlay with a specific background color
     * 
     * @param backgroundColor The ARGB color for the background
     */
    public void show(int backgroundColor) {
        if (rootLayout != null) {
            // Already showing
            return;
        }
        
        try {
            // Create a full-screen root layout
            rootLayout = new FrameLayout(context);
            rootLayout.setBackgroundColor(backgroundColor);
            
            // Create coordinate indicator
            coordinateIndicator = new TextView(context);
            coordinateIndicator.setTextColor(Color.WHITE);
            coordinateIndicator.setBackgroundColor(Color.argb(150, 0, 0, 0));
            coordinateIndicator.setPadding(20, 10, 20, 10);
            coordinateIndicator.setText(String.format("X: %d, Y: %d", x, y));
            
            // Create circle view for coordinate indicator
            circleView = new CircleView(context);
            rootLayout.addView(circleView);
            
            // Add the text view to the center of the layout
            FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            );
            textParams.gravity = Gravity.CENTER;
            rootLayout.addView(coordinateIndicator, textParams);
            
            // Configure window layout parameters
            params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | 
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.START;
            
            // Set touch listener
            rootLayout.setOnTouchListener((v, event) -> {
                if (touchListener != null) {
                    touchListener.onUnderlayTouch();
                }
                return true; // Consume the event
            });
            
            // Add the view to window
            windowManager.addView(rootLayout, params);
            
            // Update the indicator with initial coordinates
            updateCoordinateIndicator();
            
            Log.d(TAG, "Underlay view displayed");
        } catch (Exception e) {
            Log.e(TAG, "Error showing underlay view: " + e.getMessage(), e);
        }
    }
    
    /**
     * Sets the coordinates and updates the indicator
     * 
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    public void setCoords(int x, int y) {
        this.x = x;
        this.y = y;
        updateCoordinateIndicator();
    }
    
    /**
     * Updates the text in the coordinate indicator and redraws the circle
     */
    private void updateCoordinateIndicator() {
        if (coordinateIndicator != null) {
            coordinateIndicator.setText(String.format("X: %d, Y: %d", x, y));
        }
        if (circleView != null) {
            circleView.updatePosition(x, y);
            circleView.invalidate();
        }
    }
    
    /**
     * Hides the underlay if it's currently visible
     * 
     * @return true if the underlay was hidden, false if it wasn't showing
     */
    public boolean hide() {
        try {
            if (rootLayout != null) {
                if (rootLayout.isAttachedToWindow()) {
                    windowManager.removeView(rootLayout);
                }
                rootLayout = null;
                coordinateIndicator = null;
                circleView = null;
                Log.d(TAG, "Underlay view hidden");
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding underlay view: " + e.getMessage(), e);
        }
        return false;
    }
    
    /**
     * Check if the underlay is currently visible
     * 
     * @return true if visible, false otherwise
     */
    public boolean isVisible() {
        return rootLayout != null;
    }
    
    /**
     * Sets the listener for touch events on the underlay
     * 
     * @param listener The callback to invoke when underlay is touched
     */
    public void setOnUnderlayTouchListener(OnUnderlayTouchListener listener) {
        this.touchListener = listener;
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
    
    /**
     * Custom view that draws a yellow circle at the specified coordinates
     */
    private class CircleView extends View {
        private final Paint paint;
        private int circleX;
        private int circleY;
        
        public CircleView(Context context) {
            super(context);
            
            paint = new Paint();
            paint.setColor(CIRCLE_COLOR);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            
            // Initialize with current coordinates
            this.circleX = x;
            this.circleY = y;
        }
        
        /**
         * Update the circle position
         * 
         * @param x The x-coordinate
         * @param y The y-coordinate
         */
        public void updatePosition(int x, int y) {
            this.circleX = x;
            this.circleY = y;
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawCircle(circleX, circleY, CIRCLE_RADIUS, paint);
        }
    }
}