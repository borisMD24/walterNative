import React from 'react';
import {
  Canvas,
  BackdropBlur,
  RoundedRect,
  LinearGradient,
  vec,
  Fill,
  Blur,
  Group,
  Paint,
  BackdropFilter
} from '@shopify/react-native-skia';

import {
  SkiaLayoutRoot,
  LayoutGrid,
  LayoutRow,
} from './components/skiaLayoutSystem';
import {themes} from './components/themes/neumorphic';
import {Button, RadialSlider} from './components/neumorphismKit';

export default function App() {
  const canvasWidth = 400;
  const canvasHeight = 800;
  const theme = themes.light;

  return (
    <Canvas style={{width: canvasWidth, height: canvasHeight}}>
      {/* 1. Glassmorphism background blur */}
      <BackdropBlur
        blur={20}
        clip={{
          x: 20,
          y: 20,
          width: canvasWidth - 40,
          height: 100,
        }}>
        {/* 2. Frosted glass rectangle */}
        <RoundedRect
          x={20}
          y={20}
          width={canvasWidth - 40}
          height={100}
          r={20}
          color="rgba(255,255,255,0.1)">
          <Fill color="rgba(255,255,255,0.05)" />
        </RoundedRect>
      </BackdropBlur>

      {/* 3. Main content */}
      <SkiaLayoutRoot canvasRect={{width: canvasWidth, height: canvasHeight}}>
        <Group layer={<Paint><BackdropFilter blur={10} /></Paint>}>
          <LayoutGrid columns={1} gap={0} padding={150} top={140}>
            <LayoutRow>
              <Button title="ça fonctionne" width={160} />
            </LayoutRow>
            <LayoutRow>
              <RadialSlider value={50} />
            </LayoutRow>
          </LayoutGrid>
        </Group>
      </SkiaLayoutRoot>
    </Canvas>
  );
}
