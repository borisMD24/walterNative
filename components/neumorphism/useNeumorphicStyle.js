// hooks/useNeumorphicStyle.js
import { useDerivedValue } from 'react-native-reanimated';
import { themes } from "../themes/neumorphic.js";

export const useNeumorphicStyle = (theme = 'light') => {
  const colors = themes[theme];
  
  return {
    background: colors.background,
    shadowLight: colors.shadowLight,
    shadowDark: colors.shadowDark,
    textColor: colors.text,
  };
};

export const useNeumorphicShadow = (pressed, config = {}) => {
  const {
    baseDx = 3,
    baseDy = 3,
    baseBlur = 6,
    pressedMultiplier = 0.3
  } = config;

  return useDerivedValue(() => ({
    shadowDarkDx: baseDx * (1 - pressed.value * pressedMultiplier),
    shadowDarkDy: baseDy * (1 - pressed.value * pressedMultiplier),
    shadowDarkBlur: baseBlur * (1 - pressed.value * pressedMultiplier),
    shadowLightDx: -baseDx * (1 - pressed.value * pressedMultiplier),
    shadowLightDy: -baseDy * (1 - pressed.value * pressedMultiplier),
    shadowLightBlur: baseBlur * (1 - pressed.value * pressedMultiplier),
  }));
};
