package com.walter;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.walter.RoomSelection;
import com.walter.Brightness;
import com.walter.ContextMenuContext;

public class ContextMenuView {
    public int x;
    public int y;
    private final Context context;
    private int bubbleSize;
    private final ContextMenuItem[] items;
    private boolean isFirstShow = true;
    public ContextMenuContext menuContext;
    public ContextMenuView(Context context, ViewGroup underlay) {
        this.context = context;
        this.menuContext = new ContextMenuContext(this.context);
        // Initialize items
        this.items = new ContextMenuItem[3];
        items[0] = new ThemeSelection(context, underlay, 0, menuContext);
        items[1] = new Brightness(context, underlay, 1, menuContext);
        items[2] = new RoomSelection(context, underlay, 2, menuContext);
        
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.TOP | Gravity.START;
        
        if (!(underlay instanceof FrameLayout)) {
            throw new IllegalArgumentException("Underlay must be a FrameLayout");
        }
        
        FrameLayout frame = (FrameLayout) underlay;
        
        // Set initial coordinates without animation
        setCoords(0, 0);
    }
    
    /**
     * Updates the position of menu items without animation
     * This is used for subsequent position updates
     */
    public void setCoords(int x, int y) {
        menuContext.setBubbleCoords(x, y);
        // When first showing, we'll animate via showAtPosition instead
        
    }
    
    /**
     * Shows the menu at the given position with animation
     * This should be called when the underlay is first shown
     */
    public void showAtPosition(int x, int y) {
        menuContext.setBubbleX(x);
        menuContext.setBubbleY(y);
        
        // Start the animation from bubble position for each item
        for (ContextMenuItem item : items) {
            item.startAnim();
        }
        isFirstShow = false;
    }
    
    public void show(){
        for (int i = 0; i < 3; i++) {
            items[i].startAnim();
        }
    }
    
    /**
     * Sets the size of the bubbles
     */
    public void setBubbleSize(int size) {
        this.bubbleSize = size;
        menuContext.setBubbleSize(size);
    }
    public void hide(){
        for (int i = 0; i < 3; i++) {
            items[i].hide();
        }
        menuContext.forceClose();
    }
    public void bubbleFixed(){
        this.menuContext.setBubbleFixed();
    }
}