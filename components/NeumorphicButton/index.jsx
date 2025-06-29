// NeumorphicToggle.tsx
import React from 'react';
import { Canvas, RoundedRect, Shadow, Text, useFont, Circle } from '@shopify/react-native-skia';

import { themes } from "../themes/neumorphic.js";

// Le require est crucial ici
const fontPath = require("../../assets/Montserrat-VariableFont_wght.ttf");

// NeumorphicSwitch.tsx
export const NeumorphicSwitch = ({ on = false, width = 80, height = 40, theme = 'light' }) => {
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const r = height / 2;
  
  // Calculate padding needed for shadows
  // Using 3-sigma rule: blur of 8 extends ~24px, blur of 5 extends ~15px
  const maxBlur = 8;
  const shadowPadding = Math.ceil(maxBlur * 3); // 24px padding
  
  // Canvas dimensions with shadow padding
  const canvasWidth = width + (shadowPadding * 2);
  const canvasHeight = height + (shadowPadding * 2);
  
  // Offset positions to account for padding
  const offsetX = shadowPadding;
  const offsetY = shadowPadding;
  
  // Position du cercle (gauche ou droite) - adjusted for offset
  const cx = on ? offsetX + width - r - 5 : offsetX + r + 5;
  
  return (
    <Canvas style={{ width: canvasWidth, height: canvasHeight }}>
      <RoundedRect 
        x={offsetX} 
        y={offsetY} 
        width={width} 
        height={height} 
        r={r} 
        color={bg}
      >
        {/* Fond apparent */}
        <Shadow dx={4} dy={4} blur={8} color={shadowDark} />
        <Shadow dx={-4} dy={-4} blur={8} color={shadowLight} />
      </RoundedRect>
      
      <Circle 
        cx={cx} 
        cy={offsetY + height/2} 
        r={r - 5} 
        color={bg}
      >
        {/* Cercle intérieur pour le cadre du switch */}
        <Shadow dx={3} dy={3} blur={5} color={shadowDark} />
        <Shadow dx={-3} dy={-3} blur={5} color={shadowLight} />
      </Circle>
    </Canvas>
  );
};