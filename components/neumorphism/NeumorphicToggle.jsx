// components/NeumorphicToggle.js
import React from 'react';
import { View } from 'react-native';
import { RoundedRect, Shadow } from '@shopify/react-native-skia';
import { GestureDetector, Gesture } from 'react-native-gesture-handler';
import {
  useSharedValue,
  useDerivedValue,
  withTiming,
  withSpring,
  Easing,
  runOnJS,
  interpolate
} from 'react-native-reanimated';
import { useNeumorphicStyle } from './useNeumorphicStyle.js';
const NeumorphicToggle = ({
  x,
  y,
  width = 50,
  height = 24,
  thumbSize = 20,
  value = false,
  theme = 'light',
  transitionDuration = 200
}) => {
  const colors = useNeumorphicStyle(theme);
  const state = useSharedValue(value ? 1 : 0);
  const pressed = useSharedValue(false);

  const animatePress = (isPressed) => {
    'worklet';
    pressed.value = withTiming(isPressed ? 1 : 0, {
      duration: transitionDuration,
      easing: Easing.out(Easing.cubic),
    });
  };

  const toggleProps = useDerivedValue(() => {
    const pressProgress = pressed.value;
    const stateProgress = state.value;
    const thumbX = x + 2 + (stateProgress * (width - thumbSize - 4));
    
    return {
      trackX: x,
      trackY: y,
      thumbX,
      thumbY: y + 2,
      thumbShadowDx: interpolate(pressProgress, [0, 1], [2, 1]),
      thumbShadowDy: interpolate(pressProgress, [0, 1], [2, 1]),
      thumbShadowBlur: interpolate(pressProgress, [0, 1], [4, 2]),
    };
  });

  // Update state from parent
  React.useEffect(() => {
    state.value = withSpring(value ? 1 : 0, {
      damping: 15,
      stiffness: 200,
    });
  }, [value, state]);

  // Expose animation methods
  const handlePress = React.useCallback(() => {
    'worklet';
    animatePress(true);
    setTimeout(() => {
      'worklet';
      animatePress(false);
    }, 100);
  }, []);

  React.useImperativeHandle(null, () => ({
    handlePress
  }));

  return (
    <>
      {/* Track */}
      <RoundedRect
        x={toggleProps.value.trackX}
        y={toggleProps.value.trackY}
        width={width}
        height={height}
        r={height / 2}
        color={colors.background}
      >
        <Shadow
          dx={2}
          dy={2}
          blur={4}
          color={colors.shadowDark}
          inner
        />
        <Shadow
          dx={-2}
          dy={-2}
          blur={4}
          color={colors.shadowLight}
          inner
        />
      </RoundedRect>

      {/* Thumb */}
      <RoundedRect
        x={toggleProps.value.thumbX}
        y={toggleProps.value.thumbY}
        width={thumbSize}
        height={thumbSize}
        r={thumbSize / 2}
        color={colors.background}
      >
        <Shadow
          dx={toggleProps.value.thumbShadowDx}
          dy={toggleProps.value.thumbShadowDy}
          blur={toggleProps.value.thumbShadowBlur}
          color={colors.shadowDark}
        />
        <Shadow
          dx={-toggleProps.value.thumbShadowDx}
          dy={-toggleProps.value.thumbShadowDy}
          blur={toggleProps.value.thumbShadowBlur}
          color={colors.shadowLight}
        />
      </RoundedRect>
    </>
  );
};

export default NeumorphicToggle;
