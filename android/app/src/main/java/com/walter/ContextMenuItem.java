package com.walter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.walter.CoordinateConverter;
import com.walter.CoordinateConverter.Cartesian;
import com.walter.CoordinateConverter.Polar;

public class ContextMenuItem {
    private static final String TAG = "ContextMenuItem";
    private static final int ANIMATION_DURATION = 300; // shorter animation duration for better UX
    private static final String CHANNEL_ID = "context_menu_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private int centerX;
    private int centerY;
    public final Context ctx;
    public final int nth;
    public final FrameLayout containerView; // Container for circle and image
    public final View blueCircle;
    public final ImageView iconImageView; // Added ImageView for the icon
    private int circleSize = 200;  // diamètre du rond bleu
    private int bubbleSize;
    private int x = 0;
    private int y = 0;
    private ValueAnimator currentAnimator; // Track current animator to cancel if needed
    public OnMenuItemClickListener clickListener;
    public int iconID = R.drawable.bedroom;

    /**
     * Interface for click callbacks
     */
    public interface OnMenuItemClickListener {
        void onMenuItemClick(int index);
    }

    /**
     * @param ctx      le contexte
     * @param parent   le ViewGroup (FrameLayout ou autre) dans lequel on injecte le rond
     * @param nth      indice ou usage interne
     */
    public ContextMenuItem(Context ctx, ViewGroup parent, int nth) {
        this.ctx = ctx;
        this.nth = nth;

        // Create container layout that will hold both circle and icon
        containerView = new FrameLayout(ctx);
        
        // 1) Création du rond bleu
        blueCircle = new View(ctx);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        switch (nth) {
            default:
                bg.setColor(0xFFFFFFFF); // Default fallback color
                break;
        }
        
        blueCircle.setBackground(bg);
        
        // 2) Add the image view on top of the circle
        iconImageView = new ImageView(ctx);
        iconImageView.setImageResource(iconID); // Use default icon from drawable resources
        
        // Center the image in the circle and scale it to fit
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
            circleSize / 2, // Image takes up half the size of the circle
            circleSize / 2
        );
        imageParams.gravity = android.view.Gravity.CENTER; // Center in parent
        
        // Add views to container
        containerView.addView(blueCircle, new FrameLayout.LayoutParams(
            circleSize, circleSize
        ));
        containerView.addView(iconImageView, imageParams);

        // Clip container to circular shape if possible
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            containerView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            containerView.setClipToOutline(true);
        }

        // LayoutParams for the container
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            circleSize, circleSize
        );

        // Add container to parent
        parent.addView(containerView, params);

        // Hide initially
        containerView.setAlpha(0f);
        containerView.setScaleX(0.5f);
        containerView.setScaleY(0.5f);
        
        // Add click listener to the container
        containerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click(); // Appel propre à la nouvelle méthode
            }
        });
    }
    
    /**
     * Set a click listener for this menu item
     * @param listener The listener to call when this item is clicked
     */
    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.clickListener = listener;
    }
    
    /**
     * Set the icon resource ID for this menu item and update the displayed icon
     * @param iconResourceID Resource ID of the drawable to use as the icon
     */
    public void setIconID(int iconResourceID) {
        this.iconID = iconResourceID;
        // Update the ImageView with the new icon
        if (iconImageView != null) {
            iconImageView.setImageResource(iconResourceID);
        }
    }
    
    /**
     * Send a notification when the circle is clicked
     */
    public void sendNotification() {
        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Context Menu Channel";
            String description = "Notifications from context menu items";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = ctx.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        
        // Create notification content based on the item number
        String title = "Menu Action";
        String message;
        
        switch (nth) {
            case 0:
                message = "Red circle action triggered";
                break;
            case 1:
                message = "Green circle action triggered";
                break;
            case 2:
                message = "Blue circle action triggered";
                break;
            default:
                message = "Circle " + nth + " action triggered";
                break;
        }
        
        // Create an intent that will be triggered when notification is tapped
        Intent intent = new Intent(ctx, ctx.getClass()); // Replace with the actual activity you want to open
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, 
                PendingIntent.FLAG_IMMUTABLE);
        
        // Build the notification - USING CURRENT ICON
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(iconID) // Using current icon from drawable resources
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        
        // Show the notification
        NotificationManager notificationManager = 
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID + nth, builder.build());
    }
    
    public void setBubbleSize(int bubbleSize) {
        this.bubbleSize = bubbleSize *2;
        this.circleSize = bubbleSize*2;
        
        // Update container size
        ViewGroup.LayoutParams containerParams = containerView.getLayoutParams();
        containerParams.width = circleSize;
        containerParams.height = circleSize;
        containerView.setLayoutParams(containerParams);
        
        // Update circle size
        ViewGroup.LayoutParams circleParams = blueCircle.getLayoutParams();
        circleParams.width = circleSize;
        circleParams.height = circleSize;
        blueCircle.setLayoutParams(circleParams);
        
        // Update image size (half of circle size)
        FrameLayout.LayoutParams imageParams = (FrameLayout.LayoutParams) iconImageView.getLayoutParams();
        imageParams.width = circleSize / 2;
        imageParams.height = circleSize / 2;
        iconImageView.setLayoutParams(imageParams);
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
            Math.abs(normalXN1To1) * 200, 
            ((this.nth-1) * Math.abs(normalXN1To1) * (Math.PI/3)) + normalX * Math.PI
        );
        
        Cartesian c = cc.polarToCartesian(computedRotation);
        this.x = (int) c.x + x; // Store absolute position
        this.y = (int) c.y + y; // Store absolute position
        return c;
    }
    
    /**
     * Positions the container immediately (without animation)
     * This is used for non-animated updates to the position
     */
    public void setCoords(int x, int y) {
        Cartesian offset = calculateTargetPosition(x, y);
        
        // Calculate the position
        float circleRadius = circleSize / 2f; // Half of the circle diameter
        int targetX = (int) (x + offset.x - circleRadius);
        int targetY = (int) (y - offset.y - circleRadius);
        
        // Set position immediately
        containerView.setX(targetX);
        containerView.setY(targetY);
    }
    
    // Nouvelle méthode publique à appeler ou override
    public void click() {
        // Animation de feedback
        containerView.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    containerView.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100);
                }
            }).start();

        // Envoi de notification
        sendNotification();

        // Callback éventuel
        if (clickListener != null) {
            clickListener.onMenuItemClick(nth);
        }

        // Feedback immédiat
        Toast.makeText(ctx, "Menu item " + nth + " clicked", Toast.LENGTH_SHORT).show();
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
        
        // First position the container at the bubble
        containerView.setX(startX);
        containerView.setY(startY);
        containerView.setAlpha(0f);
        containerView.setScaleX(0.5f);
        containerView.setScaleY(0.5f);
        
        // Create animators for position, alpha and scale
        ValueAnimator positionAnimator = ValueAnimator.ofFloat(0f, 1f);
        positionAnimator.setDuration(ANIMATION_DURATION);
        positionAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(containerView, "alpha", 0f, 1f);
        alphaAnimator.setDuration(ANIMATION_DURATION);
        
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(containerView, "scaleX", 0.5f, 1f);
        scaleXAnimator.setDuration(ANIMATION_DURATION);
        
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(containerView, "scaleY", 0.5f, 1f);
        scaleYAnimator.setDuration(ANIMATION_DURATION);
        
        // Position animator update listener
        positionAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            
            int currentX = (int) (startX + (endX - startX) * fraction);
            int currentY = (int) (startY + (endY - startY) * fraction);
            
            containerView.setX(currentX);
            containerView.setY(currentY);
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
        if (containerView == null || containerView.getParent() == null || containerView.getAlpha() == 0f) {
            return;
        }
        
        float circleRadius = circleSize / 2f;
        
        // Current position of the container
        final float startX = containerView.getX();
        final float startY = containerView.getY();
        
        // Target position (center of bubble - radius)
        final float endX = x - circleRadius;
        final float endY = y - circleRadius;
        
        // Create position animator
        ValueAnimator positionAnimator = ValueAnimator.ofFloat(0f, 1f);
        positionAnimator.setDuration(ANIMATION_DURATION);
        positionAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        
        // Create alpha animator (fade out)
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(containerView, "alpha", containerView.getAlpha(), 0f);
        alphaAnimator.setDuration(ANIMATION_DURATION);
        
        // Create scale animators
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(containerView, "scaleX", containerView.getScaleX(), 0.5f);
        scaleXAnimator.setDuration(ANIMATION_DURATION);
        
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(containerView, "scaleY", containerView.getScaleY(), 0.5f);
        scaleYAnimator.setDuration(ANIMATION_DURATION);
        
        // Position animator update listener
        positionAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            
            float currentX = startX + (endX - startX) * fraction;
            float currentY = startY + (endY - startY) * fraction;
            
            containerView.setX(currentX);
            containerView.setY(currentY);
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
        if (containerView != null && containerView.getParent() != null) {
            ((ViewGroup) containerView.getParent()).removeView(containerView);
        }
    }
}