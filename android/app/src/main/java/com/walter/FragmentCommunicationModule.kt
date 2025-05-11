package com.walter

import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * Native module to facilitate communication between React Native and the Android Fragment
 */
class FragmentCommunicationModule(private val reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {
        private const val TAG = "FragmentCommModule"
        // Ensure this tag matches the one used when adding the fragment
        private const val FRAGMENT_TAG = "OverlayFragment"
    }

    override fun getName(): String = "FragmentCommunication"

    /**
     * Method to send data from React Native to Fragment
     */
    @ReactMethod
    fun sendDataToFragment(data: String, promise: Promise) {
        try {
            val activity = currentActivity as? FragmentActivity
            if (activity is ReactNativeFragmentOverlayActivity) {
                // Find the fragment by tag
                val fragment = activity.supportFragmentManager
                    .findFragmentByTag(FRAGMENT_TAG) as? YourAndroidFragment

                fragment?.let {
                    // Call your fragment's method to handle the data
                    it.handleDataFromReactNative(data)

                    // Success response
                    val response = Arguments.createMap().apply {
                        putBoolean("success", true)
                        putString("message", "Data sent to Fragment successfully")
                    }
                    promise.resolve(response)
                } ?: run {
                    promise.reject("FRAGMENT_NOT_FOUND", "Target Fragment not found")
                }
            } else {
                promise.reject("INVALID_ACTIVITY", "Current activity is not ReactNativeFragmentOverlayActivity")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending data to Fragment", e)
            promise.reject("ERROR", "Failed to send data to Fragment: ${e.message}")
        }
    }

    /**
     * Method to send data from Fragment to React Native
     * Call this method from your Fragment to send data to React Native
     */
    fun sendEventToReactNative(eventName: String, params: WritableMap) {
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }
}
