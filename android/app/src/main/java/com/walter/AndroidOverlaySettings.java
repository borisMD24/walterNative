package com.walter;

import android.os.Build;
import android.provider.Settings;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

public class AndroidOverlaySettings extends ReactContextBaseJavaModule {
    private final ReactApplicationContext reactContext;

    public AndroidOverlaySettings(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "AndroidOverlaySettings";
    }

    @ReactMethod
    public void canDrawOverlays(Promise promise) {
        try {
            boolean canDraw = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || 
                             Settings.canDrawOverlays(reactContext);
            promise.resolve(canDraw);
        } catch (Exception e) {
            promise.reject("ERROR", e.getMessage());
        }
    }
}