package com.walter;
import android.view.ViewGroup;
import android.content.Context;
import com.walter.ContextMenuItem; 
import androidx.core.app.NotificationCompat;
import android.widget.Toast;
import com.walter.BrightnessComponent;

public class Brightness extends ContextMenuItem {
    private BrightnessComponent brightnessComponent;
    
    Brightness(Context ctx, ViewGroup parent, int nth) {
        super(ctx, parent, nth);
        this.setIconID(R.drawable.brightnes);
        brightnessComponent = new BrightnessComponent(ctx, parent);
    }
    
    @Override
    public void click() {
        // Animation de feedback
        blueCircle.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    blueCircle.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100);
                }
            }).start();

        // Toggle visibility state with proper error handling
        try {
            if (!brightnessComponent.isVisible) {  // Using the field directly as it's defined in BrightnessComponent
                brightnessComponent.display();
            } else {
                brightnessComponent.undisplay();
            }
        } catch (Exception e) {
            Toast.makeText(ctx, "Error toggling brightness: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // Callback éventuel
        if (clickListener != null) {
            clickListener.onMenuItemClick(nth);
        }

        // Feedback immédiat
        Toast.makeText(ctx, "FROM ROOM", Toast.LENGTH_SHORT).show();
    }
    
    public void setCoords(int x, int y) {
        this.brightnessComponent.setCoords(x, y);
        this.brightnessComponent.setNormalizedX(this.getNormalizedX());
        super.setCoords(x, y);
    }
    
    public void bubbleFixed() {
        this.brightnessComponent.bubbleFixed();
    }
}