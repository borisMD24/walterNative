package com.walter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;

import com.walter.CoordinateConverter;
import com.walter.CoordinateConverter.Cartesian;
import com.walter.CoordinateConverter.Polar;

public class ContextMenuItem {
    private static final String TAG = "ContextMenuItem";
    private static final int ANIMATION_DURATION = 5000; // shorter animation duration for better UX
    
    private int centerX;
    private int centerY;
    private final Context ctx;
    private final int nth;
    private final View blueCircle;
    private int circleSize = 200;  // diamètre du rond bleu
    private int bubbleSize;
    private int x = 0;
    private int y = 0;
    private ValueAnimator currentAnimator; // Track current animator to cancel if needed

    /**
     * @param ctx      le contexte
     * @param parent   le ViewGroup (FrameLayout ou autre) dans lequel on injecte le rond
     * @param nth      indice ou usage interne
     */
    public ContextMenuItem(Context ctx, ViewGroup parent, int nth) {
        this.ctx = ctx;
        this.nth = nth;

        // 1) Création du rond bleu
        blueCircle = new View(ctx);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        switch (nth) {
            case 0:
                bg.setColor(0xFFFF0000);
                break;
            case 1:
                bg.setColor(0xFF00FF00);
                break;
            case 2:
                bg.setColor(0xFF0000FF);
                break;
            default:
                bg.setColor(0xFF888888); // Default fallback color
                break;
        }
        
        blueCircle.setBackground(bg);

        // Clip sur outline si possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            blueCircle.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            blueCircle.setClipToOutline(true);
        }

        // LayoutParams – on prend le type générique de parent
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            circleSize, circleSize
        );

        // 2) Ajout au parent (quel qu'il soit)
        parent.addView(blueCircle, params);

        // Hide initially
        blueCircle.setAlpha(0f);
        blueCircle.setScaleX(0.5f);
        blueCircle.setScaleY(0.5f);
        
        // Position initiale invisible
    }
    
    public void setBubbleSize(int bubbleSize) {
        this.bubbleSize = bubbleSize;
        this.circleSize = bubbleSize;
        
        // Update view size
        ViewGroup.LayoutParams params = blueCircle.getLayoutParams();
        params.width = circleSize;
        params.height = circleSize;
        blueCircle.setLayoutParams(params);
    }
    
    /**
     * Calculate target position based on coordinates
     * Updates internal state but does not move the view
     */
    private Cartesian calculateTargetPosition(int x, int y) {
        this.centerX = x;
        this.centerY = y;
        float normalX = this.getNormalizedX();
        float normalXN1To1 = map01toNeg1To1(normalX);
        
        CoordinateConverter cc = new CoordinateConverter();
        // Use consistent radius value based on bubbleSize
        int radius = Math.max(150, bubbleSize);
        
        CoordinateConverter.Polar computedRotation = new CoordinateConverter.Polar(
            radius, // Use fixed radius instead of scaling by normalXN1To1
            ((this.nth-1) * (Math.PI/3)) + normalX * Math.PI
        );
        
        Cartesian c = cc.polarToCartesian(computedRotation);
        this.x = (int) c.x + x; // Store absolute position
        this.y = (int) c.y + y; // Store absolute position
        return c;
    }
    
    /**
     * Positions the blue circle immediately (without animation)
     * This is used for non-animated updates to the position
     */
    public void setCoords(int x, int y) {
        Cartesian offset = calculateTargetPosition(x, y);
        
        // Calculate the position
        float circleRadius = circleSize / 2f; // Half of the circle diameter
        int targetX = (int) (x + offset.x - circleRadius);
        int targetY = (int) (y - offset.y - circleRadius);
        
        // Set position immediately
        blueCircle.setX(targetX);
        blueCircle.setY(targetY);
    }
    
    /**
     * Starts the animation from the bubble position to the calculated position
     * This is used when the context menu is first shown
     * 
     * @param bubbleX X position of the bubble center
     * @param bubbleY Y position of the bubble center
     */
    public void startAnim(int bubbleX, int bubbleY) {
        // Cancel any ongoing animation
        cancelCurrentAnimation();
        
        // Calculate the target position
        Cartesian offset = calculateTargetPosition(bubbleX, bubbleY);
        float circleRadius = circleSize / 2f;
        
        // Starting position (at the bubble)
        final int startX = bubbleX - (int)circleRadius;
        final int startY = bubbleY - (int)circleRadius;
        
        // Target position (with calculated offset)
        final int endX = (int) (bubbleX + offset.x - circleRadius);
        final int endY = (int) (bubbleY + offset.y - circleRadius); // Changed minus to plus for y-coordinate
        
        // First position the circle at the bubble
        blueCircle.setX(startX);
        blueCircle.setY(startY);
        blueCircle.setAlpha(0f);
        blueCircle.setScaleX(0.5f);
        blueCircle.setScaleY(0.5f);
        
        // Create animators for position, alpha and scale
        ValueAnimator positionAnimator = ValueAnimator.ofFloat(0f, 1f);
        positionAnimator.setDuration(ANIMATION_DURATION);
        positionAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(blueCircle, "alpha", 0f, 1f);
        alphaAnimator.setDuration(ANIMATION_DURATION);
        
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(blueCircle, "scaleX", 0.5f, 1f);
        scaleXAnimator.setDuration(ANIMATION_DURATION);
        
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(blueCircle, "scaleY", 0.5f, 1f);
        scaleYAnimator.setDuration(ANIMATION_DURATION);
        
        // Position animator update listener
        positionAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            
            int currentX = (int) (startX + (endX - startX) * fraction);
            int currentY = (int) (startY + (endY - startY) * fraction);
            
            blueCircle.setX(currentX);
            blueCircle.setY(currentY);
        });
        
        // Play all animations together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(positionAnimator, alphaAnimator, scaleXAnimator, scaleYAnimator);
        
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        
        // Start animation
        currentAnimator = positionAnimator;
        animatorSet.start();
    }
    
    /**
     * Hides the menu item with a fade-out and move animation
     * 
     * @param x Target X position (bubble center) to animate towards
     * @param y Target Y position (bubble center) to animate towards
     */
    public void hide(int x, int y) {
        // Cancel any ongoing animation
        cancelCurrentAnimation();
        
        // If view is already hidden or being removed
        if (blueCircle == null || blueCircle.getParent() == null || blueCircle.getAlpha() == 0f) {
            return;
        }
        
        float circleRadius = circleSize / 2f;
        
        // Current position of the circle
        final float startX = blueCircle.getX();
        final float startY = blueCircle.getY();
        
        // Target position (center of bubble - radius)
        final float endX = x - circleRadius;
        final float endY = y - circleRadius;
        
        // Create position animator
        ValueAnimator positionAnimator = ValueAnimator.ofFloat(0f, 1f);
        positionAnimator.setDuration(ANIMATION_DURATION);
        positionAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        
        // Create alpha animator (fade out)
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(blueCircle, "alpha", blueCircle.getAlpha(), 0f);
        alphaAnimator.setDuration(ANIMATION_DURATION);
        
        // Create scale animators
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(blueCircle, "scaleX", blueCircle.getScaleX(), 0.5f);
        scaleXAnimator.setDuration(ANIMATION_DURATION);
        
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(blueCircle, "scaleY", blueCircle.getScaleY(), 0.5f);
        scaleYAnimator.setDuration(ANIMATION_DURATION);
        
        // Position animator update listener
        positionAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            
            float currentX = startX + (endX - startX) * fraction;
            float currentY = startY + (endY - startY) * fraction;
            
            blueCircle.setX(currentX);
            blueCircle.setY(currentY);
        });
        
        // Play all animations together
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(positionAnimator, alphaAnimator, scaleXAnimator, scaleYAnimator);
        
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }
            
            @Override
            public void onAnimationCancel(Animator animation) {
                currentAnimator = null;
            }
        });
        
        // Start animation
        currentAnimator = positionAnimator;
        animatorSet.start();
    }
    
    /**
     * Cancel any ongoing animation to prevent memory leaks or animation glitches
     */
    private void cancelCurrentAnimation() {
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel();
            currentAnimator = null;
        }
    }
    
    /**
     * Returns a normalized X value between 0 and 1
     * based on the horizontal position of the bubble
     */
    public float getNormalizedX() {
        // Rayon de la bulle
        float radius = this.bubbleSize;

        // Largeur de l'écran
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        float screenWidth = dm.widthPixels;

        // Bornes pour centerX
        float minX = radius;
        float maxX = screenWidth - radius;

        // Clamp de centerX
        float clampedX = Math.max(minX, Math.min(centerX, maxX));

        // Normalisation
        return (clampedX - minX) / (maxX - minX);
    }
    
    /**
     * Maps a value from 0-1 range to -1 to 1 range
     */
    public float map01toNeg1To1(float v) {
        // on suppose v ∈ [0,1]
        return v * 2f - 1f;
    }
    
    /**
     * Clean up resources when the view is no longer needed
     */
    public void cleanup() {
        cancelCurrentAnimation();
        if (blueCircle != null && blueCircle.getParent() != null) {
            ((ViewGroup) blueCircle.getParent()).removeView(blueCircle);
        }
    }
}