//app.tsx
import React from 'react';
import { View, StyleSheet, Text } from 'react-native';
import { SkiaFluxCanvas, useSkiaFlux } from './hooks/skiaFlux.js'; // Adjust import path as needed
import RedRectangleController from './components/skiafluxtest/index.jsx';
import { Rect } from '@shopify/react-native-skia';
export default function App() {
  const skiaAPI = useSkiaFlux();

  const handleCanvasSetup = (canvas, Skia, store) => {
    console.log('SkiaFlux Canvas initialized');
    console.log('Canvas reference:', canvas);
    console.log('Skia API available:', !!Skia);
    console.log('Store instance:', store);
    
    // Your initialization logic here
    // Example: Add initial components to the store
    store.addComponent(<Rect
            x={5}
            y={50}
            width={500}
            height={55}
            color="rgba(255, 0, 0, 0.1)" // Semi-transparent red
          />, { uid: 'initial-component' });
  };

  return (
    <View style={styles.container}>
      <Text> coucou </Text>
      <RedRectangleController></RedRectangleController>
      <View style={styles.canvas}>

      <SkiaFluxCanvas
        style={styles.canvas}
        onSetup={handleCanvasSetup}
      />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
  },
  canvas: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    pointerEvents: 'none', // Makes canvas click-transparent
    
    zIndex: 1000, // Above other UI elements
  },
});