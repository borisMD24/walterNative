package com.walter;
import android.view.ViewGroup;
import android.content.Context;
import com.walter.ContextMenuItem; 
import androidx.core.app.NotificationCompat;
import android.widget.Toast;
import com.walter.BrightnessComponent;



public class Brightness extends ContextMenuItem {
    BrightnessComponent brightnessComponent;
    Brightness(Context ctx, ViewGroup parent, int nth){
        super(ctx, parent, nth);
        this.setIconID(R.drawable.brightnes);
        brightnessComponent = new BrightnessComponent(ctx);
        brightnessComponent.setCurvature(-1f);
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

    // Envoi de notification
    sendNotification();

    // Callback éventuel
    if (clickListener != null) {
        clickListener.onMenuItemClick(nth);
    }

    // Feedback immédiat
    Toast.makeText(ctx, "FROM ROOM", Toast.LENGTH_SHORT).show();
}

}
