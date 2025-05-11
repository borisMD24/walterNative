package com.walter;
import com.walter.ContextMenu;

public class ContextMenuBubbleJoint {
    public boolean isOnLeft;
    public boolean isActive;
    private ContextMenu ctxMenu;
    private boolean isSubscribed;
    public boolean underlayDisplayed;
    public ContextMenuBubbleJoint(){
        this.underlayDisplayed = false;
    }
    public void subscribe(ContextMenu ctxMenu){
        this.ctxMenu = ctxMenu;
        this.isSubscribed = true;
        this.update();
    }
    public void unsubscribe(ContextMenu menu) {
    if (this.ctxMenu == menu) {
        this.ctxMenu = null;
        this.isSubscribed = false;
    }
    }
    public void setIsOnLeft(boolean isOnLeft) {
        this.isOnLeft = isOnLeft;
        if(this.isSubscribed){
            this.update();
        }
    }
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
        if(this.isSubscribed){
            this.update();
        }
    }
    public void update(){
        this.ctxMenu.setActive(this.isActive);
        this.ctxMenu.setIsOnLeft(this.isOnLeft);
    }
}
