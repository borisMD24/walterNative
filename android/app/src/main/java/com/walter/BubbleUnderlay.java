package com.walter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 * BubbleUnderlay
 *
 * A full-screen view that appears behind the floating bubble and context menu.
 * This underlay becomes semi-transparent and touch-interactive when shown, and
 * returns to a fully transparent, click-through state when hidden.
 *
 * It helps detect taps outside the floating UI for dismissing menus or bubbles.
 */
public class BubbleUnderlay {

    private static final String TAG = "BubbleUnderlay";

    private final Context context;
    private final WindowManager windowManager;
    private View underlayView;
    private BubbleUnderlayListener listener;
    private boolean isActive = false;

    /**
     * Listener interface for underlay touch events
     */
    public interface BubbleUnderlayListener {
        void onUnderlayTouched();
    }

    /**
     * Constructor
     *
     * @param context Application context
     */
    public BubbleUnderlay(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        createUnderlayView();
    }

    /**
     * Sets the listener for touch events on the underlay
     *
     * @param listener A BubbleUnderlayListener
     */
    public void setListener(BubbleUnderlayListener listener) {
        this.listener = listener;
    }

    /**
     * Creates and configures the underlay view
     */
    private void createUnderlayView() {
        if (underlayView != null) return;

        underlayView = new View(context);
        underlayView.setBackgroundColor(Color.TRANSPARENT);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        underlayView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && isActive && listener != null) {
                listener.onUnderlayTouched();
            }
            return true;
        });

        try {
            windowManager.addView(underlayView, params);
            Log.d(TAG, "Underlay view created");
        } catch (Exception e) {
            Log.e(TAG, "Error adding underlay view: " + e.getMessage(), e);
        }
    }

    /**
     * Shows the underlay with semi-transparent background and touch interaction
     *
     * @return true if successfully shown
     */
    public boolean show() {
        if (underlayView == null) {
            createUnderlayView();
        }

        if (isActive) {
            return false;
        }

        setColor(Color.argb(50, 0, 0, 0)); // semi-transparent black
        setInterceptTouches(true);
        isActive = true;

        Log.d(TAG, "Underlay shown");
        return true;
    }

    /**
     * Hides the underlay, making it fully transparent and click-through
     *
     * @return true if successfully hidden
     */
    public boolean hide() {
        if (underlayView == null || !underlayView.isAttachedToWindow()) {
            return false;
        }

        setColor(Color.TRANSPARENT);
        setInterceptTouches(false);
        isActive = false;

        Log.d(TAG, "Underlay hidden");
        return true;
    }

    /**
     * Checks whether the underlay is currently shown and active
     *
     * @return true if active
     */
    public boolean isShowing() {
        return isActive;
    }

    /**
     * Updates the underlay background color
     *
     * @param color ARGB color
     */
    public void setColor(int color) {
        if (underlayView != null) {
            underlayView.setBackgroundColor(color);
        }
    }

    /**
     * Enables or disables touch interception on the underlay
     *
     * @param intercept true to intercept touches, false to allow passthrough
     */
    public void setInterceptTouches(boolean intercept) {
        if (underlayView != null && underlayView.isAttachedToWindow()) {
            WindowManager.LayoutParams params = (WindowManager.LayoutParams) underlayView.getLayoutParams();

            if (intercept) {
                params.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            } else {
                params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            }

            try {
                windowManager.updateViewLayout(underlayView, params);
            } catch (Exception e) {
                Log.e(TAG, "Error updating underlay layout: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Removes the underlay view and cleans up resources
     */
    public void release() {
        try {
            if (underlayView != null && underlayView.isAttachedToWindow()) {
                windowManager.removeView(underlayView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error removing underlay view: " + e.getMessage(), e);
        } finally {
            underlayView = null;
            listener = null;
        }
    }
}
