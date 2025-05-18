package com.walter;
import android.view.ViewGroup;
import android.content.Context;
import com.walter.ContextMenuItem; 
import androidx.core.app.NotificationCompat;
import android.widget.Toast;
import com.walter.BrightnessComponent;
import com.walter.ContextMenuContext;

public class Brightness extends ContextMenuItem {
    private BrightnessComponent brightnessComponent;
    
    Brightness(Context ctx, ViewGroup parent, int nth, ContextMenuContext menuContext) {
        super(ctx, parent, nth, menuContext);
        this.setIconID(R.drawable.brightnes);
        brightnessComponent = new BrightnessComponent(ctx, parent, menuContext );
    }
    
    @Override
    public void click() {
        // Animation de feedback

        // Toggle visibility state with proper error handling
        try {
            if (!brightnessComponent.isVisible) {  // Using the field directly as it's defined in BrightnessComponent
                brightnessComponent.display();
                this.menuContext.oppened(this.nth, (nth) -> this.brightnessComponent.undisplay());
            } else {
                this.menuContext.close(this.nth);
            }
        } catch (Exception e) {
            Toast.makeText(ctx, "Error toggling brightness: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        // Callback éventuel
        if (clickListener != null) {
            clickListener.onMenuItemClick(nth);
        }
        
        super.click();
    }
    
    public void setCoords() {
        super.setCoords();
    }
}