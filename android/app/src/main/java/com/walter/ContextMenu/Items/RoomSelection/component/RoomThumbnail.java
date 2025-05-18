package com.walter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

/**
 * Custom view that displays a circular room thumbnail with text label
 */
public class RoomThumbnail extends FrameLayout {
    // Constants
    private static final boolean DEBUG_LAYOUT = false;
    private static final String TAG = "RoomThumbnail";
    private static final int TEXT_MARGIN_BOTTOM_DP = 8;
    private static final int DEFAULT_PADDING_DP = 4;
    
    // Views
    private final ImageView imageView;
    private final TextView textView;
    
    // Properties
    private int radiusDp;
    private int radiusPx;
    private String imageUrl;
    private final ViewGroup parentContainer;
    private OnRoomSelectedListener onRoomSelectedListener;

    /**
     * Interface for room selection callback
     */
    public interface OnRoomSelectedListener {
        void onRoomSelected(String roomName);
    }

    /**
     * Creates a room thumbnail with an image and text
     *
     * @param context Android context
     * @param x X position in parent
     * @param y Y position in parent
     * @param name Text to display under the thumbnail
     * @param radiusDp Radius in dp
     * @param imageUrl URL of the image to load
     * @param parentContainer Parent ViewGroup
     */
    public RoomThumbnail(Context context,
                        int x,
                        int y,
                        String name,
                        int radiusDp,
                        String imageUrl,
                        ViewGroup parentContainer) {
        super(context);
        
        validateInputs(radiusDp, parentContainer);
        
        this.radiusDp = radiusDp;
        this.imageUrl = imageUrl;
        this.parentContainer = parentContainer;
        this.radiusPx = dpToPixels(radiusDp);

        setupContainer(x, y);
        this.imageView = createAndAddImageView();
        this.textView = createAndAddTextView(name);
        loadImage();
        setupDebugVisuals();
        setupClickHandling();
    }

    /**
     * Updates the thumbnail radius and refreshes the view
     * 
     * @param radiusDp New radius in dp
     */
    public void setRadius(int radiusDp) {
        if (radiusDp <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        
        this.radiusDp = radiusDp;
        this.radiusPx = dpToPixels(radiusDp);
        
        // Update layout dimensions
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = radiusPx * 2;
        params.height = radiusPx * 2;
        setLayoutParams(params);
        
        // Reload image with new dimensions
        loadImage();
    }

    /**
     * Set listener for room selection events
     * 
     * @param listener The listener to call when this room is selected
     */
    public void setOnRoomSelectedListener(OnRoomSelectedListener listener) {
        this.onRoomSelectedListener = listener;
    }

    /**
     * Get the name of the room
     * 
     * @return Room name as displayed in the label
     */
    public String getRoomName() {
        return textView.getText().toString();
    }

    /**
     * Validate constructor parameters
     */
    private void validateInputs(int radiusDp, ViewGroup parentContainer) {
        if (radiusDp <= 0) throw new IllegalArgumentException("Radius must be positive");
        if (parentContainer == null) throw new IllegalArgumentException("Parent cannot be null");
    }

    /**
     * Convert dp to pixels
     */
    private int dpToPixels(int dp) {
        final float density = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * density);
    }

    /**
     * Setup the container layout
     */
    private void setupContainer(int x, int y) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
            radiusPx * 2,
            radiusPx * 2
        );
        
        // Calculate margins to position the thumbnail correctly
        params.setMargins(
            x,
            y,
            0,
            0
        );

        setLayoutParams(params);
        parentContainer.addView(this);
        
        // Verify thumbnail is positioned correctly within parent bounds
        if (DEBUG_LAYOUT) {
            verifyPositioning();
        }
    }

    /**
     * Create and add the circular image view
     */
    private ImageView createAndAddImageView() {
        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        LayoutParams params = new LayoutParams(
            LayoutParams.MATCH_PARENT, 
            LayoutParams.MATCH_PARENT
        );
        
        addView(iv, params);
        return iv;
    }

    /**
     * Create and add the text label view
     */
    private TextView createAndAddTextView(String name) {
        TextView tv = new TextView(getContext());
        tv.setText(name);
        tv.setAllCaps(true);
        tv.setTextColor(Color.WHITE);
        tv.setShadowLayer(2f, 1f, 1f, Color.BLACK);
        
        LayoutParams params = new LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
        );
        params.bottomMargin = dpToPixels(TEXT_MARGIN_BOTTOM_DP);
        
        addView(tv, params);
        return tv;
    }

    /**
     * Load the image using Glide
     */
    private void loadImage() {
        // Clear any previous image loading requests to prevent overlap
        Glide.with(getContext()).clear(imageView);
        
        Glide.with(getContext())
             .load(imageUrl)
             .apply(createRequestOptions())
             // Remove crossfade to prevent overlap during transition
             // .transition(DrawableTransitionOptions.withCrossFade(CROSSFADE_DURATION_MS))
             .addListener(new ImageLoadListener())
             .into(imageView);
    }

    /**
     * Create request options for Glide
     */
    private RequestOptions createRequestOptions() {
        return new RequestOptions()
            .override(radiusPx * 2, radiusPx * 2)
            .transform(new CircleCrop())
            .placeholder(R.drawable.kitchen)
            .error(R.drawable.bedroom);
    }

    /**
     * Listener for Glide image loading
     */
    private class ImageLoadListener implements RequestListener<Drawable> {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                   Target<Drawable> target, boolean isFirstResource) {
            Log.e(TAG, "Image load failed for " + imageUrl, e);
            return false; // Let Glide handle the error drawable
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model,
                                      Target<Drawable> target, DataSource dataSource,
                                      boolean isFirstResource) {
            // Ensure placeholder is completely replaced
            imageView.setImageDrawable(resource);
            
            if (DEBUG_LAYOUT) {
                imageView.setBackgroundColor(Color.TRANSPARENT);
            }
            return true; // We've handled setting the resource
        }
    }

    /**
     * Verify the thumbnail is positioned correctly
     */
    private void verifyPositioning() {
        parentContainer.post(() -> {
            LayoutParams params = (LayoutParams) getLayoutParams();
            int parentWidth = parentContainer.getWidth();
            int parentHeight = parentContainer.getHeight();
            
            boolean isOutOfBounds = 
                params.leftMargin < 0 || 
                params.topMargin < 0 ||
                (params.leftMargin + getWidth()) > parentWidth ||
                (params.topMargin + getHeight()) > parentHeight;
                
            if (isOutOfBounds) {
                Log.w(TAG, String.format(
                    "Thumbnail positioning issue: thumbnail at (%d,%d) with size %dx%d in parent of size %dx%d",
                    params.leftMargin, params.topMargin,
                    getWidth(), getHeight(),
                    parentWidth, parentHeight
                ));
            }
        });
    }

    /**
     * Setup debug visuals for layout inspection
     */
    private void setupDebugVisuals() {
        if (DEBUG_LAYOUT) {
            setBackgroundColor(Color.argb(50, 255, 0, 0));
            imageView.setBackgroundColor(Color.argb(50, 0, 255, 0));
            setPadding(
                dpToPixels(DEFAULT_PADDING_DP),
                dpToPixels(DEFAULT_PADDING_DP),
                dpToPixels(DEFAULT_PADDING_DP),
                dpToPixels(DEFAULT_PADDING_DP)
            );
        }
    }

    /**
     * Setup click handling for the thumbnail
     */
    private void setupClickHandling() {
        setOnClickListener(v -> {
            String roomName = textView.getText().toString();
            Log.d(TAG, "Thumbnail clicked: " + roomName);
            
            if (onRoomSelectedListener != null) {
                onRoomSelectedListener.onRoomSelected(roomName);
            }
        });
    }

    /**
     * Cleans up resources and removes view from parent
     */
    public void destroy() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            parent.removeView(this);
        }
        Glide.with(getContext()).clear(imageView);
    }
}