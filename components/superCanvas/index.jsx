// ./components/SuperSkiaCanvas/index.jsx - Fixed Canvas Component with Y-Offset Correction
import React, { useState, useCallback, useRef, useEffect, useMemo, useContext } from 'react';
import { View, findNodeHandle, StatusBar, Platform } from 'react-native';
import { Canvas, Rect, Circle, Group } from '@shopify/react-native-skia';
import SuperSkiaCanvasContext from '../../hooks/SuperSkiaCanvasContext.js';

// Generate unique ID for canvas instances
const generateCanvasId = () => `canvas_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;

/**
 * Get the system UI offset (status bar height, etc.)
 */
const getSystemUIOffset = () => {
  if (Platform.OS === 'ios') {
    // iOS status bar is typically 44pt on newer devices, 20pt on older
    return StatusBar.currentHeight || 44;
  } else if (Platform.OS === 'android') {
    // Android status bar height varies by device
    return StatusBar.currentHeight || 24;
  }
  return 0;
};

/**
 * React Native Super Skia Canvas component
 */
export const SuperSkiaCanvas = ({
  canvasId: providedCanvasId,
  width,
  height,
  style = {},
  children
}) => {
  const canvasRef = useRef(null);
  const viewRef = useRef(null);
  const canvasId = useMemo(() => providedCanvasId || generateCanvasId(), [providedCanvasId]);
  
  // Get global context for canvas registration
  const globalContext = useContext(SuperSkiaCanvasContext);
  
  // Extract debug flag to prevent context instability
  const debugMode = useMemo(() => globalContext?.debug || false, [globalContext?.debug]);
  
  const [coordinates, setCoordinates] = useState({
    x: 0, y: 0, width: width || 0, height: height || 0
  });
  const [canvasChildren, setCanvasChildren] = useState([]);
  const [frameCount, setFrameCount] = useState(0);
  const [boundingRect, setBoundingRect] = useState({
    x: 0, y: 0, width: width || 0, height: height || 0,
    top: 0, left: 0, right: width || 0, bottom: height || 0
  });

  // Child management functions - stabilized without context dependencies
  const addChild = useCallback((child) => {
    setCanvasChildren(prev => {
      const filtered = prev.filter(c => c.id !== child.id);
      const newChildren = [...filtered, child].sort((a, b) => (a.zIndex || 0) - (b.zIndex || 0));
      
      if (debugMode) {
        console.log(`Child ${child.id} added to canvas ${canvasId}:`, child);
      }
      
      return newChildren;
    });
  }, [canvasId, debugMode]);

  const removeChild = useCallback((id) => {
    setCanvasChildren(prev => {
      const newChildren = prev.filter(c => c.id !== id);
      
      if (debugMode) {
        console.log(`Child ${id} removed from canvas ${canvasId}`);
      }
      
      return newChildren;
    });
  }, [canvasId, debugMode]);

  const updateChild = useCallback((id, updates) => {
    setCanvasChildren(prev => {
      const existingChild = prev.find(c => c.id === id);
      if (!existingChild) return prev;
      
      const hasChanges = Object.keys(updates).some(key => 
        existingChild[key] !== updates[key]
      );
      
      if (!hasChanges) return prev;
      
      const newChildren = prev.map(child => 
        child.id === id ? { ...child, ...updates } : child
      );
      
      if (debugMode) {
        console.log(`Child ${id} updated in canvas ${canvasId}:`, updates);
      }
      
      return newChildren;
    });
  }, [canvasId, debugMode]);

  const getChild = useCallback((id) => {
    return canvasChildren.find(child => child.id === id);
  }, [canvasChildren]);

  const forceUpdate = useCallback(() => {
    setFrameCount(prev => prev + 1);
  }, []);

  // FIXED: Measurement functions with proper coordinate correction
  const measureScreenCoordinates = useCallback(() => {
    return new Promise((resolve, reject) => {
      if (!viewRef.current) {
        reject(new Error('View ref not available'));
        return;
      }

      const nodeHandle = findNodeHandle(viewRef.current);
      if (!nodeHandle) {
        reject(new Error('Could not find node handle'));
        return;
      }

      // First get window coordinates (includes system UI offset)
      viewRef.current.measureInWindow((windowX, windowY, width, height) => {
        // Also get layout coordinates (relative to parent)
        viewRef.current.measure((layoutX, layoutY, layoutWidth, layoutHeight, pageX, pageY) => {
          // Calculate the corrected coordinates
          // Use pageX/pageY which are relative to the root view (app content area)
          const correctedX = pageX;
          const correctedY = pageY;
          
          const rect = {
            x: correctedX,
            y: correctedY,
            width: width,
            height: height,
            top: correctedY,
            left: correctedX,
            right: correctedX + width,
            bottom: correctedY + height,
            
            // Debug info - include both coordinate systems
            _debug: debugMode ? {
              windowCoords: { x: windowX, y: windowY },
              pageCoords: { x: pageX, y: pageY },
              layoutCoords: { x: layoutX, y: layoutY },
              systemUIOffset: getSystemUIOffset()
            } : undefined
          };
          
          setBoundingRect(prev => {
            const hasChanged = prev.x !== correctedX || prev.y !== correctedY || 
                             prev.width !== width || prev.height !== height;
            return hasChanged ? rect : prev;
          });

          if (debugMode) {
            console.log('Coordinate measurement debug:', rect._debug);
          }

          resolve(rect);
        });
      });
    });
  }, [debugMode]);

  const getClientBoundingRect = useCallback(async () => {
    try {
      return await measureScreenCoordinates();
    } catch (error) {
      console.warn('Failed to measure screen coordinates:', error);
      return boundingRect;
    }
  }, [measureScreenCoordinates, boundingRect]);

  const getBoundingRect = useCallback(() => {
    return boundingRect;
  }, [boundingRect]);

  const handleLayout = useCallback((event) => {
    const { width: layoutWidth, height: layoutHeight } = event.nativeEvent.layout;
    
    setCoordinates(prev => {
      if (prev.width === layoutWidth && prev.height === layoutHeight) {
        return prev;
      }
      return {
        x: 0,
        y: 0,
        width: layoutWidth,
        height: layoutHeight
      };
    });

    requestAnimationFrame(() => {
      measureScreenCoordinates()
        .then((rect) => {
          if (debugMode) {
            console.log('Screen coordinates updated:', rect);
          }
        })
        .catch((error) => {
          console.warn('Failed to update screen coordinates:', error);
        });
    });
  }, [measureScreenCoordinates, debugMode]);

  const updateScreenCoordinates = useCallback(async () => {
    try {
      const rect = await measureScreenCoordinates();
      if (debugMode) {
        console.log('Force update screen coordinates:', rect);
      }
      return rect;
    } catch (error) {
      console.warn('Failed to force update coordinates:', error);
      return boundingRect;
    }
  }, [measureScreenCoordinates, boundingRect, debugMode]);

  // CRITICAL FIX: Register canvas with minimal, stable dependencies
  useEffect(() => {
    if (!globalContext?.addCanvasInstance || !globalContext?.removeCanvasInstance) {
      return;
    }

    // Create canvas instance data inside effect to avoid dependency issues
    const canvasInstanceData = {
      id: canvasId,
      width,
      height,
      ref: canvasRef,
      viewRef,
      coordinates,
      boundingRect,
      children: canvasChildren,
      frameCount,
      addChild,
      removeChild,
      updateChild,
      getChild,
      forceUpdate,
      getClientBoundingRect,
      getBoundingRect,
      updateScreenCoordinates
    };

    globalContext.addCanvasInstance(canvasInstanceData);
    
    return () => {
      globalContext.removeCanvasInstance(canvasId);
    };
  }, [
    canvasId,
    width,
    height,
    // Only include primitive values and stable references
    coordinates.x,
    coordinates.y,
    coordinates.width,
    coordinates.height,
    boundingRect.x,
    boundingRect.y,
    boundingRect.width,
    boundingRect.height,
    canvasChildren.length, // Use length instead of full array
    frameCount
    // Deliberately exclude functions and globalContext to prevent loops
  ]);

  // Update coordinates when props change
  useEffect(() => {
    setCoordinates(prev => {
      const newWidth = width || 0;
      const newHeight = height || 0;
      
      if (prev.width === newWidth && prev.height === newHeight) {
        return prev;
      }
      
      return {
        x: 0,
        y: 0,
        width: newWidth,
        height: newHeight
      };
    });
  }, [width, height]);

  // Initial screen coordinates update
  useEffect(() => {
    const timer = setTimeout(() => {
      updateScreenCoordinates();
    }, 100);

    return () => clearTimeout(timer);
  }, [width, height]); // Removed updateScreenCoordinates dependency

  // Create local context value for this specific canvas
  const localContextValue = useMemo(() => ({
    canvasId,
    canvasRef,
    viewRef,
    coordinates,
    boundingRect,
    isOnScreen: true,
    children: canvasChildren,
    addChild,
    removeChild,
    updateChild,
    getChild,
    forceUpdate,
    getClientBoundingRect,
    getBoundingRect,
    updateScreenCoordinates,
    frameCount,
    debug: debugMode,
    
    // Include only stable parts of global context
    ...(globalContext && {
      canvasInstances: globalContext.canvasInstances,
      getCanvasInstance: globalContext.getCanvasInstance
    })
  }), [
    canvasId,
    coordinates,
    boundingRect,
    canvasChildren,
    addChild,
    removeChild,
    updateChild,
    getChild,
    forceUpdate,
    getClientBoundingRect,
    getBoundingRect,
    updateScreenCoordinates,
    frameCount,
    debugMode,
    globalContext?.canvasInstances,
    globalContext?.getCanvasInstance
  ]);

  // Debug render info
  if (debugMode) {
    console.log(`Canvas ${canvasId} rendering with ${canvasChildren.length} children:`, 
      canvasChildren.map(child => ({
        id: child.id,
        type: child.type,
        visible: child.visible,
        x: child.x,
        y: child.y,
        width: child.width,
        height: child.height,
        color: child.color
      }))
    );
  }

  return (
    <SuperSkiaCanvasContext.Provider value={localContextValue}>
      <View 
        ref={viewRef}
        style={[{ width, height }, style]}
        onLayout={handleLayout}
      >
        <Canvas
          ref={canvasRef}
          style={{ width, height }}
        >
          <Group>
            {canvasChildren.map(child => {
              if (!child.visible) return null;
              
              // Render different shapes based on child type
              if (child.type === 'rectangle') {
                return (
                  <Rect
                    key={child.id}
                    x={child.x || 0}
                    y={child.y || 0}
                    width={child.width || 50}
                    height={child.height || 50}
                    color={child.color || '#3b82f6'}
                    opacity={child.opacity || 1}
                  />
                );
              } else if (child.type === 'circle') {
                return (
                  <Circle
                    key={child.id}
                    cx={(child.x || 0) + (child.width || 50) / 2}
                    cy={(child.y || 0) + (child.height || 50) / 2}
                    r={(child.width || 50) / 2}
                    color={child.color || '#3b82f6'}
                    opacity={child.opacity || 1}
                  />
                );
              }
              return null;
            })}
          </Group>
        </Canvas>
        {children}
      </View>
    </SuperSkiaCanvasContext.Provider>
  );
};