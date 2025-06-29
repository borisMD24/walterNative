// GlassTabBar.js
import React from 'react';
import { useWindowDimensions } from 'react-native';
import {
  Canvas,
  Path,
  BackdropBlur,
  Fill,
  Group,
  Skia,
} from '@shopify/react-native-skia';

const Nav = ({ height = 70, borderRadius = 30, marginHorizontal = 20 }) => {
  const { width } = useWindowDimensions();
  
  // Ensure all values are positive and valid
  const safeWidth = Math.max(1, width - 2 * marginHorizontal);
  const safeHeight = Math.max(1, height);
  const safeRadius = Math.max(0, Math.min(borderRadius, safeWidth / 2, safeHeight / 2));

  // Create a rounded rectangle path using Skia.Path
  const createRoundedRectPath = () => {
    const path = Skia.Path.Make();
    path.addRRect(
      Skia.RRectXY(
        Skia.XYWHRect(marginHorizontal, 0, safeWidth, safeHeight),
        safeRadius,
        safeRadius
      )
    );
    return path;
  };

  const roundedRectPath = createRoundedRectPath();

  return (
    <Canvas
      style={{
        position: 'absolute',
        bottom: 20,
        left: 0,
        right: 0,
        height: height,
      }}
    >
      <Group>
        {/* Beautiful rounded glass background */}
        <Path path={roundedRectPath}>
          <Fill color="rgba(255,255,255,0.15)" />
        </Path>
        
        {/* Backdrop blur with rounded clip for that premium glass effect */}
        <BackdropBlur blur={25} clip={roundedRectPath}>
          <Fill color="rgba(255,255,255,0.05)" />
        </BackdropBlur>
        
        {/* Subtle border highlight */}
        <Path path={roundedRectPath} style="stroke" strokeWidth={0.5}>
          <Fill color="rgba(255,255,255,0.3)" />
        </Path>
      </Group>
    </Canvas>
  );
};

export default Nav;