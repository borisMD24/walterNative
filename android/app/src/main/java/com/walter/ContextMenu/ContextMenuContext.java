package com.walter;

import java.util.function.Consumer;

public class ContextMenuContext {
    public int nthOppened = -1;
    private Consumer<Integer> onCloseCallback = null;
    
    ContextMenuContext() {
        // Default constructor
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
}