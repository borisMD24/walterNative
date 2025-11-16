// components/NeumorphicPanel.js
import React from 'react';
import { RoundedRect, Shadow } from '@shopify/react-native-skia';
import { useNeumorphicStyle, useNeumorphicShadow } from './useNeumorphicStyle.js';
import { useSharedValue } from 'react-native-reanimated';

const NeumorphicPanel = ({ 
  x, 
  y, 
  width, 
  height, 
  borderRadius = 20, 
  theme = 'light',
  children 
}) => {
  const colors = useNeumorphicStyle(theme);
  const pressed = useSharedValue(0);
  const shadowProps = useNeumorphicShadow(pressed, {
    baseDx: 6,
    baseDy: 6,
    baseBlur: 12
  });

  return (
    <RoundedRect
      x={x}
      y={y}
      width={width}
      height={height}
      r={borderRadius}
      color={colors.background}
    >
      <Shadow
        dx={shadowProps.value.shadowDarkDx}
        dy={shadowProps.value.shadowDarkDy}
        blur={shadowProps.value.shadowDarkBlur}
        color={colors.shadowDark}
      />
      <Shadow
        dx={shadowProps.value.shadowLightDx}
        dy={shadowProps.value.shadowLightDy}
        blur={shadowProps.value.shadowLightBlur}
        color={colors.shadowLight}
      />
      {children}
    </RoundedRect>
  );
};

export default NeumorphicPanel;
