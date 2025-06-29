import React, { useState, useEffect } from 'react';
import { Dimensions } from 'react-native';
import { 
  Canvas, 
  Text, 
  Rect, 
  Circle, 
  useFont,
  Line
} from '@shopify/react-native-skia';

import { 
  LayoutColumn, 
  LayoutRow, 
  LayoutGrid, 
  AbsoluteLayout,
  FitBoxWrapper,
  ResponsiveSwitch,
  SkiaLayoutRoot,
  useSkiaLayoutDebug,
  measureText
} from './skiaLayoutSystem/layout.jsx';

const fontPath = require('../assets/Montserrat-VariableFont_wght.ttf');

const useCanvasSize = () => {
  const [size, setSize] = useState(Dimensions.get('window'));

  useEffect(() => {
    const onChange = ({ window }) => {
      setSize(window);
    };
    const sub = Dimensions.addEventListener('change', onChange);
    return () => sub.remove();
  }, []);

  return size;
};

const SkiaLayoutDemo = () => {
  const { width, height } = useCanvasSize();
  const font = useFont(fontPath, 16);
  const titleFont = useFont(fontPath, 24);
  const debugLayout = useSkiaLayoutDebug(true);

  if (!font || !titleFont) return null;

  const canvasRect = { x: 0, y: 0, width, height };
  const designRect = { x: 0, y: 0, width: 360, height: 640 };

  return (
    <Canvas style={{ width, height }}>
      <SkiaLayoutRoot canvasRect={canvasRect} debug={true}>
        <FitBoxWrapper src={designRect} dst={canvasRect} mode="contain">
          <LayoutColumn spacing={20} padding={16} align="center">
            <Text text="Skia Layout Demo" font={titleFont} color="#000000" />

            <LayoutRow spacing={12} justify="space-between" width={360}>
              <Rect width={60} height={40} color="#3498DB" rx={8} />
              <Rect width={60} height={40} color="#E74C3C" rx={8} />
              <Rect width={60} height={40} color="#2ECC71" rx={8} />
              <Rect width={60} height={40} color="#F39C12" rx={8} />
            </LayoutRow>

            <LayoutGrid rows={2} columns={2} gap={16} width={320} height={200}>
              <Rect width={150} height={90} color="#9B59B6" rx={12} />
              <Circle r={45} color="#1ABC9C" />
              <Rect width={150} height={90} color="#34495E" rx={12} />
              <Circle r={45} color="#E67E22" />
            </LayoutGrid>

            <LayoutColumn spacing={8} padding={12} align="start">
              <Text text="Layout Features:" font={font} color="#2C3E50" />
              <Text text="• Flexbox-like row/column layouts" font={font} color="#7F8C8D" />
              <Text text="• Grid positioning system" font={font} color="#7F8C8D" />
              <Text text="• Absolute positioning" font={font} color="#7F8C8D" />
              <Text text="• Responsive breakpoints" font={font} color="#7F8C8D" />
            </LayoutColumn>

            <ResponsiveSwitch
              breakpoints={{
                mobile: 0,
                tablet: 500,
                desktop: 800
              }}
              layouts={{
                mobile: (
                  <LayoutColumn spacing={8} align="center">
                    <Text text="Mobile Layout" font={font} color="#E74C3C" />
                    <Circle r={20} color="#E74C3C" />
                  </LayoutColumn>
                ),
                tablet: (
                  <LayoutRow spacing={16} justify="center">
                    <Text text="Tablet Layout" font={font} color="#3498DB" />
                    <Rect width={40} height={40} color="#3498DB" />
                  </LayoutRow>
                ),
                desktop: (
                  <LayoutGrid rows={1} columns={3} gap={12}>
                    <Text text="Desktop" font={font} color="#2ECC71" />
                    <Rect width={60} height={30} color="#2ECC71" />
                    <Circle r={15} color="#2ECC71" />
                  </LayoutGrid>
                )
              }}
              fallback={
                <Text text="Default Layout" font={font} color="#95A5A6" />
              }
            />
          </LayoutColumn>

        </FitBoxWrapper>
      </SkiaLayoutRoot>
    </Canvas>
  );
};

export { SkiaLayoutDemo };
