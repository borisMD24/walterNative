// SkiaFlux - FIXED infinite re-render issue

import { Canvas, CanvasRef, useCanvasRef, Skia } from '@shopify/react-native-skia';
import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';

/**
 * Creates a functional SkiaFlux store for managing Skia components
 */
const createSkiaFluxStore = (config = {}) => {
  // Private state
  let componentRegistry = new Map();
  let changeListeners = new Set();
  let canvasReference = null;
  let skiaAPI = null;
  let pendingUpdateQueue = new Set();
  let isCurrentlyRendering = false;
  
  const fluxConfiguration = {
    autoRender: true,
    batchUpdates: true,
    maxItems: 1000,
    ...config
  };

  // Utility functions
  const generateUniqueIdentifier = () => {
    const timestamp = Date.now();
    const randomSuffix = Math.random().toString(36).substr(2, 9);
    return `skia_${timestamp}_${randomSuffix}`;
  };

  const notifyAllListeners = () => {
    changeListeners.forEach(listenerFunction => {
      try {
        listenerFunction();
      } catch (error) {
        console.error('Error in SkiaFlux listener:', error);
      }
    });
  };

  const scheduleRenderOperation = () => {
    if (isCurrentlyRendering || !fluxConfiguration.batchUpdates) {
      if (!fluxConfiguration.batchUpdates) {
        executeRender();
      }
      return;
    }

    isCurrentlyRendering = true;
    
    requestAnimationFrame(() => {
      executeRender();
      pendingUpdateQueue.clear();
      isCurrentlyRendering = false;
    });
  };

  const validateComponent = (component) => {
    if (!component) {
      console.warn('SkiaFlux: Attempted to add null/undefined component');
      return false;
    }
    return true;
  };

  // Public API methods
  const addComponent = (component, options = {}) => {
    if (componentRegistry.size >= fluxConfiguration.maxItems) {
      throw new Error(`SkiaFlux: Maximum store capacity (${fluxConfiguration.maxItems}) reached`);
    }

    if (!validateComponent(component)) {
      throw new Error('SkiaFlux: Invalid component provided');
    }

    const componentUID = options.uid || generateUniqueIdentifier();
    
    if (componentRegistry.has(componentUID)) {
      throw new Error(`SkiaFlux: Component with UID "${componentUID}" already exists`);
    }

    const componentItem = {
      uid: componentUID,
      component: component,
      zIndex: 0,
      visible: true,
      metadata: {},
      createdAt: Date.now(),
      updatedAt: Date.now(),
      ...options
    };

    componentRegistry.set(componentUID, componentItem);
    
    notifyAllListeners();
    
    if (fluxConfiguration.autoRender) {
      scheduleRenderOperation();
    }

    return componentUID;
  };

  const updateComponent = (componentUID, updates) => {
    const existingItem = componentRegistry.get(componentUID);
    
    if (!existingItem) {
      console.warn(`SkiaFlux: Cannot update component "${componentUID}" - not found`);
      return false;
    }

    const updatedItem = {
      ...existingItem,
      ...updates,
      updatedAt: Date.now()
    };

    componentRegistry.set(componentUID, updatedItem);
    pendingUpdateQueue.add(componentUID);
    
    notifyAllListeners();

    if (fluxConfiguration.autoRender) {
      scheduleRenderOperation();
    }

    return true;
  };

  const removeComponent = (componentUID) => {
    const wasDeleted = componentRegistry.delete(componentUID);
    pendingUpdateQueue.delete(componentUID);
    
    if (wasDeleted) {
      notifyAllListeners();
      
      if (fluxConfiguration.autoRender) {
        scheduleRenderOperation();
      }
    } else {
      console.warn(`SkiaFlux: Cannot remove component "${componentUID}" - not found`);
    }

    return wasDeleted;
  };

  const getComponent = (componentUID) => {
    return componentRegistry.get(componentUID);
  };

  const getAllVisibleComponents = () => {
    return Array.from(componentRegistry.values())
      .filter(item => item.visible)
      .sort((a, b) => (a.zIndex || 0) - (b.zIndex || 0));
  };

  const getAllComponents = () => {
    return Array.from(componentRegistry.values())
      .sort((a, b) => (a.zIndex || 0) - (b.zIndex || 0));
  };

  const clearAllComponents = (skipRender = false) => {
    const hadComponents = componentRegistry.size > 0;
    
    componentRegistry.clear();
    pendingUpdateQueue.clear();
    
    if (hadComponents) {
      notifyAllListeners();
      
      if (!skipRender && fluxConfiguration.autoRender) {
        scheduleRenderOperation();
      }
    }
  };

  const setCanvasReference = (canvasRef, skiaInstance = Skia) => {
    canvasReference = canvasRef;
    skiaAPI = skiaInstance;
    console.log('SkiaFlux: Canvas reference and Skia API set');
  };

  const executeRender = () => {
    if (!canvasReference?.current) {
      console.warn('SkiaFlux: Cannot render - canvas reference not set');
      return;
    }

    try {
      notifyAllListeners();
    } catch (error) {
      console.error('SkiaFlux: Error during render operation:', error);
    }
  };

  const subscribeToChanges = (listenerFunction) => {
    if (typeof listenerFunction !== 'function') {
      throw new Error('SkiaFlux: Listener must be a function');
    }

    changeListeners.add(listenerFunction);
    
    return () => {
      changeListeners.delete(listenerFunction);
    };
  };

  const getStoreStatistics = () => {
    const allComponents = getAllComponents();
    const visibleComponents = getAllVisibleComponents();
    
    return {
      totalComponents: componentRegistry.size,
      visibleComponents: visibleComponents.length,
      hiddenComponents: allComponents.length - visibleComponents.length,
      pendingUpdates: pendingUpdateQueue.size,
      isRendering: isCurrentlyRendering,
      maxCapacity: fluxConfiguration.maxItems,
      capacityUsed: `${componentRegistry.size}/${fluxConfiguration.maxItems}`,
      configuration: { ...fluxConfiguration }
    };
  };

  const filterComponents = (filterFunction) => {
    return Array.from(componentRegistry.values()).filter(filterFunction);
  };

  const findComponentsByMetadata = (criteria) => {
    return filterComponents(item => {
      return Object.keys(criteria).every(key => 
        item.metadata[key] === criteria[key]
      );
    });
  };

  const getSkiaAPI = () => skiaAPI;
  const getCanvasRef = () => canvasReference;

  return {
    addComponent,
    updateComponent,
    removeComponent,
    getComponent,
    getAllVisibleComponents,
    getAllComponents,
    clearAllComponents,
    
    filterComponents,
    findComponentsByMetadata,
    
    setCanvasReference,
    executeRender,
    getSkiaAPI,
    getCanvasRef,
    
    subscribeToChanges,
    getStoreStatistics,
    
    // Backwards compatibility aliases
    add: addComponent,
    update: updateComponent,
    remove: removeComponent,
    get: getComponent,
    getAll: getAllVisibleComponents,
    clear: clearAllComponents,
    setCanvasRef: setCanvasReference,
    render: executeRender,
    subscribe: subscribeToChanges,
    getStats: getStoreStatistics
  };
};

// Default store instance
export const skiaFluxStore = createSkiaFluxStore();

// ✅ FIXED: Stable references prevent infinite re-renders
export function useSkiaFlux(storeInstance = skiaFluxStore) {
  const [updateCount, setUpdateCount] = useState(0);
  
  useEffect(() => {
    const unsubscribeFromChanges = storeInstance.subscribeToChanges(() => {
      setUpdateCount(prev => prev + 1);
    });

    return unsubscribeFromChanges;
  }, [storeInstance]); 

  // ✅ CRITICAL FIX: Memoize the returned object to prevent new references
  return useMemo(() => ({
    // Direct method references (these are stable from the store)
    addComponent: storeInstance.addComponent,
    updateComponent: storeInstance.updateComponent,
    removeComponent: storeInstance.removeComponent,
    getComponent: storeInstance.getComponent,
    getAllVisibleComponents: storeInstance.getAllVisibleComponents,
    getAllComponents: storeInstance.getAllComponents,
    clearAllComponents: storeInstance.clearAllComponents,
    
    filterComponents: storeInstance.filterComponents,
    findComponentsByMetadata: storeInstance.findComponentsByMetadata,
    
    // Aliases
    add: storeInstance.addComponent,
    update: storeInstance.updateComponent,
    remove: storeInstance.removeComponent,
    get: storeInstance.getComponent,
    getAll: storeInstance.getAllVisibleComponents,
    clear: storeInstance.clearAllComponents,
    
    // ✅ FIXED: Getter function instead of direct value
    getStats: () => storeInstance.getStoreStatistics()
  }), [storeInstance, updateCount]); // updateCount ensures re-render when store changes
}

// ✅ FIXED: Memoize components array to prevent unnecessary re-renders
export function SkiaFluxCanvas({ 
  style, 
  onSetup, 
  storeInstance = skiaFluxStore,
  ...otherProps 
}) {
  const canvasRef = useCanvasRef();
  const storeAPI = useSkiaFlux(storeInstance);
  const setupCalledRef = useRef(false);

  useEffect(() => {
    if (canvasRef.current && !setupCalledRef.current) {
      storeInstance.setCanvasReference(canvasRef, Skia);
      
      if (onSetup && typeof onSetup === 'function') {
        try {
          onSetup(canvasRef.current, Skia, storeInstance);
          setupCalledRef.current = true;
        } catch (error) {
          console.error('SkiaFlux: Error in onSetup callback:', error);
        }
      }
    }
  }, [canvasRef, storeInstance, onSetup]);

  // ✅ FIXED: Memoize visible components to prevent re-computing on every render
  const visibleComponents = useMemo(() => {
    return storeAPI.getAllVisibleComponents();
  }, [storeAPI]);

  return (
    <Canvas ref={canvasRef} style={style} {...otherProps}>
      {visibleComponents.map(item => (
        <React.Fragment key={item.uid}>
          {typeof item.component !== 'function' ? item.component : null}
        </React.Fragment>
      ))}
    </Canvas>
  );
}

// Factory functions
export const createSkiaFluxStoreInstance = (config) => createSkiaFluxStore(config);

export const createPerformanceOptimizedStore = (additionalConfig = {}) => {
  return createSkiaFluxStore({
    autoRender: false,
    batchUpdates: true,
    maxItems: 10000,
    ...additionalConfig
  });
};

export const createDevelopmentStore = (additionalConfig = {}) => {
  return createSkiaFluxStore({
    autoRender: true,
    batchUpdates: false,
    maxItems: 100,
    ...additionalConfig
  });
};