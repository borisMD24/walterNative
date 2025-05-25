package com.walter;

import java.util.function.Consumer;

import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.content.Context;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
public class ContextMenuContext {
    public int nthOppened = -1;
    private Consumer<Integer> onCloseCallback = null;
    private List<Runnable> onMoveCallbacks = new ArrayList<>();
    private List<Runnable> onBubbleResizeCallbacks = new ArrayList<>();
    private List<Runnable> onBubbleFixedCallbacks = new ArrayList<>();
    protected int bubbleY = 0;
    protected float normalizedBubbleY = 0;
    protected int bubbleX = 0;
    protected float normalizedBubbleX = 0;
    protected int screenHeight;
    protected int screenWidth;
    protected int bubbleSize = 200;
    protected int roomSelectionX;
    protected int roomSelectionY;
    protected int themeSelectionX;
    protected int themeSelectionY;
    private Context ctx;
    ContextMenuContext(Context ctx) {
        this.ctx = ctx;
        setScreenHeight();
        setScreenWidth();
    }
    
    public void oppened(int nth, Consumer<Integer> onClose) {
        //if oppened, close current and open new
        if (isOpen()) {
            // Store the old nth value before changing it
            int oldNth = this.nthOppened;
            // Execute the current callback if it exists
            if (this.onCloseCallback != null) {
                this.onCloseCallback.accept(oldNth);
            }
        }
        
        // Open the new one
        this.nthOppened = nth;
        //set on close lambda
        this.onCloseCallback = onClose;
    }
    public void onMove(Runnable callback){
        onMoveCallbacks.add(callback);
    }
    public void onBubbleResize(Runnable callback){
        onBubbleResizeCallbacks.add(callback);
    }
    public void onBubbleFix(Runnable callback){
        onBubbleFixedCallbacks.add(callback);
    }
    public void close(int nth) {
        if(this.nthOppened == nth) {
            //run on close lambda
            if (this.onCloseCallback != null) {
                this.onCloseCallback.accept(nth);
            }
            
            this.nthOppened = -1;
            //remove on close lambda
            this.onCloseCallback = null;
        }
    }
    
    // Optional helper method to check if menu is currently open
    public boolean isOpen() {
        return this.nthOppened != -1;
    }
    
    public void forceClose() {
        // Force close the currently open menu regardless of its nth value
        if (isOpen()) {
            // Execute the callback if it exists
            if (this.onCloseCallback != null) {
                this.onCloseCallback.accept(this.nthOppened);
            }
            
            // Reset the state
            this.nthOppened = -1;
            this.onCloseCallback = null;
        }
    }
    public void setScreenWidth() {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
    }
    public void setScreenHeight() {
        WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenHeight = displayMetrics.heightPixels;
    }
    public void setBubbleY(int y){
        bubbleY = y;
        float minY = bubbleSize;
        float maxY = screenHeight - bubbleSize;
        float clampedY = Math.max(minY, Math.min(bubbleY, maxY));
        normalizedBubbleY = (clampedY - minY) / (maxY - minY);
    }
    public void setBubbleSize(int size){
        bubbleSize = size;
        onBubbleResizeCallbacks.forEach(Runnable::run);
    }
    public void setBubbleX(int x){
        bubbleX = x;
        float minX = bubbleSize;
        float maxX = screenWidth - bubbleSize;
        float clampedX = Math.max(minX, Math.min(bubbleX, maxX));
        normalizedBubbleX = (clampedX - minX) / (maxX - minX);
    }
    public void setBubbleCoords(int x, int y){
        setBubbleX(x);
        setBubbleY(y);
        onMoveCallbacks.forEach(Runnable::run);
    }
    public void setBubbleFixed(){
        onBubbleFixedCallbacks.forEach(Runnable::run);
    }
    public void setRoomSelectionCoords(int x, int y){
        this.roomSelectionX = x;
        this.roomSelectionY = y;
    }
    public void setThemeSelectionCoords(int x, int y){
        this.themeSelectionX = x;
        this.themeSelectionY = y;
    }
}