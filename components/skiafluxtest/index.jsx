import React, { useRef, useState, useCallback, useEffect } from 'react';
import { 
  View, 
  PanResponder, 
  TouchableOpacity, 
  Text, 
  StyleSheet, 
  Dimensions 
} from 'react-native';
import { Rect } from '@shopify/react-native-skia';
import { useSkiaFlux } from '../../hooks/skiaFlux.js';

const { width: SCREEN_WIDTH, height: SCREEN_HEIGHT } = Dimensions.get('window');

// Wireframe rectangle dimensions
const WIREFRAME_WIDTH = 20;
const WIREFRAME_HEIGHT = 20;

export default function DraggableDotWireframe() {
  const { addComponent, updateComponent, removeComponent } = useSkiaFlux();
  const wireframeId = useRef(null);
  
  // Dot position state
  const [dotPosition, setDotPosition] = useState({
    x: SCREEN_WIDTH / 2 - 10, // Center horizontally (10 = half dot size)
    y: SCREEN_HEIGHT / 2 - 10, // Center vertically
  });

  // ✅ FIXED: Helper to calculate wireframe position
  const calculateWireframePosition = useCallback((dotX, dotY) => {
    return {
      x: dotX + 10 - WIREFRAME_WIDTH / 2,
      y: dotY + 10 - WIREFRAME_HEIGHT / 2  // ✅ FIXED: Was using + instead of -
    };
  }, []);

  // ✅ FIXED: Create wireframe component from position
  const createWireframeComponent = useCallback((position) => {
    return (
      <Rect
        x={position.x}
        y={position.y}
        width={WIREFRAME_WIDTH}
        height={WIREFRAME_HEIGHT}
        color="rgba(0, 102, 255, 0.3)"
      />
    );
  }, []);

  // ✅ FIXED: Update wireframe with explicit position parameter
  const updateWireframe = useCallback((dotX, dotY) => {
    if (!wireframeId.current) return false;

    const wireframePos = calculateWireframePosition(dotX, dotY);
    const updatedComponent = createWireframeComponent(wireframePos);

    const success = updateComponent(wireframeId.current, {
      component: updatedComponent,
      metadata: {
        type: 'wireframe',
        position: wireframePos,
        updatedAt: Date.now()
      }
    });

    if (!success) {
      console.warn('Failed to update wireframe');
    }

    return success;
  }, [calculateWireframePosition, createWireframeComponent, updateComponent]);

  // ✅ FIXED: Create wireframe with explicit position parameter
  const createWireframe = useCallback((dotX, dotY) => {
    // Clear existing wireframe
    if (wireframeId.current) {
      removeComponent(wireframeId.current);
    }

    const wireframePos = calculateWireframePosition(dotX, dotY);
    const wireframeComponent = createWireframeComponent(wireframePos);

    try {
      const id = addComponent(wireframeComponent, {
        uid: `wireframe-${Date.now()}`,
        zIndex: 100,
        metadata: {
          type: 'wireframe',
          position: wireframePos,
          createdAt: Date.now()
        }
      });

      wireframeId.current = id;
      console.log('Created wireframe at:', wireframePos);
      return id;
    } catch (error) {
      console.error('Error creating wireframe:', error);
      return null;
    }
  }, [addComponent, removeComponent, calculateWireframePosition, createWireframeComponent]);

  // Clear wireframe
  const clearWireframe = useCallback(() => {
    if (wireframeId.current) {
      const success = removeComponent(wireframeId.current);
      if (success) {
        wireframeId.current = null;
        console.log('Cleared wireframe');
      } else {
        console.warn('Failed to remove wireframe');
      }
    }
  }, [removeComponent]);

  // ✅ FIXED: Pan responder with current position updates
  const panResponder = useRef(
    PanResponder.create({
      onMoveShouldSetPanResponder: () => true,
      onPanResponderGrant: () => {
        // Optional: Add visual feedback when drag starts
      },
      onPanResponderMove: (event, gestureState) => {
        const newX = Math.max(0, Math.min(SCREEN_WIDTH - 20, gestureState.moveX - 10));
        const newY = Math.max(0, Math.min(SCREEN_HEIGHT - 20, gestureState.moveY - 10));
        
        setDotPosition({ x: newX, y: newY });
        
        // ✅ FIXED: Update wireframe with current position, not stale closure
        if (wireframeId.current) {
          updateWireframe(newX, newY);
        }
      },
      onPanResponderRelease: () => {
        // Optional: Add end-of-drag logic here
      },
    })
  ).current;

  // ✅ FIXED: Cleanup on unmount
  useEffect(() => {
    return () => {
      if (wireframeId.current) {
        removeComponent(wireframeId.current);
      }
    };
  }, [removeComponent]);

  // ✅ REMOVED: Duplicate useEffect that caused conflicts
  // The wireframe now updates only during drag in onPanResponderMove

  // ✅ FIXED: Button handlers that use current state
  const handleShowWireframe = useCallback(() => {
    createWireframe(dotPosition.x, dotPosition.y);
  }, [dotPosition, createWireframe]);

  const handleHideWireframe = useCallback(() => {
    clearWireframe();
  }, [clearWireframe]);

  return (
    <View style={styles.container}>
      {/* Draggable Dot */}
      <View
        style={[
          styles.dot,
          {
            left: dotPosition.x,
            top: dotPosition.y,
          }
        ]}
        {...panResponder.panHandlers}
      >
        <View style={styles.dotInner} />
      </View>

      {/* Control Panel */}
      <View style={styles.controlPanel}>
        <Text style={styles.title}>Draggable Dot & Wireframe</Text>
        <Text style={styles.positionText}>
          Position: ({dotPosition.x.toFixed(0)}, {dotPosition.y.toFixed(0)})
        </Text>

        <View style={styles.buttons}>
          <TouchableOpacity 
            style={[styles.button, styles.primaryButton]} 
            onPress={handleShowWireframe}
          >
            <Text style={styles.buttonText}>Show Wireframe</Text>
          </TouchableOpacity>

          <TouchableOpacity 
            style={[styles.button, styles.secondaryButton]} 
            onPress={handleHideWireframe}
          >
            <Text style={styles.buttonText}>Hide Wireframe</Text>
          </TouchableOpacity>
        </View>

        <Text style={styles.instructions}>
          Drag the dot around the screen{'\n'}
          Wireframe will follow the dot when active
        </Text>

        <View style={styles.debugInfo}>
          <Text style={styles.debugText}>
            Wireframe ID: {wireframeId.current || 'None'}
          </Text>
          <Text style={styles.debugText}>
            Wireframe Size: {WIREFRAME_WIDTH} × {WIREFRAME_HEIGHT}
          </Text>
          <Text style={styles.debugText}>
            Screen Size: {SCREEN_WIDTH.toFixed(0)} × {SCREEN_HEIGHT.toFixed(0)}
          </Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f8f9fa',
  },
  dot: {
    position: 'absolute',
    width: 20,
    height: 20,
    zIndex: 1, // Above other UI elements
  },
  dotInner: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#ff4444',
    borderWidth: 2,
    borderColor: '#ffffff',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  controlPanel: {
    position: 'absolute',
    top: 50,
    left: 20,
    right: 20,
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
    borderRadius: 12,
    padding: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 3,
  },
  title: {
    fontSize: 18,
    fontWeight: '700',
    color: '#333',
    textAlign: 'center',
    marginBottom: 8,
  },
  positionText: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    fontFamily: 'monospace',
    marginBottom: 16,
  },
  buttons: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 16,
  },
  button: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderRadius: 6,
    minWidth: 120,
  },
  primaryButton: {
    backgroundColor: '#0066ff',
  },
  secondaryButton: {
    backgroundColor: '#ff6600',
  },
  buttonText: {
    color: 'white',
    fontWeight: '600',
    textAlign: 'center',
    fontSize: 13,
  },
  instructions: {
    fontSize: 12,
    color: '#666',
    textAlign: 'center',
    fontStyle: 'italic',
    marginBottom: 12,
  },
  debugInfo: {
    backgroundColor: '#f0f0f0',
    borderRadius: 6,
    padding: 8,
  },
  debugText: {
    fontSize: 10,
    color: '#888',
    fontFamily: 'monospace',
    marginBottom: 1,
  },
});