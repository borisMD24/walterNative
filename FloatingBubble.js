import React, { useEffect, useState } from 'react';
import { View, Button, Platform, Text, StyleSheet, Alert, NativeEventEmitter, NativeModules, Linking, ToastAndroid, AppState } from 'react-native';
import { startFloatingBubble, stopFloatingBubble } from './FloatingBubbleService';

const FloatingBubble = () => {
  const [hasPermission, setHasPermission] = useState(false);
  const [isActive, setIsActive] = useState(false);

  useEffect(() => {
    // Set up listener for bubble clicks
    let subscription;
    if (Platform.OS === 'android' && NativeModules.FloatingBubbleModule) {
      console.log("Setting up bubble click listener");
      const eventEmitter = new NativeEventEmitter(NativeModules.FloatingBubbleModule);
      subscription = eventEmitter.addListener(
        'bubble_clicked',
        () => {
          console.log('Bubble was clicked!');
          ToastAndroid.show('Bubble was clicked!', ToastAndroid.SHORT);
          // You can trigger actions here when the bubble is clicked
        }
      );

      // Check for permission on mount
      checkPermission();
    }

    // Set up AppState listener to hide bubble when app comes to foreground
    const handleAppStateChange = (nextAppState) => {
      console.log('App state changed to:', nextAppState);
      if (nextAppState === 'active') {
        // App has come to the foreground
        console.log('App is now active, hiding bubble');
        stopBubble();
      } else if (nextAppState === 'background' && hasPermission) {
        // App has gone to the background and we have permission
        console.log('App is now in background, showing bubble');
        startBubble();
      }
    };

    const appStateSubscription = AppState.addEventListener('change', handleAppStateChange);

    // Clean up subscriptions
    return () => {
      if (subscription) subscription.remove();
      appStateSubscription.remove();
      
      // Make sure to stop the bubble when unmounting
      if (isActive) {
        stopBubble();
      }
    };
  }, [hasPermission]); // Re-run if permission status changes

  const checkPermission = async () => {
    if (Platform.OS !== 'android') {
      return;
    }

    try {
      if (!NativeModules.AndroidOverlaySettings) {
        console.error("AndroidOverlaySettings module is not available");
        ToastAndroid.show('AndroidOverlaySettings module not available', ToastAndroid.LONG);
        return;
      }
      
      const canDraw = await NativeModules.AndroidOverlaySettings.canDrawOverlays();
      console.log("Permission check result:", canDraw);
      setHasPermission(canDraw);
      return canDraw;
    } catch (error) {
      console.error('Error checking overlay permission:', error);
      ToastAndroid.show('Error checking permission: ' + error.message, ToastAndroid.LONG);
      return false;
    }
  };

  const requestPermission = () => {
    Alert.alert(
      'Permission Required',
      'To use the floating bubble, you need to grant overlay permission',
      [
        { text: 'Cancel', style: 'cancel' },
        { 
          text: 'Open Settings', 
          onPress: () => {
            try {
              // Direct link to overlay settings on Android
              Linking.openSettings();
            } catch (error) {
              console.error("Error opening settings:", error);
              ToastAndroid.show('Error opening settings: ' + error.message, ToastAndroid.LONG);
            }
          } 
        }
      ]
    );
  };

  const startBubble = async () => {
    const permitted = await checkPermission();
    if (!permitted) {
      requestPermission();
      return;
    }

    try {
      startFloatingBubble({
        bubbleSize: 150,
        bubbleIcon: 'bubble_icon'
      });
      setIsActive(true);
      ToastAndroid.show('Floating bubble started', ToastAndroid.SHORT);
    } catch (error) {
      console.error("Error in startBubble:", error);
      ToastAndroid.show('Error starting bubble: ' + error.message, ToastAndroid.LONG);
    }
  };

  const stopBubble = () => {
    if (Platform.OS === 'android') {
      try {
        stopFloatingBubble();
        setIsActive(false);
        ToastAndroid.show('Floating bubble stopped', ToastAndroid.SHORT);
      } catch (error) {
        console.error("Error in stopBubble:", error);
        ToastAndroid.show('Error stopping bubble: ' + error.message, ToastAndroid.LONG);
      }
    }
  };

  // Function to manually control bubble visibility
  const toggleBubble = () => {
    if (isActive) {
      stopBubble();
    } else {
      startBubble();
    }
  };

  return (
    <></>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 10,
    backgroundColor: '#F5F5F5',
  },
  title: {
    fontSize: 10,
    fontWeight: 'bold',
    marginBottom: 20,
    color: '#333',
  },
  description: {
    fontSize: 16,
    textAlign: 'center',
    marginBottom: 10,
    color: '#666',
  },
  permissionStatus: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  buttonSpacer: {
    height: 10,
  },
  notSupported: {
    color: '#F44336',
    textAlign: 'center',
    marginVertical: 10,
    fontWeight: 'bold',
  },
  infoText: {
    fontSize: 14,
    textAlign: 'center',
    marginTop: 10,
    color: '#666',
    fontStyle: 'italic',
  }
});

export default FloatingBubble;