import { useRef, useEffect, useState, useCallback } from 'react';
import { View, findNodeHandle, UIManager, Dimensions, Platform } from 'react-native';

export const useLiveBoundingBox = (options = {}) => {
  const { 
    enabled = true, 
    throttleMs = 16, // ~60fps default
    onBoundingBoxChange,
    onError,
    includeMargins = false,
    precision = 1, // Round to nearest pixel by default
    autoStart = true,
    debounceMs = 0,
  } = options;

  const viewRef = useRef(null);
  const [boundingBox, setBoundingBox] = useState(null);
  const [isReady, setIsReady] = useState(false);
  const [error, setError] = useState(null);
  const [isActive, setIsActive] = useState(autoStart && enabled);
  
  const animationFrameId = useRef(null);
  const timeoutId = useRef(null);
  const lastMeasureTime = useRef(0);
  const previousBoundingBox = useRef(null);
  const retryCount = useRef(0);
  const isMounted = useRef(true);

  // Performance optimization: round values to reduce unnecessary updates
  const roundValue = useCallback((value) => {
    return Math.round(value / precision) * precision;
  }, [precision]);

  // Enhanced bounding box comparison
  const boundingBoxesEqual = useCallback((a, b) => {
    if (!a || !b) return false;
    return (
      roundValue(a.x) === roundValue(b.x) &&
      roundValue(a.y) === roundValue(b.y) &&
      roundValue(a.width) === roundValue(b.width) &&
      roundValue(a.height) === roundValue(b.height)
    );
  }, [roundValue]);

  // Debounced update function
  const debouncedUpdate = useCallback((newBoundingBox) => {
    if (debounceMs > 0) {
      if (timeoutId.current) {
        clearTimeout(timeoutId.current);
      }
      timeoutId.current = setTimeout(() => {
        if (isMounted.current) {
          setBoundingBox(newBoundingBox);
          onBoundingBoxChange?.(newBoundingBox);
        }
      }, debounceMs);
    } else {
      setBoundingBox(newBoundingBox);
      onBoundingBoxChange?.(newBoundingBox);
    }
  }, [debounceMs, onBoundingBoxChange]);

  const measureBoundingBox = useCallback(() => {
    if (!viewRef.current || !enabled || !isActive || !isMounted.current) return;

    const now = Date.now();
    if (now - lastMeasureTime.current < throttleMs) {
      // Schedule next measurement
      animationFrameId.current = requestAnimationFrame(measureBoundingBox);
      return;
    }

    try {
      const nodeHandle = findNodeHandle(viewRef.current);
      if (!nodeHandle) {
        retryCount.current++;
        if (retryCount.current < 10) {
          // Retry with exponential backoff
          setTimeout(() => {
            if (isMounted.current) {
              animationFrameId.current = requestAnimationFrame(measureBoundingBox);
            }
          }, Math.min(100 * Math.pow(2, retryCount.current), 1000));
        } else {
          const error = new Error('Failed to get node handle after multiple retries');
          setError(error);
          onError?.(error);
        }
        return;
      }

      // Reset retry count on successful node handle
      retryCount.current = 0;

      const measureMethod = includeMargins ? 'measureInWindow' : 'measure';
      
      if (measureMethod === 'measureInWindow') {
        UIManager.measureInWindow(nodeHandle, (pageX, pageY, width, height) => {
          if (!isMounted.current) return;

          const newBoundingBox = {
            x: roundValue(pageX),
            y: roundValue(pageY),
            width: roundValue(width),
            height: roundValue(height),
            timestamp: now,
          };

          if (!boundingBoxesEqual(previousBoundingBox.current, newBoundingBox)) {
            previousBoundingBox.current = newBoundingBox;
            debouncedUpdate(newBoundingBox);
            
            if (!isReady) {
              setIsReady(true);
            }
            if (error) {
              setError(null);
            }
          }

          lastMeasureTime.current = now;
          
          if (enabled && isActive && isMounted.current) {
            animationFrameId.current = requestAnimationFrame(measureBoundingBox);
          }
        });
      } else {
        UIManager.measure(nodeHandle, (x, y, width, height, pageX, pageY) => {
          if (!isMounted.current) return;

          const newBoundingBox = {
            x: roundValue(pageX),
            y: roundValue(pageY),
            width: roundValue(width),
            height: roundValue(height),
            localX: roundValue(x),
            localY: roundValue(y),
            timestamp: now,
          };

          if (!boundingBoxesEqual(previousBoundingBox.current, newBoundingBox)) {
            previousBoundingBox.current = newBoundingBox;
            debouncedUpdate(newBoundingBox);
            
            if (!isReady) {
              setIsReady(true);
            }
            if (error) {
              setError(null);
            }
          }

          lastMeasureTime.current = now;
          
          if (enabled && isActive && isMounted.current) {
            animationFrameId.current = requestAnimationFrame(measureBoundingBox);
          }
        });
      }
    } catch (err) {
      if (isMounted.current) {
        setError(err);
        onError?.(err);
      }
    }
  }, [enabled, isActive, throttleMs, includeMargins, boundingBoxesEqual, debouncedUpdate, roundValue, isReady, error, onError]);

  // Control functions
  const start = useCallback(() => {
    if (!isActive) {
      setIsActive(true);
      retryCount.current = 0;
    }
  }, [isActive]);

  const stop = useCallback(() => {
    if (isActive) {
      setIsActive(false);
      if (animationFrameId.current) {
        cancelAnimationFrame(animationFrameId.current);
        animationFrameId.current = null;
      }
      if (timeoutId.current) {
        clearTimeout(timeoutId.current);
        timeoutId.current = null;
      }
    }
  }, [isActive]);

  const reset = useCallback(() => {
    setBoundingBox(null);
    setIsReady(false);
    setError(null);
    previousBoundingBox.current = null;
    retryCount.current = 0;
  }, []);

  const measureOnce = useCallback(() => {
    return new Promise((resolve, reject) => {
      if (!viewRef.current) {
        reject(new Error('View ref is null'));
        return;
      }

      const nodeHandle = findNodeHandle(viewRef.current);
      if (!nodeHandle) {
        reject(new Error('Failed to get node handle'));
        return;
      }

      try {
        UIManager.measure(nodeHandle, (x, y, width, height, pageX, pageY) => {
          const boundingBox = {
            x: roundValue(pageX),
            y: roundValue(pageY),
            width: roundValue(width),
            height: roundValue(height),
            localX: roundValue(x),
            localY: roundValue(y),
            timestamp: Date.now(),
          };
          resolve(boundingBox);
        });
      } catch (err) {
        reject(err);
      }
    });
  }, [roundValue]);

  // Main effect for starting/stopping measurements
  useEffect(() => {
    if (enabled && isActive) {
      animationFrameId.current = requestAnimationFrame(measureBoundingBox);
    } else {
      if (animationFrameId.current) {
        cancelAnimationFrame(animationFrameId.current);
        animationFrameId.current = null;
      }
    }

    return () => {
      if (animationFrameId.current) {
        cancelAnimationFrame(animationFrameId.current);
      }
      if (timeoutId.current) {
        clearTimeout(timeoutId.current);
      }
    };
  }, [enabled, isActive, measureBoundingBox]);

  // Handle orientation changes and app state
  useEffect(() => {
    const subscription = Dimensions.addEventListener('change', () => {
      if (enabled && isActive) {
        // Clear previous box and force re-measurement
        previousBoundingBox.current = null;
        setTimeout(() => {
          if (isMounted.current) {
            measureBoundingBox();
          }
        }, 100); // Give time for layout to settle
      }
    });

    return () => subscription?.remove();
  }, [enabled, isActive, measureBoundingBox]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      isMounted.current = false;
      if (animationFrameId.current) {
        cancelAnimationFrame(animationFrameId.current);
      }
      if (timeoutId.current) {
        clearTimeout(timeoutId.current);
      }
    };
  }, []);

  return {
    ref: viewRef,
    boundingBox,
    isReady,
    error,
    isActive,
    start,
    stop,
    reset,
    measureOnce,
  };
};

// Usage example:
/*
const MyComponent = () => {
  const {
    ref,
    boundingBox,
    isReady,
    error,
    isActive,
    start,
    stop,
    reset,
    measureOnce,
  } = useLiveBoundingBox({
    enabled: true,
    throttleMs: 16,
    precision: 0.5,
    debounceMs: 50,
    includeMargins: false,
    onBoundingBoxChange: (bb) => console.log('Bounding box changed:', bb),
    onError: (err) => console.error('Measurement error:', err),
  });

  return (
    <View ref={ref}>
      {isReady && (
        <Text>
          Position: {boundingBox.x}, {boundingBox.y}
        </Text>
      )}
    </View>
  );
};
*/
