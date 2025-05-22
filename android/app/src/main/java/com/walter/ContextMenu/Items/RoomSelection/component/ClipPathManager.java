package com.walter;

import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.graphics.Outline;
import android.os.Build;
import androidx.annotation.RequiresApi;

/**
 * ClipPathManager handles clipping operations for views using various shapes and patterns.
 * Provides methods to apply different clip paths like rounded rectangles, circles, 
 * custom paths, and more complex shapes.
 */
public class ClipPathManager {
    
    public enum ClipType {
        NONE,
        ROUNDED_RECTANGLE,
        CIRCLE,
        OVAL,
        CUSTOM_PATH,
        ROUNDED_TOP_CORNERS,
        ROUNDED_BOTTOM_CORNERS,
        HEXAGON,
        TRIANGLE,
        DIAMOND
    }
    
    private View targetView;
    private ClipType currentClipType = ClipType.NONE;
    private Path customPath;
    private float cornerRadius = 0f;
    private RectF bounds = new RectF();
    
    public ClipPathManager(View targetView) {
        this.targetView = targetView;
    }
    
    /**
     * Applies a rounded rectangle clip path to the view
     * @param cornerRadius Radius for all corners in pixels
     */
    public void applyRoundedRectangle(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        this.currentClipType = ClipType.ROUNDED_RECTANGLE;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            targetView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadius);
                }
            });
            targetView.setClipToOutline(true);
        } else {
            // Fallback for older versions - use custom clipping
            applyCustomClipping();
        }
    }
    
    /**
     * Applies a circular clip path to the view
     * Uses the smaller dimension (width or height) as diameter
     */
    public void applyCircle() {
        this.currentClipType = ClipType.CIRCLE;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            targetView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void getOutline(View view, Outline outline) {
                    int size = Math.min(view.getWidth(), view.getHeight());
                    int centerX = view.getWidth() / 2;
                    int centerY = view.getHeight() / 2;
                    int radius = size / 2;
                    outline.setOval(centerX - radius, centerY - radius, 
                                  centerX + radius, centerY + radius);
                }
            });
            targetView.setClipToOutline(true);
        } else {
            applyCustomClipping();
        }
    }
    
    /**
     * Applies an oval clip path to the view
     * Uses the full width and height of the view
     */
    public void applyOval() {
        this.currentClipType = ClipType.OVAL;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            targetView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            targetView.setClipToOutline(true);
        } else {
            applyCustomClipping();
        }
    }
    
    /**
     * Applies rounded corners only to the top of the view
     * @param cornerRadius Radius for top corners in pixels
     */
    public void applyRoundedTopCorners(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        this.currentClipType = ClipType.ROUNDED_TOP_CORNERS;
        applyCustomClipping();
    }
    
    /**
     * Applies rounded corners only to the bottom of the view
     * @param cornerRadius Radius for bottom corners in pixels
     */
    public void applyRoundedBottomCorners(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        this.currentClipType = ClipType.ROUNDED_BOTTOM_CORNERS;
        applyCustomClipping();
    }
    
    /**
     * Applies a hexagonal clip path to the view
     */
    public void applyHexagon() {
        this.currentClipType = ClipType.HEXAGON;
        applyCustomClipping();
    }
    
    /**
     * Applies a triangular clip path to the view
     */
    public void applyTriangle() {
        this.currentClipType = ClipType.TRIANGLE;
        applyCustomClipping();
    }
    
    /**
     * Applies a diamond-shaped clip path to the view
     */
    public void applyDiamond() {
        this.currentClipType = ClipType.DIAMOND;
        applyCustomClipping();
    }
    
    /**
     * Applies a custom path for clipping
     * @param path Custom Path object defining the clip shape
     */
    public void applyCustomPath(Path path) {
        this.customPath = new Path(path);
        this.currentClipType = ClipType.CUSTOM_PATH;
        applyCustomClipping();
    }
    
    /**
     * Removes all clipping from the view
     */
    public void removeClipping() {
        this.currentClipType = ClipType.NONE;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            targetView.setClipToOutline(false);
            targetView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        }
        
        // Remove custom clipping
        targetView.setClipToOutline(false);
        targetView.invalidate();
    }
    
    /**
     * Updates the clipping when view dimensions change
     * Call this method when the view's size changes
     */
    public void updateClipping() {
        if (currentClipType != ClipType.NONE) {
            // Reapply current clipping type
            switch (currentClipType) {
                case ROUNDED_RECTANGLE:
                    applyRoundedRectangle(cornerRadius);
                    break;
                case CIRCLE:
                    applyCircle();
                    break;
                case OVAL:
                    applyOval();
                    break;
                case ROUNDED_TOP_CORNERS:
                    applyRoundedTopCorners(cornerRadius);
                    break;
                case ROUNDED_BOTTOM_CORNERS:
                    applyRoundedBottomCorners(cornerRadius);
                    break;
                case HEXAGON:
                    applyHexagon();
                    break;
                case TRIANGLE:
                    applyTriangle();
                    break;
                case DIAMOND:
                    applyDiamond();
                    break;
                case CUSTOM_PATH:
                    if (customPath != null) {
                        applyCustomPath(customPath);
                    }
                    break;
            }
        }
    }
    
    /**
     * Creates a path for complex shapes that can't be handled by Outline
     */
    private Path createPathForCurrentType() {
        Path path = new Path();
        float width = targetView.getWidth();
        float height = targetView.getHeight();
        
        switch (currentClipType) {
            case ROUNDED_TOP_CORNERS:
                path.addRoundRect(new RectF(0, 0, width, height), 
                    new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0, 0, 0, 0}, 
                    Path.Direction.CW);
                break;
                
            case ROUNDED_BOTTOM_CORNERS:
                path.addRoundRect(new RectF(0, 0, width, height), 
                    new float[]{0, 0, 0, 0, cornerRadius, cornerRadius, cornerRadius, cornerRadius}, 
                    Path.Direction.CW);
                break;
                
            case HEXAGON:
                float centerX = width / 2f;
                float centerY = height / 2f;
                float radius = Math.min(width, height) / 2f * 0.8f;
                
                for (int i = 0; i < 6; i++) {
                    float angle = (float) (i * Math.PI / 3);
                    float x = centerX + radius * (float) Math.cos(angle);
                    float y = centerY + radius * (float) Math.sin(angle);
                    if (i == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }
                path.close();
                break;
                
            case TRIANGLE:
                path.moveTo(width / 2f, 0);
                path.lineTo(0, height);
                path.lineTo(width, height);
                path.close();
                break;
                
            case DIAMOND:
                path.moveTo(width / 2f, 0);
                path.lineTo(width, height / 2f);
                path.lineTo(width / 2f, height);
                path.lineTo(0, height / 2f);
                path.close();
                break;
                
            case CIRCLE:
                float circleRadius = Math.min(width, height) / 2f;
                path.addCircle(width / 2f, height / 2f, circleRadius, Path.Direction.CW);
                break;
                
            case OVAL:
                path.addOval(new RectF(0, 0, width, height), Path.Direction.CW);
                break;
                
            case CUSTOM_PATH:
                if (customPath != null) {
                    path.set(customPath);
                }
                break;
        }
        
        return path;
    }
    
    /**
     * Applies custom clipping using canvas clipping (for complex shapes)
     */
    private void applyCustomClipping() {
        targetView.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    targetView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
                
                // Create custom drawable with clipping
                ClipDrawable clipDrawable = new ClipDrawable(createPathForCurrentType());
                targetView.setBackground(clipDrawable);
                targetView.invalidate();
            }
        });
    }
    
    /**
     * Custom drawable that handles path-based clipping
     */
    private static class ClipDrawable extends android.graphics.drawable.Drawable {
        private Path clipPath;
        
        ClipDrawable(Path clipPath) {
            this.clipPath = clipPath;
        }
        
        @Override
        public void draw(android.graphics.Canvas canvas) {
            canvas.clipPath(clipPath);
        }
        
        @Override
        public void setAlpha(int alpha) {}
        
        @Override
        public void setColorFilter(android.graphics.ColorFilter colorFilter) {}
        
        @Override
        public int getOpacity() {
            return android.graphics.PixelFormat.TRANSLUCENT;
        }
    }
    
    // Getters
    public ClipType getCurrentClipType() {
        return currentClipType;
    }
    
    public float getCornerRadius() {
        return cornerRadius;
    }
    
    public Path getCustomPath() {
        return customPath != null ? new Path(customPath) : null;
    }
}