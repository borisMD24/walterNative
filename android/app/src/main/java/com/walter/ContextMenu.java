package com.walter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ContextMenu {
    private static final String TAG = "ContextMenu";
    private boolean isActive = false;
    private final View underlayView;
    private Context underlayCtx;
    private int[] coords = new int[2];
    private SquareView squareView = null;
    private boolean isOnLeft;

    public ContextMenu(View underlayView) {
        this.underlayView = underlayView;
        if (underlayView != null) {
            this.underlayCtx = underlayView.getContext();
            Log.d(TAG, "ContextMenu initialized with valid underlayView");
        } else {
            Log.e(TAG, "WARNING: ContextMenu initialized with NULL underlayView");
        }
    }

    public void updateCoords(int x, int y) {
        Log.d(TAG, "updateCoords called: x=" + x + ", y=" + y);
        this.coords[0] = x;
        this.coords[1] = y;
        render();
    }

    public void setActive(boolean isActive) {
        Log.d(TAG, "setActive called: " + isActive);
        this.isActive = isActive;
        render();
    }

    private void render() {
    if (underlayView == null || !(underlayView instanceof ViewGroup)) return;
    ViewGroup parent = (ViewGroup) underlayView;

    if (isActive) {
        if (squareView != null) {
            removeFromParent(squareView);
            squareView = null;
        }

        try {
            squareView = new SquareView(underlayCtx, coords[0], coords[1]);
            int size = 300;

            DisplayMetrics metrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) underlayCtx.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(metrics);

            // PROPERLY DECLARE PARAMS VARIABLE
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            
            if (isOnLeft) {
                params.leftMargin = coords[0];
            } else {
                params.leftMargin = coords[0] - size;
            }
            params.topMargin = coords[1];

            // Ensure bounds checks
            params.leftMargin = Math.max(0, Math.min(params.leftMargin, metrics.widthPixels - size));
            params.topMargin = Math.max(0, Math.min(params.topMargin, metrics.heightPixels - size));

            parent.addView(squareView, params); // Now params is properly declared
            squareView.invalidate();
        } catch (Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage(), e);
        }
    } else {
        if (squareView != null) {
            removeFromParent(squareView);
            squareView = null;
        }
    }
}


    private void removeFromParent(View view) {
        if (view != null && view.getParent() instanceof ViewGroup) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    /**
 * Permet de positionner le menu à gauche ou à droite.
 * @param isOnLeft true si on veut aligner à gauche, false pour la droite
 */
public void setIsOnLeft(boolean isOnLeft) {
    Log.d(TAG, "setIsOnLeft called: " + isOnLeft);
    this.isOnLeft = isOnLeft;
    render();
}

    private static class SquareView extends View {
        private final int xCoord;
        private final int yCoord;
        private final Paint paint;
        private final Paint textPaint;

        public SquareView(Context context, int x, int y) {
            super(context);
            this.xCoord = x;
            this.yCoord = y;

            paint = new Paint();
            paint.setColor(Color.BLUE);

            textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(50);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
            String coordsText = "x=" + xCoord + ", y=" + yCoord;
            float textWidth = textPaint.measureText(coordsText);
            canvas.drawText(coordsText, (getWidth() - textWidth) / 2, getHeight() / 2, textPaint);
        }
    }
}
