package com.walter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.view.Gravity;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class DraggableDot {
    private Context ctx;
    private ViewGroup parent;
    private DotView dotView;
    private BrightnessComponent brightnessComponent;
    private float dotPosition = 0.1f; // Position along the arc (0.0 to 1.0)
    private float lastFixDotPosition = 0.1f;
    private OnBrightnessChangeListener brightnessChangeListener;
    
    // Arc parameters as instance variables
    private float startAngle = 315f;
    private float sweepAngle = 90f;
    private float baseStartAngle = 315f;
    
    // New variable to store rotation
    private float rotation = 0f; // Degrees of rotation (0 to 90)
    
    private float pathOffset = 0f;

    private boolean onLeft = true;
    private boolean onLeftLive = true;
    public float x;
    public float y;
    
    // Animation constants
    private static final int DISPLAY_ANIM_DURATION = 250; // ms - longer for smoother ease-out
    private static final int UNDISPLAY_ANIM_DURATION = 250; // ms
    private static final float BOUNCE_FACTOR = 1.5f;  // Overshoot magnitude
    private static final float EASE_OUT_FACTOR = 2.5f; // Strong ease-out factor
    
    // Callback interface for brightness changes
    public interface OnBrightnessChangeListener {
        void onBrightnessChanged(float brightness);
    }
    
    public DraggableDot(Context ctx, ViewGroup parent, BrightnessComponent brightnessComponent) {
        this.ctx = ctx;
        this.parent = parent;
        this.brightnessComponent = brightnessComponent;
        
        // Create the dot view
        dotView = new DotView(ctx);
        
        // Setup layout parameters
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        params.gravity = Gravity.CENTER;
        dotView.setLayoutParams(params);
        
        // Initialize with zero scale and alpha for animations
        dotView.setScaleX(0f);
        dotView.setScaleY(0f);
        dotView.setAlpha(0f);
        this.brightnessComponent.menuContext.onBubbleFix(()->{
            this.lastFixDotPosition = this.dotPosition;
            this.onLeft = this.onLeftLive;
        });
    }
    
    // Setter for startAngle
    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
        refresh();
    }
    
    // Getter for startAngle
    public float getStartAngle() {
        return startAngle;
    }
    
    // Setter for sweepAngle
    public void setSweepAngle(float sweepAngle) {
        this.sweepAngle = sweepAngle;
        refresh();
    }
    
    // Getter for sweepAngle
    public float getSweepAngle() {
        return sweepAngle;
    }
    
    public void setOnBrightnessChangeListener(OnBrightnessChangeListener listener) {
        this.brightnessChangeListener = listener;
    }
    
    public void setPosition(float position) {
        setPosition(position, true);
    }
    public void setPosition(float position, boolean setFixed) {
        // Clamp position between 0.0 and 1.0
        this.dotPosition = Math.max(0.0f, Math.min(1.0f, position));
        if (dotView != null) {
            dotView.updateDotPosition();
            dotView.invalidate();
            
            // Notify listener of brightness change (position directly correlates to brightness)
            if (brightnessChangeListener != null) {
                brightnessChangeListener.onBrightnessChanged(dotPosition);
            }
        }
        if(setFixed == true){
            lastFixDotPosition = this.dotPosition;
            this.brightnessComponent.setBrightnessValue(1-(this.onLeft ? dotPosition : (float)(1.0 - dotPosition)));
        }
    }
    
    public float getPosition() {
        return dotPosition;
    }
    
    public void setBrightnessComponent(BrightnessComponent component) {
        this.brightnessComponent = component;
        if (dotView != null) {
            dotView.updateDotPosition();
            dotView.invalidate();
        }
    }
    
    // Implementation of setNormalizedX to control rotation
    public void setNormalizedX() {
        // Clamp normalizedX between 0.0 and 1.0
        float normalizedX = Math.max(0.0f, Math.min(1.0f, brightnessComponent.menuContext.normalizedBubbleX));
        
        // Update live tracking of which side we're on (left or right)
        onLeftLive = normalizedX < 0.5f;
        
        // Calculate position based on current fixed state (not the live state)
        float newPosition;
        if (onLeft) {
            // Original formula for left side
            newPosition = lastFixDotPosition * (1 - 2 * normalizedX) + normalizedX;
        } else {
            // Formula for right side
            //should be lastFixDotPosition * (1 - 2 * normalizedX) + normalizedX but symetric by 0.5, all the terms arleardy are 0 to 1
             newPosition = lastFixDotPosition * (2 * normalizedX - 1) + (1 - normalizedX);
        }
        
        setPosition(newPosition, false);
        
        // Convert normalized value to rotation angle (0.0 -> 0 degrees, 1.0 -> 90 degrees)
        rotation = normalizedX * 90f;
        
        // Apply the rotation effect by adjusting the startAngle
        pathOffset = 180 * normalizedX;
        setStartAngle(baseStartAngle + rotation);
    }
    
    // Getter for current rotation
    public float getRotation() {
        return rotation;
    }
    
    private class DotView extends View {
        private Paint paint;
        private Paint dotShadowPaint;
        private int dotRadius = 40; // Taille du dot
        private PointF dotCenter = new PointF();
        private boolean dragging = false;

        public DotView(Context context) {
            super(context);

            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.FILL);

            dotShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            dotShadowPaint.setColor(Color.DKGRAY);
            dotShadowPaint.setStyle(Paint.Style.FILL);

            setBackgroundColor(Color.TRANSPARENT);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            updateDotPosition();
        }

        public void updateDotPosition() {
            if (getWidth() == 0 || getHeight() == 0 || brightnessComponent == null) {
                return;
            }

            RectF arcRect = brightnessComponent.getArcRect();
            float arcRadius = brightnessComponent.getArcRadius();

            // Angle de départ unifié depuis BrightnessComponent
            float calculatedStartAngle =
                brightnessComponent.getStartAngle() + brightnessComponent.getRotationOffset();
            float angle = calculatedStartAngle + (dotPosition * sweepAngle);
            double radians = Math.toRadians(angle);

            dotCenter.x = arcRect.centerX() + arcRadius * (float) Math.cos(radians);
            dotCenter.y = arcRect.centerY() + arcRadius * (float) Math.sin(radians);
            x = dotCenter.x;
            y = dotCenter.y;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if (brightnessComponent == null) return;

            // Ombre du dot
            canvas.drawCircle(dotCenter.x + 3, dotCenter.y + 3, dotRadius + 2, dotShadowPaint);
            // Dot principal
            canvas.drawCircle(dotCenter.x, dotCenter.y, dotRadius, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    float dx = x - dotCenter.x;
                    float dy = y - dotCenter.y;
                    float distance = (float) Math.hypot(dx, dy);
                    if (distance <= dotRadius * 1.5f) {
                        dragging = true;
                        updateDotPositionFromTouch(x, y);
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (dragging) {
                        updateDotPositionFromTouch(x, y);
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    break;
            }

            return super.onTouchEvent(event);
        }

        private void updateDotPositionFromTouch(float touchX, float touchY) {
            if (brightnessComponent == null) return;

            RectF arcRect = brightnessComponent.getArcRect();
            float centerX = arcRect.centerX();
            float centerY = arcRect.centerY();

            float calculatedStartAngle =
                brightnessComponent.getStartAngle() + brightnessComponent.getRotationOffset();

            float dx = touchX - centerX;
            float dy = touchY - centerY;
            double rawAngle = Math.toDegrees(Math.atan2(dy, dx));
            float touchAngle = (float) ((rawAngle < 0) ? rawAngle + 360 : rawAngle);

            float normalizedStart = calculatedStartAngle % 360;
            if (normalizedStart < 0) normalizedStart += 360;
            float normalizedEnd = (normalizedStart + sweepAngle) % 360;
            boolean crossesZero = normalizedEnd < normalizedStart;

            float relativePos;
            if (crossesZero) {
                if (touchAngle >= normalizedStart || touchAngle <= normalizedEnd) {
                    if (touchAngle >= normalizedStart) {
                        relativePos = (touchAngle - normalizedStart) /
                                      (360 - normalizedStart + normalizedEnd);
                    } else {
                        relativePos = (360 - normalizedStart + touchAngle) /
                                      (360 - normalizedStart + normalizedEnd);
                    }
                } else {
                    float distStart = Math.min(Math.abs(touchAngle - normalizedStart),
                                               Math.abs(touchAngle - (normalizedStart - 360)));
                    float distEnd = Math.min(Math.abs(touchAngle - normalizedEnd),
                                             Math.abs(touchAngle - (normalizedEnd + 360)));
                    relativePos = (distStart < distEnd) ? 0f : 1f;
                }
            } else {
                if (touchAngle >= normalizedStart && touchAngle <= normalizedEnd) {
                    relativePos = (touchAngle - normalizedStart) / sweepAngle;
                } else {
                    float distStart = Math.abs(touchAngle - normalizedStart);
                    float distEnd = Math.abs(touchAngle - normalizedEnd);
                    relativePos = (distStart < distEnd) ? 0f : 1f;
                }
            }

            setPosition(Math.max(0f, Math.min(1f, relativePos)));
        }
    }

    // Public methods to interact with the dot
    public void setDotColor(int color) {
        if (dotView != null) {
            dotView.paint.setColor(color);
            dotView.invalidate();
        }
    }
    
    public void setDotSize(int radius) {
        if (dotView != null) {
            dotView.dotRadius = Math.max(10, radius);
            dotView.invalidate();
        }
    }
    
    public void setShadowColor(int color) {
        if (dotView != null) {
            dotView.dotShadowPaint.setColor(color);
            dotView.invalidate();
        }
    }
    
    // Method to force update position and redraws
    public void refresh() {
        if (dotView != null) {
            dotView.updateDotPosition();
            dotView.invalidate();
        }
    }
    
    // Cleanup method to remove the view when no longer needed
    public void destroy() {
        if (parent != null && dotView != null) {
            parent.removeView(dotView);
        }
    }

    /**
     * Displays the dot with an enhanced scale and fade animation.
     * Uses a strong ease-out effect for smooth appearance.
     */
    public void display() {
        // If view is already attached, do nothing
        if (dotView.getParent() != null) {
            return;
        }

        // Add view to parent
        parent.addView(dotView);
        
        // Force initial position calculation
        dotView.updateDotPosition();

        // Reset to initial state
        dotView.setScaleX(0f);
        dotView.setScaleY(0f);
        dotView.setAlpha(0f);
        
        // Create scale animations with overshoot (bounce) effect
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(dotView, "scaleX", 0f, 1f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(dotView, "scaleY", 0f, 1f);
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(dotView, "alpha", 0f, 1f);
        
        // Create animator set for scale with bounce
        AnimatorSet scaleAnimSet = new AnimatorSet();
        scaleAnimSet.playTogether(scaleXAnim, scaleYAnim);
        scaleAnimSet.setInterpolator(new OvershootInterpolator(BOUNCE_FACTOR));
        
        // Set alpha with strong ease-out for smoother appearance
        alphaAnim.setInterpolator(new DecelerateInterpolator(EASE_OUT_FACTOR));
        
        // Create final animator set
        AnimatorSet finalAnimSet = new AnimatorSet();
        finalAnimSet.setDuration(DISPLAY_ANIM_DURATION);
        finalAnimSet.playTogether(scaleAnimSet, alphaAnim);
        
        // Start the animations
        finalAnimSet.start();
    }

    /**
     * Hides the dot with enhanced scale and fade animations.
     * Uses strong ease-out for a smooth exit.
     */
    public void undisplay() {
        // If view is not attached, do nothing
        if (dotView.getParent() == null) {
            return;
        }

        // Create scale animations with anticipate effect
        ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(dotView, "scaleX", dotView.getScaleX(), 0f);
        ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(dotView, "scaleY", dotView.getScaleY(), 0f);
        ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(dotView, "alpha", dotView.getAlpha(), 0f);
        
        // Create animator set for scale with anticipate
        AnimatorSet scaleAnimSet = new AnimatorSet();
        scaleAnimSet.playTogether(scaleXAnim, scaleYAnim);
        scaleAnimSet.setInterpolator(new AnticipateInterpolator(1.0f));
        
        // Set alpha with strong ease-out
        alphaAnim.setInterpolator(new DecelerateInterpolator(EASE_OUT_FACTOR));
        
        // Create final animator set
        AnimatorSet finalAnimSet = new AnimatorSet();
        finalAnimSet.setDuration(UNDISPLAY_ANIM_DURATION);
        finalAnimSet.playTogether(scaleAnimSet, alphaAnim);
        
        // Add end action to remove the view
        finalAnimSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Notify brightness component
                brightnessComponent.undisplayAnim();
                // Remove view
                destroy();
            }
        });
        
        // Start the animations
        finalAnimSet.start();
    }
    public void updateValueFromParent(float normalizedValue){
        brightnessComponent.logInfo("Brightness set to: " + Float.toString(normalizedValue));
    }
}