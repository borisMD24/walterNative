package com.walter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A component that displays a scrollable grid of theme thumbnails with smooth
 * animations
 * and customizable clipping paths.
 */
public class ThemeSelectionComponent {

    // Configuration Constants
    private static final int DEFAULT_THUMBNAIL_SIZE = 100;
    private static final int DEFAULT_SPACING = 20;
    private static final int MIN_HEIGHT = 120;
    private static final int BORDER_WIDTH = 4;
    private static final int BORDER_PADDING = 8;
    private static final int ANIMATION_DURATION = 500;

    // Core Components
    private final Context context;
    private final ViewGroup parentContainer;
    private final ContextMenuContext menuContext;

    // UI Components
    private FrameLayout borderContainer;
    private ScrollView scrollView;
    private FlexboxLayout thumbnailContainer;

    // Managers - REMOVED final keyword to fix compilation error
    private ClipPathManager clipPathManager;
    private ScrollManager scrollManager;
    private AnimationManager animationManager;
    private StyleManager styleManager;

    // State
    private final List<RoomThumbnail> thumbnails = new ArrayList<>();
    private boolean isDisplayed = false;
    private int maxHeight;
    private int maxWidth;
    private int thumbnailRadius;

    // Logger
    private static UdpLogger logger;

    public ThemeSelectionComponent(Context context, ViewGroup container, ContextMenuContext menuContext) {
        this.context = context;
        this.parentContainer = container;
        this.menuContext = menuContext;
        this.maxHeight = menuContext.screenHeight / 3;
        this.maxWidth = menuContext.screenWidth - 2 * menuContext.bubbleSize;
        this.thumbnailRadius = DEFAULT_THUMBNAIL_SIZE / 2;

        initializeComponents();
        initializeManagers();
        setupLayout();
        initializeLogger();
    }

    private void initializeComponents() {
        borderContainer = new FrameLayout(context);
        scrollView = new ScrollView(context);
        thumbnailContainer = new FlexboxLayout(context);
    }

    private void initializeManagers() {
        clipPathManager = new ClipPathManager(borderContainer);
        scrollManager = new ScrollManager(scrollView, this::onScrollPositionChanged);
        animationManager = new AnimationManager();
        styleManager = new StyleManager(borderContainer, menuContext);
    }

    private void setupLayout() {
        configureThumbnailContainer();
        configureScrollView();
        configureBorderContainer();
        buildViewHierarchy();
        applyDefaultStyling();
    }

    private void configureThumbnailContainer() {
        thumbnailContainer.setFlexDirection(FlexDirection.ROW);
        thumbnailContainer.setFlexWrap(FlexWrap.WRAP);
        thumbnailContainer.setJustifyContent(JustifyContent.CENTER);
        thumbnailContainer.setBackgroundColor(Color.TRANSPARENT);
    }

    private void configureScrollView() {
        scrollView.setVerticalScrollBarEnabled(false);
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        scrollView.setBackgroundColor(Color.TRANSPARENT);
    }

    private void configureBorderContainer() {
        borderContainer.post(() -> styleManager.applyDefaultStyle());
    }

    private void buildViewHierarchy() {
        scrollView.addView(thumbnailContainer, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        borderContainer.addView(scrollView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
    }

    private void applyDefaultStyling() {
        float cornerRadius = menuContext.bubbleSize / 8f;
        clipPathManager.applyRoundedRectangle(cornerRadius);
    }

    private void initializeLogger() {
        try {
            logger = new UdpLogger("192.168.1.18", 9999);
        } catch (Exception e) {
            // Logger initialization failed - continue without logging
        }
    }

    // Public API Methods

    public void display() {
        if (isDisplayed)
            return;

        addToParentContainer();
        loadAndDisplaythemes();
        animateIn();
        resetScrollPosition();

        isDisplayed = true;
        logInfo("theme selection displayed at: " + menuContext.themeSelectionX + "," + menuContext.themeSelectionY);
    }

    public void hide() {
        if (!isDisplayed)
            return;

        animateOut(() -> {
            removeFromParentContainer();
            clearThumbnails();
            isDisplayed = false;
        });
    }

    // ADDED: undisplay method to fix the missing method error
    public void undisplay() {
        hide();
    }

    public void onIconMove() {
        updatePosition();
        updateClippingForMovement();
    }

    // Layout Management

    public void updateDimensions(int width, int height) {
        ViewGroup.LayoutParams params = borderContainer.getLayoutParams();
        if (params != null) {
            params.width = width;
            params.height = height;
            borderContainer.setLayoutParams(params);

            if (height > 0 && height != ViewGroup.LayoutParams.MATCH_PARENT &&
                    height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                maxHeight = height;
            }

            borderContainer.requestLayout();
            schedulePostLayoutUpdate();
        }
    }

    public void updatePosition(int x, int y) {
        borderContainer.setX(x);
        borderContainer.setY(y);
    }

    public void updateMargins(int left, int top, int right, int bottom) {
        ViewGroup.LayoutParams params = borderContainer.getLayoutParams();

        if (params instanceof ViewGroup.MarginLayoutParams) {
            ((ViewGroup.MarginLayoutParams) params).setMargins(left, top, right, bottom);
        } else {
            FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(
                    params.width, params.height);
            newParams.setMargins(left, top, right, bottom);
            borderContainer.setLayoutParams(newParams);
        }

        borderContainer.requestLayout();
    }

    // Styling Methods

    public void updateStyle(float cornerRadius, int borderWidth, int borderColor, int backgroundColor) {
        styleManager.updateStyle(cornerRadius, borderWidth, borderColor, backgroundColor);
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
        if (isDisplayed) {
            adjustContainerHeight();
        }
    }

    // Clipping Methods

    public void applyRoundedRectangleClip(float cornerRadius) {
        clipPathManager.applyRoundedRectangle(cornerRadius);
    }

    public void applyCircularClip() {
        clipPathManager.applyCircle();
    }

    public void applyCustomPathClip(Path path) {
        clipPathManager.applyCustomPath(path);
    }

    public void removeClipping() {
        clipPathManager.removeClipping();
    }

    // Scroll Methods

    public void scrollToTop() {
        scrollManager.scrollToTop();
    }

    public void scrollToBottom() {
        scrollManager.scrollToBottom();
    }

    public void scrollBy(int deltaY) {
        scrollManager.scrollBy(deltaY);
    }

    public boolean isScrollable() {
        return scrollManager.isScrollable();
    }

    // Getters

    public FrameLayout getBorderContainer() {
        return borderContainer;
    }

    public ScrollView getScrollView() {
        return scrollView;
    }

    public FlexboxLayout getThumbnailContainer() {
        return thumbnailContainer;
    }

    public ClipPathManager getClipPathManager() {
        return clipPathManager;
    }

    // Private Implementation Methods

    private void addToParentContainer() {
        if (borderContainer.getParent() == null) {
            parentContainer.addView(borderContainer, 0, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
        updateDimensions(maxWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

        // Set initial position to match onIconMove positioning
        borderContainer.post(this::updatePosition);

        styleManager.applyDefaultStyle();
    }

    private void removeFromParentContainer() {
        parentContainer.removeView(borderContainer);
    }

    private void loadAndDisplaythemes() {
        clearThumbnails();
        List<Room> rooms = themeDataParser.parsethemesFromJson();
        createThumbnails(rooms);
        scheduleHeightAdjustment();
    }

    private void createThumbnails(List<Room> themes) {
        int thumbnailSize = thumbnailRadius * 2;
        int spacing = DEFAULT_SPACING;

        for (Room room : themes) {
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(thumbnailSize, thumbnailSize);
            params.setMargins(spacing / 2, spacing / 2, spacing / 2, spacing / 2);

            RoomThumbnail thumbnail = new RoomThumbnail(
                    context,
                    room.id,
                    thumbnailRadius / 2,
                    room.imageUrl,
                    thumbnailContainer,
                    params);
            thumbnail.name = room.name;
            thumbnail.updateNameLabel();
            thumbnail.setDragCallback(scrollManager::onScroll);
            thumbnail.setClickCallback(this::handleThumbnailClick);

            thumbnails.add(thumbnail);
        }

        scheduleScrollBoundsUpdate();
    }

    private void clearThumbnails() {
        for (RoomThumbnail thumbnail : thumbnails) {
            thumbnail.destroy();
        }
        thumbnails.clear();
        thumbnailContainer.removeAllViews();
        scrollManager.resetScroll();
    }

    private void adjustContainerHeight() {
        thumbnailContainer.post(() -> {
            int contentHeight = thumbnailContainer.getHeight();
            int borderPadding = borderContainer.getPaddingTop() + borderContainer.getPaddingBottom();
            int totalNeededHeight = contentHeight + borderPadding;
            int newHeight = Math.min(Math.max(totalNeededHeight, MIN_HEIGHT), maxHeight);

            updateContainerHeights(newHeight, borderPadding);
        });
    }

    private void updateContainerHeights(int newBorderHeight, int borderPadding) {
        ViewGroup.LayoutParams borderParams = borderContainer.getLayoutParams();
        if (borderParams != null && borderParams.height != newBorderHeight) {
            borderParams.height = newBorderHeight;
            borderContainer.setLayoutParams(borderParams);

            int scrollViewHeight = newBorderHeight - borderPadding;
            FrameLayout.LayoutParams scrollParams = (FrameLayout.LayoutParams) scrollView.getLayoutParams();
            if (scrollParams != null) {
                scrollParams.height = scrollViewHeight;
                scrollView.setLayoutParams(scrollParams);
            }

            schedulePostLayoutUpdate();
        }
    }

    private void animateIn() {
        setInitialClipState();
        animationManager.createRevealAnimation(0f, 1f, ANIMATION_DURATION,
                progress -> updateClippingForReveal(progress, menuContext.themeSelectionX, menuContext.themeSelectionY))
                .start();
    }

    private void animateOut(Runnable onComplete) {
        animationManager.createRevealAnimation(1f, 0f, ANIMATION_DURATION,
                progress -> updateClippingForReveal(progress, menuContext.themeSelectionX, menuContext.themeSelectionY))
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(onComplete, ANIMATION_DURATION);
    }

    private void setInitialClipState() {
        updateClippingForReveal(0f, menuContext.themeSelectionX, menuContext.themeSelectionY);
    }

    private void updateClippingForReveal(float progress, int absoluteX, int absoluteY) {
        ClippingCalculator calculator = new ClippingCalculator(
                parentContainer, borderContainer, absoluteX, absoluteY);
        calculator.applyCircularClip(clipPathManager, progress);
    }

    private void updateClippingForMovement() {
        float progress = (float) Math.pow(Math.abs(menuContext.normalizedBubbleX * 2 - 1), 4);
        updateClippingForReveal(progress, menuContext.themeSelectionX, menuContext.themeSelectionY);
    }

    private void updatePosition() {
        int x = menuContext.themeSelectionX + computeMoveXOffset();
        int y = menuContext.themeSelectionY + computeMoveYOffset();
        updatePosition(x, y);
    }

    private int computeMoveXOffset() {
        return (int) (-menuContext.normalizedBubbleX * maxWidth
                - (menuContext.normalizedBubbleX * 2 - 1) * menuContext.bubbleSize
                - menuContext.bubbleSize / 10 * menuContext.normalizedBubbleX);
    }

    // Alternative implementation with more sophisticated positioning logic:
    private int computeMoveYOffsetAdvanced() {
        int containerHeight = borderContainer.getHeight();

        // If container hasn't been measured yet, fall back to maxHeight
        if (containerHeight <= 0) {
            containerHeight = maxHeight;
        }

        // Consider bubble position for better positioning
        float bubbleYFactor = menuContext.normalizedBubbleY; // Assuming this exists

        return (int) (menuContext.bubbleSize / 2
                - containerHeight
                + (bubbleYFactor * menuContext.bubbleSize) // Adjust based on bubble Y position
        );
    }

    // Simple fix - just replace maxHeight with actual container height:
    private int computeMoveYOffset() {
        return (int) (menuContext.bubbleSize / 2
                - borderContainer.getHeight() // Use actual container height
        );
    }

    private void resetScrollPosition() {
        scrollManager.resetScroll();
        scrollView.scrollTo(0, 0);
    }

    private void handleThumbnailClick(int id) {
        logInfo("Thumbnail clicked: " + id);
    }

    private void onScrollPositionChanged() {
        schedulePostLayoutUpdate();
    }

    private void scheduleHeightAdjustment() {
        thumbnailContainer.post(this::adjustContainerHeight);
    }

    private void scheduleScrollBoundsUpdate() {
        thumbnailContainer.post(scrollManager::updateScrollBounds);
    }

    private void schedulePostLayoutUpdate() {
        borderContainer.post(() -> {
            scrollManager.updateScrollBounds();
            clipPathManager.updateClipping();
        });
    }

    private static void logInfo(String message) {
        if (logger != null) {
            logger.info(message);
        }
    }

    // Inner Classes and Helpers

    private static class Room {
        final String name;
        final int id;
        final String imageUrl;

        Room(String name, int id, String imageUrl) {
            this.name = name;
            this.id = id;
            this.imageUrl = imageUrl;
        }
    }

    private static class themeDataParser {
        static List<Room> parsethemesFromJson() {
            List<Room> themes = new ArrayList<>();
            try {
                JSONObject jsonObject = new JSONObject(GetJson.get());
                JSONArray jsonthemes = jsonObject.getJSONArray("rooms");

                for (int i = 0; i < jsonthemes.length(); i++) {
                    JSONObject theme = jsonthemes.getJSONObject(i);
                    themes.add(new Room(
                            theme.getString("name"),
                            theme.getInt("id"),
                            theme.getString("img")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return themes;
        }
    }

    private static class StyleManager {
        private final FrameLayout borderContainer;
        private final ContextMenuContext menuContext;

        StyleManager(FrameLayout borderContainer, ContextMenuContext menuContext) {
            this.borderContainer = borderContainer;
            this.menuContext = menuContext;
        }

        void applyDefaultStyle() {
            float cornerRadius = menuContext.bubbleSize / 4f;
            int borderColor = Color.parseColor("#CCCCCC");
            updateStyle(cornerRadius, BORDER_WIDTH, borderColor, Color.WHITE);
        }

        void updateStyle(float cornerRadius, int borderWidth, int borderColor, int backgroundColor) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(cornerRadius);
            drawable.setStroke(borderWidth, borderColor);
            drawable.setColor(backgroundColor);

            int padding = borderWidth + BORDER_PADDING;
            borderContainer.setPadding(padding, padding, padding, padding);
            borderContainer.setBackground(drawable);
        }
    }

    private static class ScrollManager {
        private static final float SCROLL_SENSITIVITY = 0.8f;
        private static final int SCROLL_ANIMATION_DURATION = 300;
        private static final float MIN_SCROLL_VELOCITY = 50f;
        private static final float SCROLL_FRICTION = 0.85f;

        private final ScrollView scrollView;
        private final Runnable onScrollChanged;

        private float currentScrollY = 0f;
        private float maxScrollY = 0f;
        private boolean isScrolling = false;
        private android.animation.ValueAnimator scrollAnimator;
        private float scrollVelocity = 0f;
        private long lastScrollTime = 0;

        ScrollManager(ScrollView scrollView, Runnable onScrollChanged) {
            this.scrollView = scrollView;
            this.onScrollChanged = onScrollChanged;
            setupScrollListener();
        }

        private void setupScrollListener() {
            scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
                currentScrollY = scrollView.getScrollY();
                updateScrollBounds();
                onScrollChanged.run();
            });
        }

        void updateScrollBounds() {
            int contentHeight = ((ViewGroup) scrollView.getChildAt(0)).getHeight();
            int containerHeight = scrollView.getHeight();
            maxScrollY = Math.max(0, contentHeight - containerHeight);
            currentScrollY = Math.max(0, Math.min(currentScrollY, maxScrollY));
        }

        void scrollToTop() {
            smoothScrollTo(0);
        }

        void scrollToBottom() {
            updateScrollBounds();
            smoothScrollTo(maxScrollY);
        }

        void scrollBy(int deltaY) {
            onScroll(deltaY);
        }

        void resetScroll() {
            currentScrollY = 0f;
            maxScrollY = 0f;
        }

        boolean isScrollable() {
            return maxScrollY > 0;
        }

        private void smoothScrollTo(float targetY) {
            targetY = Math.max(0, Math.min(targetY, maxScrollY));

            if (scrollAnimator != null && scrollAnimator.isRunning()) {
                scrollAnimator.cancel();
            }

            scrollAnimator = android.animation.ValueAnimator.ofFloat(currentScrollY, targetY);
            scrollAnimator.setDuration(SCROLL_ANIMATION_DURATION);
            scrollAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator());
            scrollAnimator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                scrollView.scrollTo(0, (int) value);
                currentScrollY = value;
            });
            scrollAnimator.start();
        }

        private void onScroll(int dy) {
            long currentTime = System.currentTimeMillis();
            float deltaTime = Math.max(1, currentTime - lastScrollTime);
            lastScrollTime = currentTime;

            float adjustedDy = dy * SCROLL_SENSITIVITY;
            float newScrollY = Math.max(0, Math.min(currentScrollY + adjustedDy, maxScrollY));

            scrollVelocity = adjustedDy / deltaTime * 1000;

            if (Math.abs(adjustedDy) > 0) {
                scrollView.scrollTo(0, (int) newScrollY);
                currentScrollY = newScrollY;
                isScrolling = true;

                if (scrollAnimator != null && scrollAnimator.isRunning()) {
                    scrollAnimator.cancel();
                }

                Handler handler = new Handler(Looper.getMainLooper());
                handler.removeCallbacksAndMessages("momentum_scroll");
                handler.postDelayed(() -> {
                    if (isScrolling) {
                        isScrolling = false;
                        applyMomentumScroll(scrollVelocity);
                    }
                }, 100);
            }
        }

        private void applyMomentumScroll(float velocity) {
            if (Math.abs(velocity) < MIN_SCROLL_VELOCITY)
                return;

            float decelerationDistance = (velocity * velocity) / (2 * SCROLL_FRICTION * 1000);
            if (velocity < 0)
                decelerationDistance = -decelerationDistance;

            float targetY = currentScrollY + decelerationDistance;
            smoothScrollTo(targetY);
        }
    }

    private static class AnimationManager {
        android.animation.ValueAnimator createRevealAnimation(float startValue, float endValue,
                long duration, AnimationUpdateCallback callback) {
            android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(startValue, endValue);
            animator.setDuration(duration);
            animator.setInterpolator(new android.view.animation.DecelerateInterpolator());
            animator.addUpdateListener(animation -> callback.onUpdate((float) animation.getAnimatedValue()));
            return animator;
        }

        interface AnimationUpdateCallback {
            void onUpdate(float progress);
        }
    }

    private static class ClippingCalculator {
        private final int offsetX;
        private final int offsetY;
        private final int relativeX;
        private final int relativeY;
        private final int containerWidth;
        private final int containerHeight;
        private final float maxRadius;

        ClippingCalculator(ViewGroup parentContainer, FrameLayout borderContainer,
                int absoluteX, int absoluteY) {
            int[] parentLocation = new int[2];
            int[] borderLocation = new int[2];
            parentContainer.getLocationOnScreen(parentLocation);
            borderContainer.getLocationOnScreen(borderLocation);

            this.offsetX = borderLocation[0] - parentLocation[0];
            this.offsetY = borderLocation[1] - parentLocation[1];
            this.relativeX = absoluteX - offsetX;
            this.relativeY = absoluteY - offsetY;
            this.containerWidth = borderContainer.getWidth();
            this.containerHeight = borderContainer.getHeight();
            this.maxRadius = calculateMaxRadius();
        }

        private float calculateMaxRadius() {
            float dTL = (float) Math.hypot(relativeX, relativeY);
            float dTR = (float) Math.hypot(containerWidth - relativeX, relativeY);
            float dBL = (float) Math.hypot(relativeX, containerHeight - relativeY);
            float dBR = (float) Math.hypot(containerWidth - relativeX, containerHeight - relativeY);
            return Math.max(Math.max(dTL, dTR), Math.max(dBL, dBR));
        }

        void applyCircularClip(ClipPathManager clipManager, float progress) {
            float currentRadius = Math.max(1f, progress * maxRadius);

            clipManager.removeClipping();
            clipManager.setCoords(
                    relativeX - currentRadius,
                    relativeY - currentRadius,
                    relativeX + currentRadius,
                    relativeY + currentRadius);
            clipManager.applyCircle();
        }
    }
}