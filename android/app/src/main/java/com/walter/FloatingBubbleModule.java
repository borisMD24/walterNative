package com.walter;

import android.animation.ValueAnimator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import android.content.res.Resources;

/**
 * FloatingBubbleModule
 * 
 * A React Native module that creates and manages a floating bubble overlay
 * that stays on top of all apps and can be used as a quick access point to
 * return to the main application.
 */
public class FloatingBubbleModule extends ReactContextBaseJavaModule {
    private static final String TAG = "FloatingBubbleModule";
    private static final String MODULE_NAME = "FloatingBubbleModule";
    private static final String EVENT_BUBBLE_CLICKED = "bubble_clicked";
    private static final String EVENT_BUBBLE_DELETED = "bubble_deleted"; // New event for deletion
    
    // Static variables to maintain state across service restarts
    private static ReactApplicationContext reactContext;
    private static int lastBubbleX = 0;
    private static int lastBubbleY = 0;
    private static ContextMenuBubbleJoint ctxBubbleJoint;
    private static UdpLogger logger;

    /**
     * Constructor that initializes the module with React context
     * 
     * @param context The React application context
     */
    public FloatingBubbleModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        ctxBubbleJoint = new ContextMenuBubbleJoint();
        initializeLogger();
    }

    /**
     * Initialize UDP logger for debugging
     */
    private void initializeLogger() {
        try {
            logger = new UdpLogger("192.168.1.18", 9999);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize UDP logger: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the name of this module for React Native
     * 
     * @return The module name
     */
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    /**
     * Starts the floating bubble service with the provided configuration
     * 
     * @param config Configuration options for the bubble (icon, size, etc.)
     */
    @ReactMethod
    public void startBubble(ReadableMap config) {
        logInfo("Starting bubble service");
        try {
            Intent intent = new Intent(reactContext, FloatingBubbleService.class);
            
            // Pass configuration options to the service
            if (config != null) {
                if (config.hasKey("bubbleIcon")) {
                    intent.putExtra("bubbleIcon", config.getString("bubbleIcon"));
                }
                
                if (config.hasKey("bubbleSize")) {
                    intent.putExtra("bubbleSize", config.getInt("bubbleSize"));
                }
                
                // New configuration options for underlay
                if (config.hasKey("underlayColor")) {
                    intent.putExtra("underlayColor", config.getInt("underlayColor"));
                }
                
                // New configuration options for delete circle
                if (config.hasKey("deleteCircleColor")) {
                    intent.putExtra("deleteCircleColor", config.getInt("deleteCircleColor"));
                }
            }
            
            reactContext.startService(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error starting bubble service: " + e.getMessage(), e);
        }
    }

    /**
     * Stops the floating bubble service
     */
    @ReactMethod
    public void stopBubble() {
        logInfo("Stopping bubble service");
        try {
            Intent intent = new Intent(reactContext, FloatingBubbleService.class);
            reactContext.stopService(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error stopping bubble service: " + e.getMessage(), e);
        }
    }

    /**
     * Helper method to log information through UDP logger
     */
    private static void logInfo(String message) {
        if (logger != null) {
            logger.info(message);
        }
        Log.d(TAG, message);
    }

    /**
     * Service class that manages the floating bubble view
     */
    public static class FloatingBubbleService extends Service {
        private WindowManager windowManager;
        private View bubbleView;
        private View deleteCircleView;
        private WindowManager.LayoutParams params;
        private WindowManager.LayoutParams deleteParams;
        private UnderlayView underlayView;
        private Intent cachedIntent = null;
        private ContextMenu contextMenu = null;
        private static final int ANIMATION_DURATION = 300;
        private static final int DEFAULT_BUBBLE_SIZE = 150;
        private static final int DRAG_THRESHOLD = 5;
        private static final int EXPANDED_BUBBLE_SIZE = 64;
        private static final int DEFAULT_BUBBLE_SIZE_DP = 48;
        private static final int DELETE_CIRCLE_SIZE_DP = 64;
        private static final int INITIAL_Y_OFFSET = 150;
        private static final int DEFAULT_UNDERLAY_COLOR = Color.argb(20, 0, 0, 0);
        private static final int DEFAULT_DELETE_CIRCLE_COLOR = Color.argb(200, 255, 0, 0);
        private boolean isDeleteCircleVisible = false;
        private boolean isDraggingOverDeleteCircle = false;

        @Override
        public void onCreate() {
            super.onCreate();
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }

        /**
         * Creates and displays the floating bubble on the screen
         */
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            try {
                // Only create bubble if it doesn't already exist
                if (bubbleView == null) {
                    initializeBubble(intent);
                }
                
                // Initialize context menu if needed
                if (contextMenu == null) {
                    // Initialize with null since we'll use our UnderlayView class now
                    contextMenu = new ContextMenu(null);
                    ctxBubbleJoint.subscribe(contextMenu);
                }
                
                // Cache intent for later recreation
                cachedIntent = intent;
            } catch (Exception e) {
                Log.e(TAG, "Error in onStartCommand: " + e.getMessage(), e);
            }
            
            // START_STICKY ensures the service restarts if killed by the system
            return START_STICKY;
        }

        /**
         * Convert dp to pixels
         * 
         * @param dp The density-independent pixels value
         * @param context The context to get display metrics
         * @return The pixel value
         */
        public static float convertDpToPixel(float dp, Context context) {
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            return dp * (metrics.densityDpi / 160f);
        }

        /**
         * Scales the floating bubble to a new size using animation
         * 
         * @param newSizeDp The target size for the bubble in dp
         */
        private void scaleBubble(final int newSizeDp) {
            try {
                // Get current size in pixels from layout params
                final int currentSizePx = params.width;
                
                // Convert newSize from dp to pixels
                float newSizePxFloat = convertDpToPixel(newSizeDp, bubbleView.getContext());
                int newSizePx = (int) newSizePxFloat;
                
                // Log the size conversion for debugging
                logInfo("Scaling bubble from " + currentSizePx + "px to " + newSizePx + 
                       "px (" + newSizeDp + "dp)");
                
                // Create animator between pixel values
                ValueAnimator scaleAnimator = ValueAnimator.ofInt(currentSizePx, newSizePx);
                scaleAnimator.setDuration(ANIMATION_DURATION);
                scaleAnimator.setInterpolator(new DecelerateInterpolator());
                scaleAnimator.addUpdateListener(animation -> {
                    int currentValuePx = (int) animation.getAnimatedValue();
                    
                    // Update layout params with pixel values
                    params.width = currentValuePx;
                    params.height = currentValuePx;
                    
                    updateBubblePosition();
                });
                scaleAnimator.start();
            } catch (Exception e) {
                Log.e(TAG, "Error scaling bubble: " + e.getMessage(), e);
                logInfo("Bubble failed to scale");
            }
        }

        /**
         * Toggles the visibility of underlay view for bubble context menu
         */
        private void toggleBubbleClosingUnderlay() {
            // Use our new UnderlayView class
            if (underlayView == null) {
                // Create underlay if it doesn't exist
                underlayView = new UnderlayView(this);
                underlayView.setCoords(params.x, params.y);
                // Set up touch listener for the underlay
                underlayView.setOnUnderlayTouchListener(() -> {
                    // When user touches underlay, deactivate menu and hide underlay
                    if (contextMenu != null) {
                        contextMenu.setActive(false);
                    }
                    
                    hideDeleteCircle();
                    hideUnderlay();
                });
            }
            
            if (underlayView.isVisible()) {
                // Hide the underlay
                hideUnderlay();
                hideDeleteCircle();
            } else {
                // Show the underlay
                showUnderlay();
                showDeleteCircle();
            }
        }
        
        /**
         * Shows the underlay with current settings
         */
        private void showUnderlay() {
            // Get underlay color from intent if available
            int underlayColor = (cachedIntent != null && cachedIntent.hasExtra("underlayColor")) 
                    ? cachedIntent.getIntExtra("underlayColor", DEFAULT_UNDERLAY_COLOR)
                    : DEFAULT_UNDERLAY_COLOR;
            
            // Show the underlay with specified color
            underlayView.show(underlayColor);
            ctxBubbleJoint.underlayDisplayed = true;
            
            // Scale bubble to expanded size
            scaleBubble(EXPANDED_BUBBLE_SIZE);
            
            // Make sure bubble is on top of underlay
            removeBubbleView();
            initializeBubble(cachedIntent);
            
            logInfo("Underlay displayed, scaled bubble to " + EXPANDED_BUBBLE_SIZE + "dp");
        }
        
        /**
         * Hides the underlay
         */
        private void hideUnderlay() {
            if (underlayView != null) {
                underlayView.hide();
                ctxBubbleJoint.underlayDisplayed = false;
                
                // Scale back to normal size
                scaleBubble(DEFAULT_BUBBLE_SIZE_DP);
                logInfo("Underlay closed, scaled bubble to " + DEFAULT_BUBBLE_SIZE_DP + "dp");
            }
        }
        
        /**
         * Creates and shows the delete circle
         */
        private void showDeleteCircle() {
            if (deleteCircleView == null) {
                // Initialize delete circle
                initializeDeleteCircle();
            }
            
            if (!isDeleteCircleVisible) {
                // Make the delete circle visible with animation
                ValueAnimator fadeInAnimator = ValueAnimator.ofFloat(0f, 1f);
                fadeInAnimator.setDuration(ANIMATION_DURATION);
                fadeInAnimator.addUpdateListener(animation -> {
                    float alpha = (float) animation.getAnimatedValue();
                    deleteCircleView.setAlpha(alpha);
                });
                fadeInAnimator.start();
                
                // Add the view to window if not already added
                try {
                    windowManager.addView(deleteCircleView, deleteParams);
                    isDeleteCircleVisible = true;
                    logInfo("Delete circle displayed");
                } catch (Exception e) {
                    if (e instanceof IllegalStateException) {
                        // View is already added, just update it
                        try {
                            windowManager.updateViewLayout(deleteCircleView, deleteParams);
                        } catch (Exception ex) {
                            Log.e(TAG, "Error updating delete circle: " + ex.getMessage());
                        }
                    } else {
                        Log.e(TAG, "Error showing delete circle: " + e.getMessage());
                    }
                }
            }
        }

        /**
         * Hides the delete circle
         */
        private void hideDeleteCircle() {
            if (deleteCircleView != null && isDeleteCircleVisible) {
                try {
                    windowManager.removeView(deleteCircleView);
                    isDeleteCircleVisible = false;
                    logInfo("Delete circle hidden");
                } catch (Exception e) {
                    Log.e(TAG, "Error hiding delete circle: " + e.getMessage());
                }
            }
        }

        /**
         * Initializes the delete circle view
         */
        private void initializeDeleteCircle() {
            try {
                // Get delete circle color from intent or use default
                int deleteCircleColor = (cachedIntent != null && cachedIntent.hasExtra("deleteCircleColor")) 
                    ? cachedIntent.getIntExtra("deleteCircleColor", DEFAULT_DELETE_CIRCLE_COLOR)
                    : DEFAULT_DELETE_CIRCLE_COLOR;
                
                // Create the delete circle view from layout
                deleteCircleView = LayoutInflater.from(this).inflate(R.layout.delete_circle_layout, null);
                ImageView deleteIcon = deleteCircleView.findViewById(R.id.delete_icon);
                
                // Set background color for the delete circle
                View circleBackground = deleteCircleView.findViewById(R.id.delete_circle_background);
                circleBackground.setBackgroundColor(deleteCircleColor);
                
                // Calculate size in pixels
                int deleteCircleSizePx = (int) convertDpToPixel(DELETE_CIRCLE_SIZE_DP, this);
                
                // Create layout params for delete circle (centered at bottom)
                deleteParams = new WindowManager.LayoutParams(
                        deleteCircleSizePx,
                        deleteCircleSizePx,
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT);
                
                // Position the delete circle at the bottom center of screen
                deleteParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                deleteParams.y = (int) convertDpToPixel(30, this); // 30dp from bottom
                
                // Make the delete circle initially invisible
                deleteCircleView.setAlpha(0f);
                
                logInfo("Delete circle initialized");
            } catch (Exception e) {
                Log.e(TAG, "Error initializing delete circle: " + e.getMessage());
            }
        }

        /**
         * Checks if the bubble is over the delete circle
         * 
         * @param bubbleX The bubble's X coordinate
         * @param bubbleY The bubble's Y coordinate
         * @return true if bubble is overlapping delete circle
         */
        private boolean isBubbleOverDeleteCircle(int bubbleX, int bubbleY) {
            if (!isDeleteCircleVisible) {
                return false;
            }
            
            // Get screen dimensions
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            
            // Calculate bubble center
            int bubbleCenterX = bubbleX + params.width / 2;
            int bubbleCenterY = bubbleY + params.height / 2;
            
            // Calculate delete circle center (it's at bottom center)
            int deleteCircleCenterX = metrics.widthPixels / 2;
            int deleteCircleCenterY = metrics.heightPixels - deleteParams.y - deleteParams.height / 2;
            
            // Calculate distance between centers
            double distance = Math.sqrt(
                Math.pow(bubbleCenterX - deleteCircleCenterX, 2) + 
                Math.pow(bubbleCenterY - deleteCircleCenterY, 2)
            );
            
            // Consider it overlapping if distance is less than sum of radii plus tolerance
            int tolerance = (int) convertDpToPixel(20, this); // 20dp tolerance
            int deleteRadius = deleteParams.width / 2;
            int bubbleRadius = params.width / 2;
            
            boolean isOverlapping = distance < (deleteRadius + bubbleRadius - tolerance);
            
            // Visual feedback when overlapping
            if (isOverlapping && !isDraggingOverDeleteCircle) {
                // Scale up delete circle to indicate active target
                scaleDeleteCircle(1.2f);
                isDraggingOverDeleteCircle = true;
            } else if (!isOverlapping && isDraggingOverDeleteCircle) {
                // Scale back to normal
                scaleDeleteCircle(1.0f);
                isDraggingOverDeleteCircle = false;
            }
            
            return isOverlapping;
        }
        
        /**
         * Scales the delete circle for visual feedback
         * 
         * @param scale The scale factor to apply
         */
        private void scaleDeleteCircle(float scale) {
            if (deleteCircleView != null) {
                ValueAnimator scaleAnimator = ValueAnimator.ofFloat(
                    deleteCircleView.getScaleX(), scale);
                scaleAnimator.setDuration(200);
                scaleAnimator.addUpdateListener(animation -> {
                    float currentScale = (float) animation.getAnimatedValue();
                    deleteCircleView.setScaleX(currentScale);
                    deleteCircleView.setScaleY(currentScale);
                });
                scaleAnimator.start();
            }
        }

        /**
         * Handles bubble deletion
         */
        private void deleteBubble() {
            try {
                // Animate the bubble shrinking before removal
                ValueAnimator shrinkAnimator = ValueAnimator.ofFloat(1f, 0f);
                shrinkAnimator.setDuration(300);
                shrinkAnimator.addUpdateListener(animation -> {
                    float scale = (float) animation.getAnimatedValue();
                    if (bubbleView != null) {
                        bubbleView.setScaleX(scale);
                        bubbleView.setScaleY(scale);
                    }
                });
                shrinkAnimator.start();
                
                // Send event to React Native
                sendEventToReactNative(EVENT_BUBBLE_DELETED, null);
                
                // Delay the actual removal to allow animation to complete
                bubbleView.postDelayed(() -> {
                    hideUnderlay();
                    hideDeleteCircle();
                    stopSelf(); // Stop the service
                }, 300);
                
                logInfo("Bubble deleted");
            } catch (Exception e) {
                Log.e(TAG, "Error deleting bubble: " + e.getMessage());
            }
        }

        /**
         * Initializes the bubble view and its parameters
         * 
         * @param intent The intent containing bubble configuration
         */
        private void initializeBubble(Intent intent) {
            try {
                // Get parameters from intent with defaults
                int bubbleSize = (intent != null) ? 
                        intent.getIntExtra("bubbleSize", DEFAULT_BUBBLE_SIZE) : 
                        DEFAULT_BUBBLE_SIZE;
                
                // Create the bubble view from layout
                bubbleView = LayoutInflater.from(this).inflate(R.layout.bubble_layout, null);
                ImageView bubbleImage = bubbleView.findViewById(R.id.bubble_image);
                
                // Set bubble image if provided
                if (intent != null && intent.hasExtra("bubbleIcon")) {
                    String iconName = intent.getStringExtra("bubbleIcon");
                    int resourceId = getResources().getIdentifier(
                            iconName, "drawable", getPackageName());
                    if (resourceId != 0) {
                        bubbleImage.setImageResource(resourceId);
                    }
                }
                
                // Configure the layout parameters for the overlay window
                params = createBubbleLayoutParams(bubbleSize);
                
                // Set initial position (using saved positions or defaults)
                params.x = lastBubbleX;
                params.y = lastBubbleY == 0 ? INITIAL_Y_OFFSET : lastBubbleY;

                // Set up touch listener for drag and click events
                bubbleView.setOnTouchListener(createBubbleTouchListener(bubbleSize));

                // Add the view to the window
                windowManager.addView(bubbleView, params);
                
                // Update context menu if it exists
                if (contextMenu != null) {
                    contextMenu.updateCoords(params.x, params.y);
                }
                
                updateBubblePosition();
            } catch (Exception e) {
                Log.e(TAG, "Error initializing bubble: " + e.getMessage(), e);
            }
        }

        /**
         * Creates the layout parameters for the bubble window
         * 
         * @param size The size of the bubble in pixels
         * @return WindowManager.LayoutParams configured for the bubble
         */
        private WindowManager.LayoutParams createBubbleLayoutParams(int size) {
            WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(
                    size,
                    size,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            parameters.gravity = Gravity.TOP | Gravity.START;
            return parameters;
        }

        /**
         * Creates a touch listener for handling bubble drag and click events
         * 
         * @param bubbleSize The size of the bubble for edge calculations
         * @return View.OnTouchListener for the bubble
         */
        private View.OnTouchListener createBubbleTouchListener(final int bubbleSize) {
            return new View.OnTouchListener() {
                private boolean isClick = true;
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;
                
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    try {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                // Record initial positions for drag calculation
                                isClick = true;
                                initialX = params.x;
                                initialY = params.y;
                                initialTouchX = event.getRawX();
                                initialTouchY = event.getRawY();
                                return true;
                                
                            case MotionEvent.ACTION_MOVE:
                                handleBubbleDrag(event);
                                return true;
                                
                            case MotionEvent.ACTION_UP:
                                handleBubbleRelease(event, bubbleSize);
                                return true;
                                
                            default:
                                return false;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in bubble touch listener: " + e.getMessage(), e);
                        return false;
                    }
                }
                
                /**
                 * Handles the bubble dragging motion and constrains position to screen bounds
                 * 
                 * @param event The motion event
                 */
                private void handleBubbleDrag(MotionEvent event) {
                    // Calculate distance moved
                    float movedX = event.getRawX() - initialTouchX;
                    float movedY = event.getRawY() - initialTouchY;
                    
                    // If moved significantly, not a click
                    if (Math.abs(movedX) > DRAG_THRESHOLD || Math.abs(movedY) > DRAG_THRESHOLD) {
                        isClick = false;
                    }
                    
                    // Calculate new position
                    int newX = initialX + (int) movedX;
                    int newY = initialY + (int) movedY;
                    
                    // Get screen dimensions using bubbleView's context
                    int screenWidth = getScreenWidth();
                    int screenHeight = getScreenHeight();
                    
                    // Constrain X to [0, screenWidth - bubbleSize]
                    params.x = Math.max(0, Math.min(newX, screenWidth - bubbleSize));
                    
                    // Constrain Y as well if needed (optional)
                    params.y = Math.max(0, Math.min(newY, screenHeight - bubbleSize));
                    
                    // Update the layout
                    updateBubblePosition();
                    
                    // Update context menu position
                    if (contextMenu != null) {
                        contextMenu.updateCoords(params.x, params.y);
                    }
                    
                    if (underlayView != null) {
                        underlayView.setCoords(params.x, params.y);
                    }
                    
                    // Check if bubble is over delete circle
                    if (isDeleteCircleVisible) {
                        isBubbleOverDeleteCircle(params.x, params.y);
                    }
                }

                /**
                 * Gets the screen width
                 * @return Width of the screen in pixels
                 */
                private int getScreenWidth() {
                    WindowManager windowManager = (WindowManager) bubbleView.getContext().getSystemService(Context.WINDOW_SERVICE);
                    DisplayMetrics metrics = new DisplayMetrics();
                    windowManager.getDefaultDisplay().getMetrics(metrics);
                    return metrics.widthPixels;
                }

                /**
                 * Gets the screen height
                 * @return Height of the screen in pixels
                 */
                private int getScreenHeight() {
                    WindowManager windowManager = (WindowManager) bubbleView.getContext().getSystemService(Context.WINDOW_SERVICE);
                    DisplayMetrics metrics = new DisplayMetrics();
                    windowManager.getDefaultDisplay().getMetrics(metrics);
                    return metrics.heightPixels;
                }
                
                /**
                 * Handles the release of the bubble (animation and click events)
                 * 
                 * @param event The motion event
                 * @param bubbleSize The size of the bubble for edge calculations
                 */
                private void handleBubbleRelease(MotionEvent event, int bubbleSize) {
                    if (bubbleView == null || !bubbleView.isAttachedToWindow()) {
                        return;
                    }
                    
                    // Check if we should delete the bubble
                    if (isDeleteCircleVisible && isBubbleOverDeleteCircle(params.x, params.y)) {
                        deleteBubble();
                        return;
                    }
                    
                    // Animate bubble to edge of screen
                    animateBubbleToEdge(bubbleSize);
                    
                    // Handle click event
                    if (isClick) {
                        handleBubbleClick();
                    }
                }
            };
        }

        /**
         * Updates the bubble position in the window and notifies components
         */
        private void updateBubblePosition() {
            if (bubbleView != null && bubbleView.isAttachedToWindow()) {
                try {
                    windowManager.updateViewLayout(bubbleView, params);
                    
                    // Update bubble joint position
                    DisplayMetrics metrics = new DisplayMetrics();
                    windowManager.getDefaultDisplay().getMetrics(metrics);
                    ctxBubbleJoint.setIsOnLeft(params.x < metrics.widthPixels / 2);
                    
                    // Save last position for service restarts
                    lastBubbleX = params.x;
                    lastBubbleY = params.y;
                } catch (IllegalArgumentException e) {
                    // This can happen if the view was already removed
                    Log.e(TAG, "Error updating bubble position: " + e.getMessage(), e);
                }
            }
        }

        /**
         * Animates the bubble to stick to the nearest edge of the screen
         * 
         * @param bubbleSize The size of the bubble
         */
        private void animateBubbleToEdge(int bubbleSize) {
            try {
                int screenWidth = getScreenWidth();
                int halfScreenWidth = screenWidth / 2;
                
                // Determine end position (left or right edge)
                final int startX = params.x;
                final int endX = params.x < halfScreenWidth ? 0 : screenWidth - bubbleSize;
                
                // Create and configure the animator
                ValueAnimator animator = ValueAnimator.ofInt(startX, endX);
                animator.setDuration(ANIMATION_DURATION);
                animator.setInterpolator(new DecelerateInterpolator(1.5f));
                
                // Update the bubble position during animation
                animator.addUpdateListener(animation -> {
                    try {
                        if (bubbleView != null && bubbleView.isAttachedToWindow()) {
                            params.x = (Integer) animation.getAnimatedValue();
                            updateBubblePosition();
                            if (underlayView != null) {
                                underlayView.setCoords(params.x, params.y);
                            }
                        } else {
                            animation.cancel();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error in edge animation: " + e.getMessage(), e);
                        animation.cancel();
                    }
                });
                
                animator.start();
                
                // Save final position for service restarts
                lastBubbleX = endX;
                lastBubbleY = params.y;
            } catch (Exception e) {
                Log.e(TAG, "Error animating bubble to edge: " + e.getMessage(), e);
            }
        }

        /**
         * Gets the width of the screen
         * 
         * @return Screen width in pixels
         */
        private int getScreenWidth() {
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            return size.x;
        }

        /**
         * Handles the bubble click event
         */
        private void handleBubbleClick() {
            try {
                // Send event to React Native
                sendEventToReactNative(EVENT_BUBBLE_CLICKED, null);
                
                if (underlayView != null && underlayView.isVisible()) {
                    launchMainApp();
                } else {
                    // Expand the bubble when clicked
                    scaleBubble(EXPANDED_BUBBLE_SIZE);
                }
                
                toggleBubbleClosingUnderlay();
            } catch (Exception e) {
                Log.e(TAG, "Error handling bubble click: " + e.getMessage(), e);
            }
        }

        /**
         * Launches the main application activity
         */
        private void launchMainApp() {
            try {
                String packageName = getApplicationContext().getPackageName();
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(launchIntent);
                    logInfo("Launched main app: " + packageName);
                } else {
                    Log.e(TAG, "Could not find launch intent for package: " + packageName);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch main activity: " + e.getMessage(), e);
            }
        }

        /**
         * Sends an event to the React Native JavaScript side
         * 
         * @param eventName The name of the event
         * @param params The parameters to send with the event
         */
        private void sendEventToReactNative(String eventName, @Nullable String params) {
            try {
                if (reactContext != null && reactContext.hasActiveCatalystInstance()) {
                    reactContext
                        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                        .emit(eventName, params);
                    
                    logInfo("Sent event to React Native: " + eventName);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending event to React Native: " + e.getMessage(), e);
            }
        }

        /**
         * Cleans up resources when the service is destroyed
         */
        @Override
        public void onDestroy() {
            super.onDestroy();
            
            if (ctxBubbleJoint != null) {
                //ctxBubbleJoint.unsubscribe();
            }
            
            removeBubbleView();
            removeDeleteCircleView();
            cleanupUnderlay();
            
            logInfo("Floating bubble service destroyed");
        }

        /**
         * Removes the bubble view from the window manager
         */
        private void removeBubbleView() {
            try {
                if (bubbleView != null) {
                    if (bubbleView.isAttachedToWindow()) {
                        windowManager.removeView(bubbleView);
                    }
                    bubbleView = null;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error removing bubble view: " + e.getMessage(), e);
            }
        }
        
        /**
         * Removes the delete circle view from the window manager
         */
        private void removeDeleteCircleView() {
            try {
                if (deleteCircleView != null) {
                    if (deleteCircleView.isAttachedToWindow()) {
                        windowManager.removeView(deleteCircleView);
                    }
                    deleteCircleView = null;
                    isDeleteCircleVisible = false;
                }
            } catch (Exception e) {
                Log.e(TAG, "Error removing delete circle view: " + e.getMessage(), e);
            }
        }
        
        /**
         * Cleans up the underlay view
         */
        private void cleanupUnderlay() {
            if (underlayView != null) {
                underlayView.hide();
                underlayView = null;
            }
        }

        /**
         * Binding method for the service (not used in this implementation)
         */
        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}