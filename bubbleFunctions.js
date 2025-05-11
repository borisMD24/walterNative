import React, { useEffect, useState } from 'react';
import { View, Button, Platform, Text, StyleSheet, Alert, NativeEventEmitter, NativeModules, Linking, ToastAndroid } from 'react-native';
import { startFloatingBubble, stopFloatingBubble } from './FloatingBubbleService';

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

    export {startBubble, stopBubble}