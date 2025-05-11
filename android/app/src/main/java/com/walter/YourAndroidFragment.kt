package com.walter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

/**
 * Example Android Fragment that will be overlaid with a React Native component
 */
class YourAndroidFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // This is a simple example - replace with your actual Fragment UI
        return TextView(context).apply {
            text = "This is a native Android Fragment"
            textSize = 18f
            setPadding(40, 40, 40, 40)
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Additional setup for your fragment
        // You can access native functionality here
    }
    
    /**
     * Example method that can be called from React Native
     */
    fun handleDataFromReactNative(data: String) {
        // Handle data received from React Native
        // For example, update UI or trigger some action
    }
}