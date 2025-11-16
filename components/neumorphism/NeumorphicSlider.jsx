// components/NeumorphicSlider.js
import React from 'react';
import { View } from 'react-native';
import { RoundedRect, Shadow, Circle } from '@shopify/react-native-skia';
import { GestureDetector, Gesture } from 'react-native-gesture-handler';
import {
  useSharedValue,
  useDerivedValue,
  withTiming,
  Easing,
  runOnJS,
  interpolate
} from 'react-native-reanimated';
import { useNeumorphicStyle } from './useNeumorphicStyle.js'
const NeumorphicSlider = ({
  x,
  y,
  width = 160,
  height = 16,
  thumbRadius = 12,
  value = 0.5,
  onValueChange,
  onPressStart,
  onPressEnd,
  theme = 'light',
  transitionDuration = 200
}) => {
  const colors = useNeumorphicStyle(theme);
  const progress = useSharedValue(value);
  const pressed = useSharedValue(false);

  const animatePress = (isPressed) => {
    'worklet';
    pressed.value = withTiming(isPressed ? 1 : 0, {
      duration: transitionDuration,
      easing: Easing.out(Easing.cubic),
    });
  };

  const sliderProps = useDerivedValue(() => {
    const thumbX = x + 8 + (progress.value * (width - 16));
    const pressProgress = pressed.value;
    
    return {
      trackX: x,
      trackY: y,
      thumbX,
      thumbY: y + height / 2,
      thumbShadowDx: interpolate(pressProgress, [0, 1], [3, 1]),
      thumbShadowDy: interpolate(pressProgress, [0, 1], [3, 1]),
      thumbShadowBlur: interpolate(pressProgress, [0, 1], [6, 3]),
      trackInsetShadowDx: 2,
      trackInsetShadowDy: 2,
      trackInsetShadowBlur: 4,
    };
  });

  // Expose methods for parent to call
  React.useImperativeHandle(onPressStart, () => ({
    handleTouchStart: () => {
      'worklet';
      animatePress(true);
    }
  }));

  React.useImperativeHandle(onPressEnd, () => ({
    handleTouchEnd: () => {
      'worklet';
      animatePress(false);
    }
  }));

  // Update progress from parent
  React.useEffect(() => {
    progress.value = value;
  }, [value, progress]);

  return (
    <>
      {/* Track (Inset) */}
      <RoundedRect
        x={sliderProps.value.trackX}
        y={sliderProps.value.trackY}
        width={width}
        height={height}
        r={height / 2}
        color={colors.background}
      >
        <Shadow
          dx={sliderProps.value.trackInsetShadowDx}
          dy={sliderProps.value.trackInsetShadowDy}
          blur={sliderProps.value.trackInsetShadowBlur}
          color={colors.shadowDark}
          inner
        />
        <Shadow
          dx={-sliderProps.value.trackInsetShadowDx}
          dy={-sliderProps.value.trackInsetShadowDy}
          blur={sliderProps.value.trackInsetShadowBlur}
          color={colors.shadowLight}
          inner
        />
      </RoundedRect>

      {/* Thumb */}
      <Circle
        cx={sliderProps.value.thumbX}
        cy={sliderProps.value.thumbY}
        r={thumbRadius}
        color={colors.background}
      >
        <Shadow
          dx={sliderProps.value.thumbShadowDx}
          dy={sliderProps.value.thumbShadowDy}
          blur={sliderProps.value.thumbShadowBlur}
          color={colors.shadowDark}
        />
        <Shadow
          dx={-sliderProps.value.thumbShadowDx}
          dy={-sliderProps.value.thumbShadowDy}
          blur={sliderProps.value.thumbShadowBlur}
          color={colors.shadowLight}
        />
      </Circle>
    </>
  );
};

export default NeumorphicSlider;
