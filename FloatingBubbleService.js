import { NativeModules } from 'react-native';
const { FloatingBubbleModule } = NativeModules;

export const startFloatingBubble = (config) => {
  if (FloatingBubbleModule) {
    console.log("Starting floating bubble with config:", JSON.stringify(config));
    try {
      FloatingBubbleModule.startBubble(config);
      console.log("Floating bubble service started successfully");
    } catch (error) {
      console.error("Error starting floating bubble:", error);
    }
  } else {
    console.error("FloatingBubbleModule is not available!");
  }
};

export const stopFloatingBubble = () => {
  if (FloatingBubbleModule) {
    console.log("Stopping floating bubble");
    try {
      FloatingBubbleModule.stopBubble();
      console.log("Floating bubble service stopped successfully");
    } catch (error) {
      console.error("Error stopping floating bubble:", error);
    }
  } else {
    console.error("FloatingBubbleModule is not available!");
  }
};