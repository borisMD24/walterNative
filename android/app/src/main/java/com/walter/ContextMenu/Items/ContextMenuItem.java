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
import com.walter.ContextMenuContext;
import java.util.function.BiConsumer;

public class ContextMenuItem {
    private static final int ANIMATION_DURATION = 300;
    
    public final Context ctx;
    public final int nth;
    public final FrameLayout containerView;
    public final View blueCircle;
    public final ImageView iconImageView;
    private ValueAnimator currentAnimator;
    public OnMenuItemClickListener clickListener;
    public int iconID = R.drawable.bedroom;
    public ContextMenuContext menuContext;
    private boolean oppened = false;
    private BiConsumer<Integer, Integer> moveCallback = null;
    

    public interface OnMenuItemClickListener {
        void onMenuItemClick(int index);
    }

    public ContextMenuItem(Context ctx, ViewGroup parent, int nth, ContextMenuContext menuContext) {
        this.ctx = ctx;
        this.nth = nth;
        this.menuContext =  menuContext;
        this.menuContext.onMove(() -> {
            this.setCoords();

        });
        this.menuContext.onBubbleResize(()->{
            this.onBubbleResize();
        });
        containerView = new FrameLayout(ctx);
        blueCircle = new View(ctx);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(0xFFFFFFFF);
        
        blueCircle.setBackground(bg);
        iconImageView = new ImageView(ctx);
        iconImageView.setImageResource(iconID);
        
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
            menuContext.expandedBubbleSize / 2, menuContext.expandedBubbleSize / 2
        );
        imageParams.gravity = android.view.Gravity.CENTER;
        
        containerView.addView(blueCircle, new FrameLayout.LayoutParams(
            menuContext.expandedBubbleSize, menuContext.expandedBubbleSize
        ));
        containerView.addView(iconImageView, imageParams);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            containerView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            containerView.setClipToOutline(true);
        }

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            menuContext.expandedBubbleSize, menuContext.expandedBubbleSize
        );

        parent.addView(containerView, params);
        containerView.setAlpha(0f);
        containerView.setScaleX(0.5f);
        containerView.setScaleY(0.5f);
        
        containerView.setOnClickListener(v -> click());
    }
    
    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.clickListener = listener;
    }
    
    public void setIconID(int iconResourceID) {
        this.iconID = iconResourceID;
        if (iconImageView != null) {
            iconImageView.setImageResource(iconResourceID);
        }
    }
    
    public void onBubbleResize() {
        ViewGroup.LayoutParams containerParams = containerView.getLayoutParams();
        containerParams.width = menuContext.expandedBubbleSize;
        containerParams.height = menuContext.expandedBubbleSize;
        containerView.setLayoutParams(containerParams);
        
        ViewGroup.LayoutParams circleParams = blueCircle.getLayoutParams();
        circleParams.width = menuContext.expandedBubbleSize;
        circleParams.height = menuContext.expandedBubbleSize;
        blueCircle.setLayoutParams(circleParams);
        
        FrameLayout.LayoutParams imageParams = (FrameLayout.LayoutParams) iconImageView.getLayoutParams();
        imageParams.width = menuContext.expandedBubbleSize / 2;
        imageParams.height = menuContext.expandedBubbleSize / 2;
        iconImageView.setLayoutParams(imageParams);
    }
    
    private Cartesian calculateTargetPosition() {
        float normalX = getNormalizedX();
        float normalXN1To1 = map01toNeg1To1(normalX);
        
        CoordinateConverter cc = new CoordinateConverter();
        int radius = menuContext.expandedBubbleSize;
        
        CoordinateConverter.Polar computedRotation = new CoordinateConverter.Polar(
            Math.abs(normalXN1To1) * menuContext.expandedBubbleSize * 1.5, 
            ((this.nth-1) * (normalXN1To1 * (Math.PI/3))) + Math.abs(normalX * Math.PI)
        );
        Cartesian r = cc.polarToCartesian(computedRotation);
        this.menuContext.setRoomSelectionCoords((int)r.x, (int)r.y);
        return r;
    }
    
    public void setCoords() {
        Cartesian offset = calculateTargetPosition();
        float circleRadius = menuContext.expandedBubbleSize / 2f;
        int targetX = (int) (menuContext.bubbleX + offset.x - circleRadius);
        int targetY = (int) (menuContext.bubbleY - offset.y - circleRadius);
        
        containerView.setX(targetX);
        containerView.setY(targetY);
        if (moveCallback != null) {
            moveCallback.accept((int)targetX, (int)targetY);
        }
    }
    
    public void click() {
        containerView.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .withEndAction(() -> containerView.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(100))
            .start();
            
        if (clickListener != null) {
            clickListener.onMenuItemClick(nth);
        }
    }

    public void startAnim() {
        cancelCurrentAnimation();
        
        Cartesian offset = calculateTargetPosition();
        float circleRadius = menuContext.expandedBubbleSize / 2f;
        
        final int startX = menuContext.bubbleX - (int)circleRadius;
        final int startY = menuContext.bubbleY - (int)circleRadius;
        final int endX = (int) (menuContext.bubbleX + offset.x - circleRadius);
        final int endY = (int) (menuContext.bubbleY - offset.y - circleRadius);
        
        containerView.setX(startX);
        containerView.setY(startY);
        containerView.setAlpha(0f);
        containerView.setScaleX(0.5f);
        containerView.setScaleY(0.5f);
        
        ValueAnimator positionAnimator = ValueAnimator.ofFloat(0f, 1f);
        positionAnimator.setDuration(ANIMATION_DURATION);
        positionAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(containerView, "alpha", 0f, 1f);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(containerView, "scaleX", 0.5f, 1f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(containerView, "scaleY", 0.5f, 1f);

        positionAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            containerView.setX(startX + (endX - startX) * fraction);
            containerView.setY(startY + (endY - startY) * fraction);
        });
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(positionAnimator, alphaAnimator, scaleXAnimator, scaleYAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }
        });
        
        currentAnimator = positionAnimator;
        animatorSet.start();
    }

    public void hide() {
        cancelCurrentAnimation();
        if (containerView == null || containerView.getParent() == null || containerView.getAlpha() == 0f) return;
        
        float circleRadius = menuContext.expandedBubbleSize / 2f;
        final float startX = containerView.getX();
        final float startY = containerView.getY();
        final float endX = menuContext.bubbleX - circleRadius;
        final float endY = menuContext.bubbleY - circleRadius;
        
        ValueAnimator positionAnimator = ValueAnimator.ofFloat(0f, 1f);
        positionAnimator.setDuration(ANIMATION_DURATION);
        positionAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(containerView, "alpha", containerView.getAlpha(), 0f);
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(containerView, "scaleX", containerView.getScaleX(), 0.5f);
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(containerView, "scaleY", containerView.getScaleY(), 0.5f);

        positionAnimator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            containerView.setX(startX + (endX - startX) * fraction);
            containerView.setY(startY + (endY - startY) * fraction);
        });
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(positionAnimator, alphaAnimator, scaleXAnimator, scaleYAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
            }
        });
        
        currentAnimator = positionAnimator;
        animatorSet.start();
    }

    private void cancelCurrentAnimation() {
        if (currentAnimator != null && currentAnimator.isRunning()) {
            currentAnimator.cancel();
            currentAnimator = null;
        }
    }
    
    public float getNormalizedX() {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        float screenWidth = dm.widthPixels;
        float minX = menuContext.expandedBubbleSize;
        float maxX = screenWidth - menuContext.expandedBubbleSize;
        float clampedX = Math.max(minX, Math.min(menuContext.bubbleX, maxX));
        return (clampedX - minX) / (maxX - minX);
    }
    
    public float map01toNeg1To1(float v) {
        return v * 2f - 1f;
    }
    
    public void cleanup() {
        cancelCurrentAnimation();
        if (containerView != null && containerView.getParent() != null) {
            ((ViewGroup) containerView.getParent()).removeView(containerView);
        }
    }
    public void setMoveCallback(BiConsumer<Integer, Integer> cb){
        moveCallback = cb;
    }
    public void bubbleFixed() {}
}