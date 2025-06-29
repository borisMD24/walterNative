import React from 'react';
import { StyleSheet, View } from 'react-native';
import { Canvas, Text as SkiaText, matchFont } from '@shopify/react-native-skia';

export default function CustomText(props){
    
  const font = matchFont({
    fontFamily: 'sans-serif', // or 'System' for iOS, 'sans-serif' for Android
    fontSize: props.fontSize || 24,
    fontWeight: props.fontWeight || 'bold',
  });

  return(
    <Canvas style={[styles.canvas, props.style]}>
      <SkiaText
        x={props.x || 20}
        y={props.y || 30}
        text={props.children || props.text || ''}
        font={font}
        color={props.color || "#FF5722"}
      />
    </Canvas>
  );
}

const styles = StyleSheet.create({
  canvas: {
    width: 200,
    height: 50,
  },
});