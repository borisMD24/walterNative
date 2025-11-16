// components/ThemeButtonIcon.js
import React from 'react';
import { Circle } from '@shopify/react-native-skia';
import { useNeumorphicStyle } from './useNeumorphicStyle.js';

const ThemeButtonIcon = ({ centerX, centerY, theme = 'light' }) => {
  const colors = useNeumorphicStyle(theme);

  return (
    <>
      <Circle
        cx={centerX - 4}
        cy={centerY - 4}
        r={1.5}
        color={colors.textColor}
      />
      <Circle
        cx={centerX + 4}
        cy={centerY - 4}
        r={1.5}
        color={colors.textColor}
      />
      <Circle
        cx={centerX - 4}
        cy={centerY + 4}
        r={1.5}
        color={colors.textColor}
      />
      <Circle
        cx={centerX + 4}
        cy={centerY + 4}
        r={1.5}
        color={colors.textColor}
      />
    </>
  );
};

export default ThemeButtonIcon;
