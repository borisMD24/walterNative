package com.walter;

import android.content.Context;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Path;
import android.animation.ValueAnimator;
import android.view.animation.DecelerateInterpolator;
import com.walter.UdpLogger;
import android.os.Handler;
import android.os.Looper;
import android.widget.ScrollView;
import android.view.MotionEvent;
import android.animation.ObjectAnimator;
import android.view.animation.OvershootInterpolator;

public class ThemeSelectionComponents {
    private static final int DEFAULT_THUMBNAIL_SIZE = 100;
    private static final int DEFAULT_SPACING = 20;
    private static final float SCROLL_SENSITIVITY = 0.8f;
    private static final int SCROLL_ANIMATION_DURATION = 300;
    private static final float MIN_SCROLL_VELOCITY = 50f;
    private static final float SCROLL_FRICTION = 0.85f;
    
    private final Context context;
    private final ViewGroup container;
    private final List<RoomThumbnail> thumbnails = new ArrayList<>();
    private int thumbnailRadius = DEFAULT_THUMBNAIL_SIZE / 2;
    private FlexboxLayout thumbnailContainer;
    private ScrollView scrollView;
    private FrameLayout borderContainer; // New border container
    private boolean isDisplayed = false;
    private ContextMenuContext menuContext;
    private int maxHeight;
    private int maxWidth;
    private static UdpLogger logger;
    
    // Scroll state variables
    private float currentScrollY = 0f;
    private float maxScrollY = 0f;
    private boolean isScrolling = false;
    private ValueAnimator scrollAnimator;
    private float scrollVelocity = 0f;
    private long lastScrollTime = 0;
    
    public static ValueAnimator createEaseOut(float startValue, float endValue, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat(startValue, endValue);
        animator.setDuration(duration);
        animator.setInterpolator(new DecelerateInterpolator());
        return animator;
    }
    
    public static ValueAnimator createEaseOut(float startValue, float endValue, long duration, 
                                            ValueAnimator.AnimatorUpdateListener updateListener) {
        ValueAnimator animator = createEaseOut(startValue, endValue, duration);
        animator.addUpdateListener(updateListener);
        return animator;
    }
    
    // ClipPathManager integration
    private ClipPathManager clipPathManager;

    public ThemeSelectionComponents(Context context, ViewGroup container, ContextMenuContext menuContext) {
        this.context = context;
        this.container = container;
        this.menuContext = menuContext;
        this.maxHeight = menuContext.screenHeight/3;
        this.maxWidth = menuContext.screenWidth - 2 * menuContext.bubbleSize;
        
        // Create the hierarchy: borderContainer -> ScrollView -> thumbnailContainer
        this.borderContainer = new FrameLayout(context);
        this.scrollView = new ScrollView(context);
        this.thumbnailContainer = new FlexboxLayout(context);
        
        // Initialize ClipPathManager on the border container
        this.clipPathManager = new ClipPathManager(borderContainer);
        
        setupLayout();
        this.updateThumbnailContainerDimensions(maxWidth, maxHeight);
        
        // Apply default rounded rectangle clipping
        applyDefaultClipping();
        initializeLogger();
    }
    
    private void initializeLogger() {
        try {
            logger = new UdpLogger("192.168.1.18", 9999);
        } catch (Exception e) {
        }
    }
    
    private static void logInfo(String message) {
        if (logger != null) {
            logger.info(message);
        }
    }
    
    private void setupLayout() {
        // Configure border container
        setupBorderContainer();
        
        // Configure ScrollView
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        scrollView.setBackgroundColor(Color.TRANSPARENT); // Make ScrollView transparent
        
        // Configure FlexboxLayout
        thumbnailContainer.setFlexDirection(FlexDirection.ROW);
        thumbnailContainer.setFlexWrap(FlexWrap.WRAP);
        thumbnailContainer.setJustifyContent(JustifyContent.CENTER);
        thumbnailContainer.setBackgroundColor(Color.TRANSPARENT); // Make thumbnailContainer transparent
        
        // Build the hierarchy
        scrollView.addView(thumbnailContainer, new ScrollView.LayoutParams(
            ScrollView.LayoutParams.MATCH_PARENT,
            ScrollView.LayoutParams.WRAP_CONTENT));
            
        borderContainer.addView(scrollView, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT));
        
        // Setup scroll listener for smooth scrolling feedback
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            currentScrollY = scrollView.getScrollY();
            updateScrollBounds();
        });
    }

    /**
     * Sets up the border container with default styling
     */
    private void setupBorderContainer() {
        borderContainer.post(() -> {
            float cornerRadius = menuContext.bubbleSize / 4f;
            int borderWidth = 4;
            int borderColor = Color.parseColor("#CCCCCC");
            int backgroundColor = Color.WHITE;
            
            applyBorderContainerStyle(cornerRadius, borderWidth, borderColor, backgroundColor);
        });
    }

    /**
     * Applies styling to the border container
     */
    private void applyBorderContainerStyle(float cornerRadius, int borderWidth, int borderColor, int backgroundColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(cornerRadius);
        drawable.setStroke(borderWidth, borderColor);
        drawable.setColor(backgroundColor);
        
        // Add padding to the border container so content doesn't touch the border
        int padding = borderWidth + 8;
        borderContainer.setPadding(padding, padding, padding, padding);
        
        borderContainer.setBackground(drawable);
    }

    /**
     * Updates the border container styling with custom parameters
     */
    public void updateBorderContainerStyle(float cornerRadius, int borderWidth, int borderColor, int backgroundColor) {
        borderContainer.post(() -> {
            applyBorderContainerStyle(cornerRadius, borderWidth, borderColor, backgroundColor);
        });
    }

    /**
     * Applies default clipping (rounded rectangle) to the border container
     */
    private void applyDefaultClipping() {
        float cornerRadius = menuContext.bubbleSize / 8f;
        clipPathManager.applyRoundedRectangle(cornerRadius);
    }

    public void display() {
        if (!isDisplayed) {
            if (borderContainer.getParent() == null) {
                container.addView(borderContainer, 0, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            }
            this.updateThumbnailContainerDimensions(maxWidth, maxHeight);
            setupBorderContainer();
            clearThumbnails();
            List<Room> rooms = parseRoomsFromJson();
            addThumbnails(rooms);
            isDisplayed = true;
            logInfo(Integer.toString(menuContext.themeSelectionX)+";"+Integer.toString(menuContext.themeSelectionY));
            
            // Reset scroll position
            currentScrollY = 0f;
            scrollView.scrollTo(0, 0);
            updateScrollBounds();
            
            // Set initial clip to a tiny circle at the touch point
            setClipPathBasedOnNormalizedValue(0f, menuContext.themeSelectionX, menuContext.themeSelectionY);
            
            ValueAnimator animator = createEaseOut(0f, 1f, 500, animation -> {
                setClipPathBasedOnNormalizedValue((float)animation.getAnimatedValue(), menuContext.themeSelectionX, menuContext.themeSelectionY);
            });
            animator.start();
        }
    }
    
    void setClipPathBasedOnNormalizedValue(float normalized, int absoluteX, int absoluteY) {
        // Get positions of container and borderContainer
        int[] containerLocation = new int[2];
        int[] borderContainerLocation = new int[2];
        container.getLocationOnScreen(containerLocation);
        borderContainer.getLocationOnScreen(borderContainerLocation);

        // Offset between the two (in absolute coordinates)
        int offsetX = borderContainerLocation[0] - containerLocation[0];
        int offsetY = borderContainerLocation[1] - containerLocation[1];

        // Clipping point coordinates *relative* to borderContainer
        int x = absoluteX - offsetX;
        int y = absoluteY - offsetY;

        // Border container dimensions
        int screenW = borderContainer.getWidth();
        int screenH = borderContainer.getHeight();

        // Maximum radius to cover the entire borderContainer from (x, y)
        float dTL = (float) Math.hypot(x, y);
        float dTR = (float) Math.hypot(screenW - x, y);
        float dBL = (float) Math.hypot(x, screenH - y);
        float dBR = (float) Math.hypot(screenW - x, screenH - y);
        float maxRadius = Math.max(Math.max(dTL, dTR), Math.max(dBL, dBR));

        // Current radius according to progress
        float currentRadius = Math.max(1f, normalized * maxRadius);

        // Circular clipping
        clipPathManager.removeClipping();
        clipPathManager.setCoords(
            x - currentRadius,
            y - currentRadius,
            x + currentRadius,
            y + currentRadius
        );
        clipPathManager.applyCircle();
    }

    public void undisplay() {
        ValueAnimator animator = createEaseOut(1f, 0f, 500, animation -> {
            setClipPathBasedOnNormalizedValue((float)animation.getAnimatedValue(), menuContext.themeSelectionX, menuContext.themeSelectionY);
        });
        animator.start();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isDisplayed) {
                clearThumbnails();
                container.removeView(borderContainer);
                isDisplayed = false;
            }
        }, 500);
    }

    private List<Room> parseRoomsFromJson() {
        List<Room> rooms = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(GetJson.get());
            JSONArray jsonRooms = jsonObject.getJSONArray("rooms");
            for (int i = 0; i < jsonRooms.length(); i++) {
                JSONObject room = jsonRooms.getJSONObject(i);
                rooms.add(new Room(
                        room.getString("name"),
                        room.getInt("id"),
                        room.getString("img")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    private void addThumbnails(List<Room> rooms) {
        int thumbnailSize = thumbnailRadius * 2;
        int spacing = DEFAULT_SPACING;
        for (Room room : rooms) {
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(thumbnailSize, thumbnailSize);
            params.setMargins(spacing / 2, spacing / 2, spacing / 2, spacing / 2);
            RoomThumbnail thumbnail = new RoomThumbnail(
                    context, room.id, thumbnailRadius / 2, room.imageUrl, thumbnailContainer, params);
            thumbnail.name = room.name;
            thumbnail.updateNameLabel();
            thumbnail.setDragCallback((dy)->{
                this.onScroll(dy);
            });
            thumbnail.setClickCallback((id)->{
                this.handleIconClick(id);
            });
            thumbnails.add(thumbnail);
        }
        
        // Update scroll bounds after adding thumbnails
        thumbnailContainer.post(() -> updateScrollBounds());
    }

    private void clearThumbnails() {
        for (RoomThumbnail thumbnail : thumbnails) {
            thumbnail.destroy();
        }
        thumbnails.clear();
        thumbnailContainer.removeAllViews();
        currentScrollY = 0f;
        maxScrollY = 0f;
    }

    private static class Room {
        String name;
        int id;
        String imageUrl;

        Room(String name, int id, String imageUrl) {
            this.name = name;
            this.id = id;
            this.imageUrl = imageUrl;
        }
    }
    
    /**
     * Updates scroll bounds based on content and container sizes
     */
    private void updateScrollBounds() {
        int contentHeight = thumbnailContainer.getHeight();
        int containerHeight = scrollView.getHeight();
        maxScrollY = Math.max(0, contentHeight - containerHeight);
        
        // Clamp current scroll position
        currentScrollY = Math.max(0, Math.min(currentScrollY, maxScrollY));
    }
    
    /**
     * Smoothly scroll to a specific Y position
     */
    private void smoothScrollTo(float targetY) {
        targetY = Math.max(0, Math.min(targetY, maxScrollY));
        
        if (scrollAnimator != null && scrollAnimator.isRunning()) {
            scrollAnimator.cancel();
        }
        
        scrollAnimator = ValueAnimator.ofFloat(currentScrollY, targetY);
        scrollAnimator.setDuration(SCROLL_ANIMATION_DURATION);
        scrollAnimator.setInterpolator(new DecelerateInterpolator());
        scrollAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            scrollView.scrollTo(0, (int) value);
            currentScrollY = value;
        });
        scrollAnimator.start();
    }
    
    /**
     * Apply momentum scrolling with deceleration
     */
    private void applyMomentumScroll(float velocity) {
        if (Math.abs(velocity) < MIN_SCROLL_VELOCITY) {
            return;
        }
        
        // Calculate target position based on velocity and friction
        float decelerationDistance = (velocity * velocity) / (2 * SCROLL_FRICTION * 1000);
        if (velocity < 0) {
            decelerationDistance = -decelerationDistance;
        }
        
        float targetY = currentScrollY + decelerationDistance;
        smoothScrollTo(targetY);
    }
    
    /**
     * Handle scroll input from drag callbacks
     */
    private void onScroll(int dy) {
        logInfo("Scroll delta: " + Integer.toString(dy));
        
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.max(1, currentTime - lastScrollTime); // Prevent division by zero
        lastScrollTime = currentTime;
        
        // Apply scroll sensitivity
        float adjustedDy = dy * SCROLL_SENSITIVITY;
        
        // Calculate new scroll position
        float newScrollY = currentScrollY + adjustedDy;
        newScrollY = Math.max(0, Math.min(newScrollY, maxScrollY));
        
        // Calculate velocity for momentum scrolling
        scrollVelocity = adjustedDy / deltaTime * 1000; // pixels per second
        
        // Apply immediate scroll
        if (Math.abs(adjustedDy) > 0) {
            scrollView.scrollTo(0, (int) newScrollY);
            currentScrollY = newScrollY;
            isScrolling = true;
            
            // Stop any existing momentum animation
            if (scrollAnimator != null && scrollAnimator.isRunning()) {
                scrollAnimator.cancel();
            }
            
            // Set up momentum scroll when scrolling stops
            Handler handler = new Handler(Looper.getMainLooper());
            handler.removeCallbacksAndMessages("momentum_scroll");
            handler.postDelayed(() -> {
                if (isScrolling) {
                    isScrolling = false;
                    applyMomentumScroll(scrollVelocity);
                }
            }, 100); // Wait 100ms after last scroll input
        }
        
        // Provide haptic feedback at scroll boundaries
        if (newScrollY <= 0 || newScrollY >= maxScrollY) {
            // Add subtle haptic feedback when hitting boundaries
            // You can implement haptic feedback here if needed
            logInfo("Scroll boundary reached");
        }
    }
    
    /**
     * Scroll to top of the container
     */
    public void scrollToTop() {
        smoothScrollTo(0);
    }
    
    /**
     * Scroll to bottom of the container
     */
    public void scrollToBottom() {
        updateScrollBounds();
        smoothScrollTo(maxScrollY);
    }
    
    /**
     * Scroll by a specific amount
     */
    public void scrollBy(int deltaY) {
        onScroll(deltaY);
    }
    
    /**
     * Get current scroll position
     */
    public float getCurrentScrollPosition() {
        return currentScrollY;
    }
    
    /**
     * Get maximum scroll position
     */
    public float getMaxScrollPosition() {
        return maxScrollY;
    }
    
    /**
     * Check if content is scrollable
     */
    public boolean isScrollable() {
        return maxScrollY > 0;
    }
    
    private void handleIconClick(int id){
        logInfo("thumbnail with id : " + Integer.toString(id) + " clicked");
    }
    
    /**
     * Updates the dimensions of the border container and its contents.
     */
    public void updateThumbnailContainerDimensions(int width, int height) {
        ViewGroup.LayoutParams borderParams = borderContainer.getLayoutParams();
        
        if (borderParams != null) {
            borderParams.width = width;
            borderParams.height = height;
            borderContainer.setLayoutParams(borderParams);
            
            if (height != ViewGroup.LayoutParams.MATCH_PARENT && 
                height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                this.maxHeight = height;
            }
        } else {
            ViewGroup.LayoutParams newParams = new ViewGroup.LayoutParams(width, height);
            borderContainer.setLayoutParams(newParams);
            
            if (height != ViewGroup.LayoutParams.MATCH_PARENT && 
                height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                this.maxHeight = height;
            }
        }
        
        borderContainer.requestLayout();
        
        // Update scroll bounds and clipping after dimensions change
        borderContainer.post(() -> {
            updateScrollBounds();
            clipPathManager.updateClipping();
        });
    }

    /**
     * Updates the coordinates (margins) of the border container.
     */
    public void updateThumbnailContainerMargins(int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams currentParams = borderContainer.getLayoutParams();
        
        if (currentParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) currentParams;
            params.setMargins(left, top, right, bottom);
            borderContainer.setLayoutParams(params);
        } else {
            FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(
                    currentParams.width, 
                    currentParams.height);
            newParams.setMargins(left, top, right, bottom);
            borderContainer.setLayoutParams(newParams);
        }
        
        borderContainer.requestLayout();
    }

    /**
     * Updates the position of the border container using X and Y coordinates.
     */
    public void updateThumbnailContainerPosition(int x, int y) {
        borderContainer.setX(x);
        borderContainer.setY(y);
    }

    /**
     * Updates both dimensions and position of the border container.
     */
    public void updateThumbnailContainerBounds(int x, int y, int width, int height) {
        updateThumbnailContainerDimensions(width, height);
        updateThumbnailContainerPosition(x, y);
    }

    /**
     * Dynamically adjusts the height of the border container based on content
     * without exceeding the maxHeight value.
     */
    public void adjustThumbnailContainerHeight() {
        int contentHeight = calculateContentHeight();
        int newHeight = Math.min(contentHeight, maxHeight);
        
        ViewGroup.LayoutParams layoutParams = borderContainer.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = newHeight;
            borderContainer.setLayoutParams(layoutParams);
        }
        
        borderContainer.requestLayout();
        
        borderContainer.post(() -> {
            updateScrollBounds();
            clipPathManager.updateClipping();
        });
    }

    /**
     * Calculates the total height needed to display all thumbnails.
     */
    private int calculateContentHeight() {
        int thumbnailSize = thumbnailRadius * 2;
        int spacing = DEFAULT_SPACING;
        int itemsPerRow = Math.max(1, maxWidth / (thumbnailSize + spacing));
        int rowCount = (int) Math.ceil((double) thumbnails.size() / itemsPerRow);
        
        return rowCount * (thumbnailSize + spacing);
    }

    /**
     * Updates the maximum height for the border container.
     */
    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        if (isDisplayed) {
            adjustThumbnailContainerHeight();
        }
    }
    
    private int computeMoveXOffset(){
         return(int)(
            - menuContext.normalizedBubbleX * maxWidth
            - (menuContext.normalizedBubbleX * 2 - 1) * menuContext.bubbleSize
            -  menuContext.bubbleSize / 10 * menuContext.normalizedBubbleX
        );
    }
    
    private int computeMoveYOffset(){
        return(int)(
            menuContext.bubbleSize / 2
            - maxHeight
        );
    }
    
    public void onIconMove(){
        updateThumbnailContainerPosition(
            menuContext.themeSelectionX + computeMoveXOffset(),
            menuContext.themeSelectionY + computeMoveYOffset()
         );
         setClipPathBasedOnNormalizedValue(
            (float)Math.pow(Math.abs(menuContext.normalizedBubbleX*2-1), 4),
            menuContext.themeSelectionX, 
            menuContext.themeSelectionY
         );
    }
    
    // ClipPathManager integration methods (now applied to border container)
    
    public void applyRoundedRectangleClip(float cornerRadius) {
        clipPathManager.applyRoundedRectangle(cornerRadius);
    }
    
    public void applyCircularClip() {
        clipPathManager.applyCircle();
    }
    
    public void applyOvalClip() {
        clipPathManager.applyOval();
    }
    
    public void applyRoundedTopCornersClip(float cornerRadius) {
        clipPathManager.applyRoundedTopCorners(cornerRadius);
    }
    
    public void applyRoundedBottomCornersClip(float cornerRadius) {
        clipPathManager.applyRoundedBottomCorners(cornerRadius);
    }
    
    public void applyHexagonClip() {
        clipPathManager.applyHexagon();
    }
    
    public void applyTriangleClip() {
        clipPathManager.applyTriangle();
    }
    
    public void applyDiamondClip() {
        clipPathManager.applyDiamond();
    }
    
    public void applyCustomPathClip(Path path) {
        clipPathManager.applyCustomPath(path);
    }
    
    public void removeClipping() {
        clipPathManager.removeClipping();
    }
    
    public ClipPathManager.ClipType getCurrentClipType() {
        return clipPathManager.getCurrentClipType();
    }
    
    public ClipPathManager getClipPathManager() {
        return clipPathManager;
    }

    /**
     * Get reference to the border container for direct styling access
     */
    public FrameLayout getBorderContainer() {
        return borderContainer;
    }

    /**
     * Get reference to the scroll view for direct access
     */
    public ScrollView getScrollView() {
        return scrollView;
    }

    /**
     * Get reference to the thumbnail container for direct access
     */
    public FlexboxLayout getThumbnailContainer() {
        return thumbnailContainer;
    }
}