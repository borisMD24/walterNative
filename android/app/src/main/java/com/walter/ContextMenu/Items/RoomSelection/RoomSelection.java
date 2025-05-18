package com.walter;
import android.view.ViewGroup;
import android.content.Context;
import com.walter.ContextMenuItem; 

import androidx.core.app.NotificationCompat;
import android.widget.Toast;
import com.walter.ContextMenuContext;

public class RoomSelection extends ContextMenuItem {
    private boolean oppened = false;
    RoomSelection(Context ctx, ViewGroup parent, int nth, ContextMenuContext menuContext){
        super(ctx, parent, nth, menuContext);
        this.setIconID(R.drawable.kitchen);
    }
    @Override
public void click() {
    // Animation de feedback
    if(!oppened){
        // Don't declare a local variable with the same name as lambda parameter
        this.menuContext.oppened(this.nth, (nth) -> this.click());
        oppened = true;
    } else {
        this.menuContext.close(this.nth);
        oppened = false;
    }
    super.click();
}
    public void t(){

    }
}
