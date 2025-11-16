// components/NeumorphicButton.js
import React from 'react';
import { View } from 'react-native';
import { Circle, Shadow } from '@shopify/react-native-skia';
import { GestureDetector, Gesture } from 'react-native-gesture-handler';
import {
  useSharedValue,
  useDerivedValue,
  withTiming,
  Easing,
  runOnJS,
  interpolate
} from 'react-native-reanimated';
import { useNeumorphicStyle } from './useNeumorphicStyle.js';
const NeumorphicButton = ({
  x,
  y,
  radius = 15,
  theme = 'light',
  transitionDuration = 200,
  children
}) => {
  const colors = useNeumorphicStyle(theme);
  const pressed = useSharedValue(false);

  const animatePress = (isPressed) => {
    'worklet';
    pressed.value = withTiming(isPressed ? 1 : 0, {
      duration: transitionDuration,
      easing: Easing.out(Easing.cubic),
    });
  };

  const buttonProps = useDerivedValue(() => {
    const pressProgress = pressed.value;
    
    return {
      centerX: x,
      centerY: y,
      shadowDx: interpolate(pressProgress, [0, 1], [3, 1]),
      shadowDy: interpolate(pressProgress, [0, 1], [3, 1]),
      shadowBlur: interpolate(pressProgress, [0, 1], [6, 3]),
    };
  });

  // Expose animation methods
  const handlePress = React.useCallback(() => {
    'worklet';
    animatePress(true);
    setTimeout(() => {
      'worklet';
      animatePress(false);
    }, 150);
  }, []);

  React.useImperativeHandle(null, () => ({
    handlePress
  }));

  return (
    <>
      <Circle
        cx={buttonProps.value.centerX}
        cy={buttonProps.value.centerY}
        r={radius}
        color={colors.background}
      >
        <Shadow
          dx={buttonProps.value.shadowDx}
          dy={buttonProps.value.shadowDy}
          blur={buttonProps.value.shadowBlur}
          color={colors.shadowDark}
        />
        <Shadow
          dx={-buttonProps.value.shadowDx}
          dy={-buttonProps.value.shadowDy}
          blur={buttonProps.value.shadowBlur}
          color={colors.shadowLight}
        />
      </Circle>
      {children}
    </>
  );
};

export default NeumorphicButton;
