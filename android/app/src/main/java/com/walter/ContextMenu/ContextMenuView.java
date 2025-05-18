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
    private final TextView coordsText;
    private int bubbleSize;
    private final ContextMenuItem[] items;
    private boolean isFirstShow = true;
    public ContextMenuContext menuContext;
    public ContextMenuView(Context context, ViewGroup underlay) {
        this.context = context;
        this.menuContext = new ContextMenuContext(this.context);
        // Initialize items
        this.items = new ContextMenuItem[3];
        items[0] = new ContextMenuItem(context, underlay, 0, menuContext);
        items[1] = new Brightness(context, underlay, 1, menuContext);
        items[2] = new RoomSelection(context, underlay, 2, menuContext);
        
        // Initialize coordinate display
        coordsText = new TextView(this.context);
        coordsText.setTextColor(0xFFFFFFFF);
        coordsText.setBackgroundColor(0x88000000);
        coordsText.setPadding(20, 10, 20, 10);
        coordsText.setTextSize(16);
        coordsText.setGravity(Gravity.CENTER);
        
        FrameLayout.LayoutParams textParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        textParams.gravity = Gravity.TOP | Gravity.START;
        
        if (!(underlay instanceof FrameLayout)) {
            throw new IllegalArgumentException("Underlay must be a FrameLayout");
        }
        
        FrameLayout frame = (FrameLayout) underlay;
        frame.addView(coordsText, textParams);
        
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
    
        // Update coordinates text
        coordsText.post(() -> {
            float halfTW = coordsText.getWidth() / 2f;
            coordsText.setX(x - halfTW);
            coordsText.setY(y + 10);
        });
        
        updateDisplay();
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
        
        // Update coordinates text
        coordsText.post(() -> {
            float halfTW = coordsText.getWidth() / 2f;
            coordsText.setX(x - halfTW);
            coordsText.setY(y + 10);
        });
        
        updateDisplay();
        isFirstShow = false;
    }
    
    /**
     * Updates the coordinates display text
     */
    private void updateDisplay() {
        coordsText.setText("X: " + x + " | Y: " + y);
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