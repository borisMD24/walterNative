package com.walter;
import android.view.ViewGroup;
import android.content.Context;
import com.walter.ContextMenuItem; 
import com.walter.RoomSelectionComponent;

import androidx.core.app.NotificationCompat;
import android.widget.Toast;
import com.walter.ContextMenuContext;

public class RoomSelection extends ContextMenuItem {
    private boolean oppened = false;
    private RoomSelectionComponent roomSelectionCmpnt;
    RoomSelection(Context ctx, ViewGroup parent, int nth, ContextMenuContext menuContext){
        super(ctx, parent, nth, menuContext);
        roomSelectionCmpnt = new RoomSelectionComponent(ctx, parent, menuContext);
        this.setIconID(R.drawable.kitchen);
    }
    @Override
public void click() {
    // Animation de feedback
    if(!oppened){
        // Don't declare a local variable with the same name as lambda parameter
        this.roomSelectionCmpnt.display();
        this.menuContext.oppened(this.nth, (nth) -> {
            this.roomSelectionCmpnt.undisplay();
            this.oppened = false;
        });
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
