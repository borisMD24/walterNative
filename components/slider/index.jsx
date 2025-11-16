import React, { useRef, useState, useEffect } from 'react';
import { SkiaFluxCanvas } from '../../hooks/skiaFlux';
import { View, PanResponder, StyleSheet } from 'react-native';
import { Rect } from '@shopify/react-native-skia';

export const SimpleSlider = ({ id = 'slider-1', onValueChange }) => {
  const containerRef = useRef(null);
  const [position, setPosition] = useState(0);
  const [isDragging, setIsDragging] = useState(false);
  const [containerLayout, setContainerLayout] = useState(null);

  const panResponder = useRef(
    PanResponder.create({
      onStartShouldSetPanResponder: () => true,
      onMoveShouldSetPanResponder: () => true,
      
      onPanResponderGrant: () => {
        setIsDragging(true);
      },
      
      onPanResponderMove: (_, gestureState) => {
        const trackWidth = 260; // 300 - 40 (container width - thumb width)
        const newPosition = Math.min(1, Math.max(0, gestureState.moveX / trackWidth));
        setPosition(newPosition);
        onValueChange?.(newPosition);
      },
      
      onPanResponderRelease: () => {
        setIsDragging(false);
      },
      
      onPanResponderTerminate: () => {
        setIsDragging(false);
      },
    })
  ).current;

  // Calculate thumb center position in container coordinates
  const getThumbCenterPosition = () => {
    const thumbLeftOffset = 20; // left margin
    const thumbWidth = 20;
    const thumbCenterX = thumbLeftOffset + (position * 260) + (thumbWidth / 2);
    const thumbCenterY = 20; // thumb top position + half height
    
    return { x: thumbCenterX, y: thumbCenterY };
  };

  // Red square component for SkiaFlux
  const RedSquareComponent = () => {
    const { x, y } = getThumbCenterPosition();
    const squareSize = 16;
    
    return (
      <Rect
        x={x - squareSize / 2}
        y={y - squareSize / 2}
        width={squareSize}
        height={squareSize}
        color="red"
        // Optional: add slight opacity variation based on drag state
        opacity={isDragging ? 0.8 : 1.0}
      />
    );
  };

  // Handle container layout to ensure proper positioning
  const handleContainerLayout = (event) => {
    setContainerLayout(event.nativeEvent.layout);
  };

  return (
    <View style={styles.container} ref={containerRef} onLayout={handleContainerLayout}>
      <View style={styles.track} />
      <View
        style={[
          styles.thumb,
          {
            transform: [{ translateX: position * 260 }],
            backgroundColor: isDragging ? '#005BB5' : '#007AFF',
          },
        ]}
        {...panResponder.panHandlers}
      />
      
      {/* SkiaFlux Canvas for red square overlay */}
      {containerLayout && (
        <SkiaFluxCanvas
          style={[StyleSheet.absoluteFillObject, { pointerEvents: 'none' }]}
          key={`${id}-skia-overlay`}
          components={[
            {
              id: `${id}-red-square`,
              component: RedSquareComponent,
              zIndex: 1000,
              visible: true,
            }
          ]}
        />
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    width: 300,
    height: 40,
    justifyContent: 'center',
    backgroundColor: '#eee',
    borderRadius: 20,
    margin: 20,
    position: 'relative',
  },
  track: {
    height: 4,
    backgroundColor: '#aaa',
    marginHorizontal: 20,
    borderRadius: 2,
  },
  thumb: {
    width: 20,
    height: 20,
    backgroundColor: '#007AFF',
    borderRadius: 10,
    position: 'absolute',
    top: 10,
    left: 20, // Account for track margin
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.2,
    shadowRadius: 2,
  },
});
