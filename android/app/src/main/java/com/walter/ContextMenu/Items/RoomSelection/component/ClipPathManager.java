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
     * Sets custom bounds for clipping (left, top, right, bottom)
     */
    public void setCoords(float left, float top, float right, float bottom) {
        bounds.set(left, top, right, bottom);
        updateClipping();
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
                    RectF rect = getEffectiveBounds(view);
                    outline.setRoundRect((int)rect.left, (int)rect.top,
                                         (int)rect.right, (int)rect.bottom,
                                         cornerRadius);
                }
            });
            targetView.setClipToOutline(true);
        } else {
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
                // Utilisation stricte des bounds prédéfinis (carré de côté 2×currentRadius)
                RectF rect = getEffectiveBounds(view);
                // On trace un ovale qui sera un cercle parfait dans le carré (left, top, right, bottom)
                outline.setOval(
                    (int) rect.left,
                    (int) rect.top,
                    (int) rect.right,
                    (int) rect.bottom
                );
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
                    RectF rect = getEffectiveBounds(view);
                    outline.setOval((int)rect.left, (int)rect.top,
                                    (int)rect.right, (int)rect.bottom);
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
        
        targetView.setClipToOutline(false);
        targetView.invalidate();
    }
    
    /**
     * Updates the clipping when view dimensions change
     * Call this method when the view's size changes
     */
    public void updateClipping() {
        if (currentClipType != ClipType.NONE) {
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
     * Gets the effective bounds: uses custom bounds if set, otherwise full view
     */
    private RectF getEffectiveBounds(View view) {
        if (bounds.width() > 0 && bounds.height() > 0) {
            return bounds;
        }
        return new RectF(0, 0, view.getWidth(), view.getHeight());
    }
    
    /**
     * Creates a path for complex shapes that can't be handled by Outline
     */
    private Path createPathForCurrentType() {
        Path path = new Path();
        RectF rect = getEffectiveBounds(targetView);
        float width = rect.width();
        float height = rect.height();
        float left = rect.left;
        float top = rect.top;
        
        switch (currentClipType) {
            case ROUNDED_TOP_CORNERS:
                path.addRoundRect(new RectF(left, top, left+width, top+height), 
                    new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0, 0, 0, 0}, 
                    Path.Direction.CW);
                break;
            case ROUNDED_BOTTOM_CORNERS:
                path.addRoundRect(new RectF(left, top, left+width, top+height), 
                    new float[]{0, 0, 0, 0, cornerRadius, cornerRadius, cornerRadius, cornerRadius}, 
                    Path.Direction.CW);
                break;
            case HEXAGON:
                float centerX = left + width/2f;
                float centerY = top + height/2f;
                float radius = Math.min(width, height) / 2f * 0.8f;
                for (int i = 0; i < 6; i++) {
                    float angle = (float) (i * Math.PI / 3);
                    float x = centerX + radius * (float) Math.cos(angle);
                    float y = centerY + radius * (float) Math.sin(angle);
                    if (i == 0) path.moveTo(x, y);
                    else path.lineTo(x, y);
                }
                path.close();
                break;
            case TRIANGLE:
                path.moveTo(left+width/2f, top);
                path.lineTo(left, top+height);
                path.lineTo(left+width, top+height);
                path.close();
                break;
            case DIAMOND:
                path.moveTo(left+width/2f, top);
                path.lineTo(left+width, top+height/2f);
                path.lineTo(left+width/2f, top+height);
                path.lineTo(left, top+height/2f);
                path.close();
                break;
            case CIRCLE:
                float circleRadius = Math.min(width, height) / 2f;
                path.addCircle(left+width/2f, top+height/2f, circleRadius, Path.Direction.CW);
                break;
            case OVAL:
                path.addOval(new RectF(left, top, left+width, top+height), Path.Direction.CW);
                break;
            case CUSTOM_PATH:
                if (customPath != null) {
                    path.set(customPath);
                }
                break;
            default:
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
