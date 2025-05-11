package com.walter

import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity
import com.facebook.react.ReactInstanceManager
import com.facebook.react.ReactRootView
import com.facebook.react.common.LifecycleState
import com.facebook.react.modules.core.DefaultHardwareBackBtnHandler
import com.facebook.soloader.SoLoader

/**
 * Activity that hosts an Android Fragment with a React Native component overlay
 */
class ReactNativeFragmentOverlayActivity : FragmentActivity(), DefaultHardwareBackBtnHandler {

    private lateinit var reactRootView: ReactRootView
    private lateinit var reactInstanceManager: ReactInstanceManager
    private lateinit var fragmentContainer: FrameLayout
    private lateinit var reactNativeContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SoLoader.init(this, /* native exopackage */ false)

        val mainContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        fragmentContainer = FrameLayout(this).apply {
            id = ViewGroup.generateViewId()
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        reactNativeContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        mainContainer.addView(fragmentContainer)
        mainContainer.addView(reactNativeContainer)
        setContentView(mainContainer)

        if (savedInstanceState == null) {
            addFragment()
        }

        initializeReactNative()
    }

    private fun addFragment() {
        val fragment = YourAndroidFragment()
        supportFragmentManager
            .beginTransaction()
            .add(fragmentContainer.id, fragment)
            .commit()
    }

    private fun initializeReactNative() {
        val app = application as MainApplication
        reactInstanceManager = app.reactNativeHost.reactInstanceManager

        reactRootView = ReactRootView(this).apply {
            val initialProps = Bundle().apply {}
            startReactApplication(
                reactInstanceManager,
                "OverlayComponent",
                initialProps
            )
        }
        reactNativeContainer.addView(reactRootView)
    }

    override fun onPause() {
        super.onPause()
        reactInstanceManager.onHostPause(this)
    }

    override fun onResume() {
        super.onResume()
        reactInstanceManager.onHostResume(this, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        reactInstanceManager.onHostDestroy(this)
        reactRootView.unmountReactApplication()
    }

    override fun onBackPressed() {
        reactInstanceManager.onBackPressed()
        // If you still want to potentially handle back presses yourself:
        // Uncomment the line below if you want the default behavior as fallback
        // super.onBackPressed()
    }

    override fun invokeDefaultOnBackPressed() {
        super.onBackPressed()
    }
}