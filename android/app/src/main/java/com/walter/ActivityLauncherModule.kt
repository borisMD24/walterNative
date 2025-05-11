package com.walter

import android.content.Intent
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

/**
 * Module to launch the Fragment Overlay Activity from React Native
 */
class ActivityLauncherModule(private val reactContext: ReactApplicationContext) : 
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = "ActivityLauncher"

    /**
     * Method to launch the ReactNativeFragmentOverlayActivity from JavaScript
     */
    @ReactMethod
    fun openFragmentOverlayActivity() {
        val intent = Intent(reactContext, ReactNativeFragmentOverlayActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        reactContext.startActivity(intent)
    }
}