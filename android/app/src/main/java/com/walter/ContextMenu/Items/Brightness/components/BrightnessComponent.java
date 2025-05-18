package com.walter;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.Gravity;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import com.walter.DraggableDot;
import com.walter.UdpLogger;
import com.walter.HapticFeedbackManager;
import com.walter.ContextMenuContext;

public class BrightnessComponent {
    private Context ctx;
    private ViewGroup parent;
    private ArcView arcView;
    private float curvatureFactor = 0f; // Default curvature factor (0 is middle)
    public boolean isVisible = false;
    private int arcRadius = 400; // Default arc radius
    private DraggableDot dot;
    private float rotationOffset = 315f;
    private HapticFeedbackManager hfm;
    // Arc parameters as instance variables
    private float startAngle = 315f;
    private float sweepAngle = 90f;
    private float value = 0f;
    
    // Animation properties
    private ValueAnimator sweepAnimator;
    private float currentSweepAngle = 0f;
    private static final int ANIMATION_DURATION = 250;
    public boolean isHidingAfterAnimation = false;
    private static UdpLogger logger;
    public ContextMenuContext menuContext;
    
    // Custom strong ease-out interpolator
    private static final Interpolator STRONG_EASE_OUT = new Interpolator() {
        @Override
        public float getInterpolation(float input) {
            // Strong ease-out function: 1 - (1-t)^4
            return 1.0f - (float)Math.pow(1.0f - input, 4);
        }
    };
    
    public BrightnessComponent(Context ctx, ViewGroup parent, ContextMenuContext menuContext) {
        this.ctx = ctx;
        this.parent = parent;
        this.menuContext = menuContext;
        this.menuContext.onMove(()->{
            this.setCoords();
            this.dot.setNormalizedX();
            rotateWithNormalizedX();
        });
        // Initialize HapticFeedbackManager early
        this.hfm = new HapticFeedbackManager(ctx);

        // Create the arc view
        arcView = new ArcView(ctx);

        // Setup layout parameters to match parent size (full screen)
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        params.gravity = Gravity.CENTER;
        arcView.setLayoutParams(params);

        // Don't show the view immediately - it's invisible by default
        this.dot = new DraggableDot(ctx, parent, this);
        
        // Initialize the sweep animator
        initSweepAnimator();
        initializeLogger();
    }
    
    private void initializeLogger() {
        try {
            logger = new UdpLogger("192.168.1.18", 9999);
        } catch (Exception e) {
            //Log.e(TAG, "Failed to initialize UDP logger: " + e.getMessage(), e);
        }
    }
    
    public void logInfo(String message) {
        if (logger != null) {
            logger.info(message);
        }
    }

    /**
     * Initialize the sweep animator for the arc animation with strong ease-out effect
     */
    private void initSweepAnimator() {
        sweepAnimator = ValueAnimator.ofFloat(0f, sweepAngle);
        sweepAnimator.setDuration(ANIMATION_DURATION);
        
        // Apply the strong ease-out interpolator
        sweepAnimator.setInterpolator(STRONG_EASE_OUT);
        
        sweepAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentSweepAngle = (float) animation.getAnimatedValue();
                if (arcView != null) {
                    arcView.invalidate();
                }
            }
        });
        
        // Add listener to handle hiding after animation completes
        sweepAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (isHidingAfterAnimation) {
                    hide();
                    dot.undisplay();
                    isHidingAfterAnimation = false;
                } else {
                    dot.display();
                }
            }
        });
    }
    
    // Setter for startAngle
    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
        if (arcView != null) {
            arcView.invalidate();
        }
        if (dot != null) {
            dot.refresh();
        }
    }
    
    // Getter for startAngle
    public float getStartAngle() {
        return startAngle;
    }
    
    // Setter for sweepAngle
    public void setSweepAngle(float sweepAngle) {
        this.sweepAngle = sweepAngle;
        if (arcView != null) {
            arcView.invalidate();
        }
        if (dot != null) {
            dot.refresh();
        }
        // Update the animator with the new sweep angle
        if (sweepAnimator != null) {
            sweepAnimator.setFloatValues(0f, sweepAngle);
        }
    }
    
    // Getter for sweepAngle
    public float getSweepAngle() {
        return sweepAngle;
    }
    
    // Setter for rotation offset
    public void setRotationOffset(float rotationOffset) {
        this.rotationOffset = rotationOffset;
        if (arcView != null) {
            arcView.invalidate();
        }
        if (dot != null) {
            dot.refresh();
        }
    }
    
    /**
     * Rotates the arc clockwise by 45 degrees (1/8 turn)
     */
    public void rotateEighthTurnClockwise() {
        // Add 45 degrees to the current rotation offset
        float newRotation = rotationOffset + 45f;
        
        // Normalize to keep within 0-360 range for clarity
        if (newRotation >= 360f) {
            newRotation -= 360f;
        }
        
        // Set the new rotation offset
        setRotationOffset(newRotation);
    }
    
    public float getRotationOffset() {
        return rotationOffset;
    }
    
    /**
     * Rotates the arc based on normalized X position (0 to 1)
     * @param normalizedX Value between 0 and 1 representing horizontal position
     */
    public void rotateWithNormalizedX() {
        // Ensure normalizedX is between 0 and 1
        float normalizedX = Math.max(0f, Math.min(1f, menuContext.normalizedBubbleX));
        
        // Map normalizedX to rotation angle (e.g., 0 to 360 degrees)
        float rotationAngle = normalizedX * 180f;
        
        // Update the rotation offset
        setRotationOffset(rotationAngle);
    }
    
    public void show() {
        if (!isVisible) {
            parent.addView(arcView);
            isVisible = true;
        }
    }

    public void hide() {
        if (isVisible) {
            parent.removeView(arcView);
            isVisible = false;
        }
    }

    /**
     * Sets the coordinates of the arc's center. Call before show/update.
     */
    public void setCoords() {
        if (arcView != null) {
            arcView.updateArcRect();
            arcView.invalidate();
        }
        if (dot != null) {
            dot.refresh();
        }
    }

    public void setCurvature(float factor /* -1 to 1 */) {
        this.curvatureFactor = Math.max(-1f, Math.min(1f, factor));
        if (arcView != null) {
            arcView.updateArcBend(curvatureFactor);
            arcView.invalidate();
        }
    }

    public void setArcRadius(int radius) {
        this.arcRadius = Math.max(50, radius); // Ensure minimum radius of 50
        if (arcView != null) {
            arcView.updateArcRect();
            arcView.invalidate();
        }
        if (dot != null) {
            dot.refresh();
        }
    }

    // Getter for curvature factor (added for DraggableDot)
    public float getCurvatureFactor() {
        return curvatureFactor;
    }

    // Getter for arc radius (added for DraggableDot)
    public int getArcRadius() {
        return arcRadius;
    }

    // Getter for arc dimensions (added for DraggableDot)
    public RectF getArcRect() {
        if (arcView != null) {
            return arcView.getArcRect();
        }
        return new RectF();
    }

    
    public void setValue(float value){
        this.value = Math.max(0f, Math.min(1f, value));
    }
    
    /**
     * Display method with sweep animation using strong ease-out
     * Call this method to trigger the arc sweep animation
     */
    public void display() {
        // Reset animation state
        currentSweepAngle = 0f;
        isHidingAfterAnimation = false;
        
        // Make sure the arc is visible
        show();
        
        // Start the sweep animation
        if (sweepAnimator != null) {
            // Cancel any ongoing animation
            if (sweepAnimator.isRunning()) {
                sweepAnimator.cancel();
            }
            // Set animation to go from 0 to full sweep angle
            sweepAnimator.setFloatValues(0f, sweepAngle);
            // Ensure the strong ease-out interpolator is applied
            sweepAnimator.setInterpolator(STRONG_EASE_OUT);
            sweepAnimator.start();
        }
    }
    
    /**
     * Undisplay method with reverse sweep animation
     * Animates the arc back to zero and then hides it
     */
    public void undisplay() {
        // Only proceed if the component is currently visible
        if (!isVisible) {
            return;
        }
        
        dot.undisplay();
    }
    
    /**
     * Undisplay with animation using strong ease-out for closing animation
     */
    public void undisplayAnim(){
        isHidingAfterAnimation = true;
        
        // Set up and start the reverse animation
        if (sweepAnimator != null) {
            // Cancel any ongoing animation
            if (sweepAnimator.isRunning()) {
                sweepAnimator.cancel();
            }
            
            // Set animation to go from current sweep angle to 0
            float startValue = currentSweepAngle > 0 ? currentSweepAngle : sweepAngle;
            sweepAnimator.setFloatValues(startValue, 0f);
            
            // Custom interpolator for closing animation - reversed ease-out for smooth closing
            sweepAnimator.setInterpolator(STRONG_EASE_OUT);
            
            sweepAnimator.start();
        } else {
            // If animator isn't available, just hide immediately
            hide();
        }
    }

    private class ArcView extends View {
        private Paint paint;
        private RectF arcRect;
        private float bendFactor = 0f;

        public ArcView(Context context) {
            super(context);

            // Initialize paint for drawing the arc
            paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStrokeWidth(30f); // Thicker stroke
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setAntiAlias(true);

            // Initialize rectangle for arc bounds
            arcRect = new RectF();

            // Make background transparent
            setBackgroundColor(Color.TRANSPARENT);
        }

        public void updateArcBend(float bend) {
            this.bendFactor = bend;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            // Update arc dimensions whenever the view size changes
            updateArcRect();
        }

        public void updateArcRect() {
            // Use custom center if set, otherwise default to bottom-right
            float cx = (menuContext.bubbleX != 0f || menuContext.bubbleY != 0f) ? menuContext.bubbleX : (getWidth() - arcRadius);
            float cy = (menuContext.bubbleX != 0f || menuContext.bubbleY != 0f) ? menuContext.bubbleY : (getHeight() - arcRadius);
            arcRect.left = cx - arcRadius;
            arcRect.top = cy - arcRadius;
            arcRect.right = cx + arcRadius;
            arcRect.bottom = cy + arcRadius;
        }

        // Getter for arc rectangle (added for DraggableDot)
        public RectF getArcRect() {
            return new RectF(arcRect);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (getWidth() == 0 || getHeight() == 0) {
                return; // Don't draw if dimensions aren't available yet
            }

            // Ensure arc rect is updated with current radius
            updateArcRect();

            // When animating, use the currentSweepAngle instead of the full sweepAngle
            float drawSweepAngle = sweepAnimator != null && sweepAnimator.isRunning() 
                ? currentSweepAngle 
                : sweepAngle;
                
            // Draw arc using the calculated angle values
            float calculatedStartAngle = startAngle + rotationOffset;
            canvas.drawArc(arcRect, calculatedStartAngle, drawSweepAngle, false, paint);
        }
    }
    
    public void setBrightnessValue(float value) {
        // Ensure value is between 0 and 1
        float normalizedValue = Math.max(0f, Math.min(1f, value));
        
        // Log the brightness value
        logInfo("Brightness set to: " + normalizedValue);
        
        // Store the value
        setValue(normalizedValue);
        
        // Apply haptic feedback based on the brightness level
        // Check if haptic feedback manager is initialized
        if (hfm != null) {
            // Higher brightness = stronger vibration
            hfm.vibrate(normalizedValue, 5);
        }
    }
    private void setNormalizedValueByArcTouch(float normalizedValue){
        // should be called when the arc AND THE ARC ONLY is touched
        // this value should be the touch angle normalized (0 to 1)
    }
}