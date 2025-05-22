package com.walter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.flexbox.FlexboxLayout;

public class RoomThumbnail extends LinearLayout {
    private final ImageView imageView;
    private final TextView nameLabel;
    private final int id;
    private int radiusPx;
    private String imageUrl;
    public String name;

    public RoomThumbnail(Context context,
                         int id,
                         int radiusDp,
                         String imageUrl,
                         ViewGroup parentContainer,
                         FlexboxLayout.LayoutParams layoutParams) {
        super(context);
        validateInputs(id, radiusDp, parentContainer);
        this.id = id;
        this.radiusPx = dpToPx(radiusDp, context);
        this.imageUrl = imageUrl;

        setupContainer(parentContainer, layoutParams);
        this.imageView = createImageView();
        this.nameLabel = createNameLabel();
        loadImage();
    }

    public RoomThumbnail(Context context,
                         int id,
                         int radiusDp,
                         String imageUrl,
                         ViewGroup parentContainer,
                         String name) {
        this(context, id, radiusDp, imageUrl, parentContainer,
            new FlexboxLayout.LayoutParams(
                dpToPx(radiusDp, context) * 2,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        this.name = name;
        updateNameLabel();
    }

    private void validateInputs(int id, int radiusDp, ViewGroup parent) {
        if (radiusDp <= 0) throw new IllegalArgumentException("Radius must be positive");
        if (parent == null) throw new IllegalArgumentException("Parent cannot be null");
    }

    private void setupContainer(ViewGroup parent, FlexboxLayout.LayoutParams params) {
        // Set up LinearLayout with vertical orientation
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        
        // Adjust layout params for the new structure
        params.width = radiusPx * 2;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        setLayoutParams(params);
        parent.addView(this);
    }

    private ImageView createImageView() {
        ImageView iv = new ImageView(getContext());
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        // Create layout params for the circular image
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
            radiusPx * 2, 
            radiusPx * 2
        );
        imageParams.gravity = Gravity.CENTER_HORIZONTAL;
        
        addView(iv, imageParams);
        return iv;
    }

    private TextView createNameLabel() {
        TextView textView = new TextView(getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        textView.setTypeface(Typeface.DEFAULT);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        
        // Add some top margin to separate from the image
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.topMargin = dpToPx(4); // 4dp margin
        textParams.gravity = Gravity.CENTER_HORIZONTAL;
        
        addView(textView, textParams);
        return textView;
    }

    public void updateNameLabel() {
        if (nameLabel != null) {
            if (name != null && !name.trim().isEmpty()) {
                nameLabel.setText(name);
                nameLabel.setVisibility(VISIBLE);
            } else {
                nameLabel.setVisibility(GONE);
            }
        }
    }

    private void loadImage() {
        Glide.with(getContext())
            .load(imageUrl)
            .apply(new RequestOptions()
                .override(radiusPx * 2, radiusPx * 2)
                .transform(new CircleCrop())
                .placeholder(R.drawable.kitchen)
                .error(R.drawable.bedroom))
            .into(imageView);
    }

    // Method to update the room name after creation
    public void setName(String name) {
        this.name = name;
        updateNameLabel();
    }

    public String getName() {
        return name;
    }

    // Method to customize label appearance
    public void setLabelTextSize(float textSizeSp) {
        if (nameLabel != null) {
            nameLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        }
    }

    public void setLabelTextColor(int color) {
        if (nameLabel != null) {
            nameLabel.setTextColor(color);
        }
    }

    // Méthode d'instance (facultative si utilisée ailleurs dans la classe)
    private int dpToPx(int dp) {
        return dpToPx(dp, getContext());
    }

    // Version statique utilisée dans le constructeur
    private static int dpToPx(int dp, Context context) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public void destroy() {
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) parent.removeView(this);
        Glide.with(getContext()).clear(imageView);
    }
}