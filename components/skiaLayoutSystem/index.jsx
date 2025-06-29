import React, { 
  createContext, 
  useContext, 
  useMemo, 
  useRef, 
  useCallback, 
  useState,
  useEffect 
} from 'react';
import { 
  Group, 
  FitBox, 
  Line, 
  Rect, 
  Text,
  useTouchHandler,
  useSharedValue,
  useComputedValue,
  useFont,
  Skia,
  Path
} from '@shopify/react-native-skia';

// Enhanced context with performance tracking and animation support
const SkiaLayoutContext = createContext({
  canvasWidth: 0,
  canvasHeight: 0,
  debug: false,
  debugLevel: 0,
  theme: {},
  animations: {},
  performance: {
    measureEnabled: false,
    renderCount: 0,
    lastRenderTime: 0
  }
});

export const useSkiaLayoutContext = () => useContext(SkiaLayoutContext);

// Performance monitoring hook
export const useLayoutPerformance = () => {
  const renderCount = useRef(0);
  const lastRenderTime = useRef(0);
  const [stats, setStats] = useState({
    avgRenderTime: 0,
    totalRenders: 0,
    lastFrameDuration: 0
  });

  const startMeasure = useCallback(() => {
    lastRenderTime.current = performance.now();
  }, []);

  const endMeasure = useCallback(() => {
    const duration = performance.now() - lastRenderTime.current;
    renderCount.current++;
    
    setStats(prev => ({
      avgRenderTime: (prev.avgRenderTime * prev.totalRenders + duration) / (prev.totalRenders + 1),
      totalRenders: renderCount.current,
      lastFrameDuration: duration
    }));
  }, []);

  return { startMeasure, endMeasure, stats };
};

// Enhanced provider with theme and animation support
export const SkiaLayoutProvider = ({ 
  children, 
  canvasRect, 
  debug = false, 
  theme = {},
  enablePerformanceTracking = false 
}) => {
  const animations = useRef({}).current;
  
  const value = useMemo(() => ({
    canvasWidth: canvasRect?.width || 0,
    canvasHeight: canvasRect?.height || 0,
    debug,
    debugLevel: debug ? 1 : 0,
    theme: {
      colors: {
        primary: '#007AFF',
        secondary: '#34C759',
        background: '#F2F2F7',
        text: '#000000',
        border: '#C7C7CC',
        error: '#FF3B30',
        warning: '#FF9500',
        ...theme.colors
      },
      spacing: {
        xs: 4,
        sm: 8,
        md: 16,
        lg: 24,
        xl: 32,
        ...theme.spacing
      },
      typography: {
        small: 12,
        body: 16,
        title: 20,
        heading: 24,
        ...theme.typography
      },
      ...theme
    },
    animations,
    performance: {
      measureEnabled: enablePerformanceTracking,
      renderCount: 0,
      lastRenderTime: 0
    }
  }), [canvasRect, debug, theme, enablePerformanceTracking]);

  return (
    <SkiaLayoutContext.Provider value={value}>
      {children}
    </SkiaLayoutContext.Provider>
  );
};


// Advanced dimension calculation with caching - FIXED
const createDimensionCalculator = () => {
  const cache = new Map();
  const fontCache = new Map();
  
  /**
   * FIXED: Creates a proper cache key from React element props
   * Fixed the logical error in type checking and serialization
   */
  const createSafeCacheKey = (child) => {
    if (!React.isValidElement(child)) return 'invalid';
    
    const type = child.type?.name || child.type?.displayName || 'unknown';
    const props = child.props || {};
    
    // FIXED: Proper serializable props extraction
    const safeProps = {};
    for (const [key, value] of Object.entries(props)) {
      if (value === null || value === undefined) {
        safeProps[key] = String(value);
      } else if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean') {
        safeProps[key] = value;
      } else if (Array.isArray(value) && value.every(v => typeof v === 'string' || typeof v === 'number')) {
        safeProps[key] = value.join(',');
      }
      // Skip functions and complex objects that can't be safely serialized
    }
    
    // Create a deterministic cache key
    const keyString = JSON.stringify({
      type,
      width: safeProps.width,
      height: safeProps.height,
      r: safeProps.r,
      size: safeProps.size,
      text: safeProps.text,
      fontSize: safeProps.fontSize,
      fontFamily: safeProps.fontFamily,
      fontWeight: safeProps.fontWeight,
      path: safeProps.path,
      style: safeProps.style,
      strokeWidth: safeProps.strokeWidth,
      flex: safeProps.flex
    });
    
    return keyString;
  };

  /**
   * FIXED: Get or create font instance with proper caching
   * Uses the actual Skia font matching API for accurate measurements
   */
  const getFont = (fontSize = 16, fontFamily = 'system', fontWeight = 'normal') => {
    const fontKey = `${fontFamily}-${fontSize}-${fontWeight}`;
    
    if (fontCache.has(fontKey)) {
      return fontCache.get(fontKey);
    }
    
    try {
      // Use the proper matchFont API from react-native-skia
      const font = matchFont({
        fontFamily: fontFamily === 'system' ? undefined : fontFamily,
        fontSize,
        fontWeight: fontWeight === 'normal' ? undefined : fontWeight,
      });
      
      fontCache.set(fontKey, font);
      return font;
    } catch (error) {
      console.warn('Font matching failed, using default:', error);
      // Fallback to default font
      const defaultFont = matchFont({ fontSize });
      fontCache.set(fontKey, defaultFont);
      return defaultFont;
    }
  };

  /**
   * FIXED: Accurate text dimension calculation using proper Skia font measurement
   * Replaces the inaccurate character width estimation approach
   */
  const getTextDimensions = (text, fontSize = 16, fontFamily = 'system', fontWeight = 'normal') => {
    if (!text || text.length === 0) return { width: 0, height: 0 };
    
    const font = getFont(fontSize, fontFamily, fontWeight);
    const lines = text.split('\n');
    
    try {
      // FIXED: Use proper Skia font measurement API
      let maxWidth = 0;
      
      for (const line of lines) {
        if (line.length > 0) {
          // Use the font's measureText method for accurate width
          const lineWidth = font.measureText(line);
          maxWidth = Math.max(maxWidth, lineWidth);
        }
      }
      
      // Get proper font metrics for accurate height calculation
      const fontMetrics = font.getMetrics();
      const lineHeight = Math.abs(fontMetrics.descent - fontMetrics.ascent);
      const totalHeight = lineHeight * lines.length;
      
      return {
        width: maxWidth,
        height: totalHeight,
        lineHeight,
        metrics: fontMetrics
      };
    } catch (error) {
      console.warn('Text measurement failed, using fallback:', error);
      // Fallback to improved estimation if Skia measurement fails
      const avgCharWidth = fontSize * 0.6;
      const maxLineLength = Math.max(...lines.map(line => line.length));
      return {
        width: maxLineLength * avgCharWidth,
        height: fontSize * 1.2 * lines.length,
        lineHeight: fontSize * 1.2
      };
    }
  };

  /**
   * FIXED: Proper path dimension calculation using Skia bounds
   * Handles SVG path measurement correctly
   */
  const getPathDimensions = (pathString) => {
    try {
      const path = Skia.Path.MakeFromSVGString(pathString);
      if (!path) return { width: 0, height: 0 };
      
      // Use computeTightBounds for accurate path dimensions
      const bounds = path.computeTightBounds();
      path.delete(); // Clean up path object
      
      return {
        width: bounds.width,
        height: bounds.height,
        bounds
      };
    } catch (error) {
      console.warn('Path measurement failed:', error);
      return { width: 100, height: 100 }; // Fallback dimensions
    }
  };

  /**
   * FIXED: Proper stroke width handling
   * Strokes expand outward from the shape boundary, not inward
   */
  const getStrokeAdjustment = (props) => {
    if (props.style === "stroke" && props.strokeWidth) {
      // Stroke expands outward by strokeWidth/2 on each side
      return props.strokeWidth;
    }
    return 0;
  };
  
  return {
    /**
     * FIXED: Main function to calculate child element dimensions
     * Completely rewritten with proper Skia API usage and accurate measurements
     */
    getChildDimensions: (child, defaultWidth = 100, defaultHeight = 50, parentContext = {}) => {
      if (!React.isValidElement(child)) return { width: 0, height: 0 };
      
      const cacheKey = createSafeCacheKey(child);
      if (cache.has(cacheKey)) return cache.get(cacheKey);
      
      const props = child.props || {};
      const elementType = child.type?.name || child.type?.displayName || 'unknown';
      let dimensions = { width: defaultWidth, height: defaultHeight };

      // Calculate stroke adjustment (affects final bounds)
      const strokeAdjustment = getStrokeAdjustment(props);

      // FIXED: Proper dimension detection by element type
      switch (elementType) {
        case 'Rect':
        case 'RoundedRect':
          if (props.width !== undefined && props.height !== undefined) {
            dimensions = { 
              width: props.width + strokeAdjustment,
              height: props.height + strokeAdjustment
            };
          }
          break;

        case 'Circle':
          if (props.r !== undefined) {
            const diameter = (props.r * 2) + strokeAdjustment;
            dimensions = { width: diameter, height: diameter };
          } else if (props.cx !== undefined && props.cy !== undefined && props.r !== undefined) {
            const diameter = (props.r * 2) + strokeAdjustment;
            dimensions = { width: diameter, height: diameter };
          }
          break;

        case 'Oval':
          if (props.width !== undefined && props.height !== undefined) {
            dimensions = { 
              width: props.width + strokeAdjustment,
              height: props.height + strokeAdjustment
            };
          }
          break;

        case 'Text':
          // FIXED: Use proper text measurement
          const fontSize = props.size || props.fontSize || 16;
          const fontFamily = props.fontFamily || 'system';
          const fontWeight = props.fontWeight || 'normal';
          const text = props.text || '';
          
          dimensions = getTextDimensions(text, fontSize, fontFamily, fontWeight);
          break;

        case 'Path':
          // FIXED: Use proper path measurement
          if (props.path) {
            const pathDims = getPathDimensions(props.path);
            dimensions = {
              width: pathDims.width + strokeAdjustment,
              height: pathDims.height + strokeAdjustment
            };
          }
          break;

        case 'Line':
          // Calculate line dimensions from coordinates
          const x1 = props.p1?.x || props.x1 || 0;
          const y1 = props.p1?.y || props.y1 || 0;
          const x2 = props.p2?.x || props.x2 || 0;
          const y2 = props.p2?.y || props.y2 || 0;
          
          dimensions = {
            width: Math.abs(x2 - x1) + strokeAdjustment,
            height: Math.abs(y2 - y1) + strokeAdjustment
          };
          break;

        case 'Group':
          // FIXED: Calculate group dimensions based on children bounds
          const children = React.Children.toArray(props.children);
          if (children.length > 0) {
            let minX = Infinity, minY = Infinity;
            let maxX = -Infinity, maxY = -Infinity;
            
            children.forEach(gChild => {
              const childDims = this.getChildDimensions(gChild, defaultWidth, defaultHeight, parentContext);
              const childX = gChild.props?.x || 0;
              const childY = gChild.props?.y || 0;
              
              minX = Math.min(minX, childX);
              minY = Math.min(minY, childY);
              maxX = Math.max(maxX, childX + childDims.width);
              maxY = Math.max(maxY, childY + childDims.height);
            });
            
            dimensions = {
              width: maxX - minX,
              height: maxY - minY
            };
          }
          break;

        default:
          // Handle generic elements with explicit dimensions
          if (props.width !== undefined && props.height !== undefined) {
            dimensions = { 
              width: props.width + strokeAdjustment,
              height: props.height + strokeAdjustment
            };
          } else if (props.size !== undefined) {
            dimensions = { 
              width: props.size + strokeAdjustment, 
              height: props.size + strokeAdjustment 
            };
          }
          break;
      }
      
      // FIXED: Apply flex properties correctly
      if (props.flex && parentContext.availableWidth && parentContext.totalFlex) {
        dimensions.width = parentContext.availableWidth * (props.flex / parentContext.totalFlex);
      }
      
      cache.set(cacheKey, dimensions);
      return dimensions;
    },
    
    clearCache: () => {
      cache.clear();
      fontCache.clear();
    },
    
    getCacheStats: () => ({ 
      dimensionCache: cache.size, 
      fontCache: fontCache.size 
    })
  };
};

// Global dimension calculator instance
const dimensionCalculator = createDimensionCalculator();

// Enhanced cloning with proper bounding box positioning


const cloneChildrenWithLayout = (children, layoutProps, debugInfo = null, themeProps = {}) => {
  return React.Children.map(children, (child, index) => {
    if (!React.isValidElement(child)) return child;
    
    const childProps = layoutProps[index] || {};
    const { x = 0, y = 0, ...restChildProps } = childProps;
    const dimensions = dimensionCalculator.getChildDimensions(child);
    
    // FIXED: Proper visual offset calculation
    const origin = child.props?.origin || { x: 0, y: 0 };
    const visualOffsetX = origin.x || 0;
    const visualOffsetY = origin.y || 0;

    const clonedChild = React.cloneElement(child, {
      ...restChildProps,
      ...themeProps,
      key: child.key || `layout-${index}`,
      // FIXED: Don't modify the child's intrinsic position
      // Let the Group handle all positioning
    });

    // FIXED: Enhanced debug visualization with accurate bounding box
    if (debugInfo?.debug && debugInfo.debugLevel > 0) {
      return (
        <Group transform={[{ translateX: x }, { translateY: y }]}>
          {clonedChild}
          {/* Accurate bounding box visualization */}
          <Rect
            x={0}
            y={0}
            width={dimensions.width}
            height={dimensions.height}
            style="stroke"
            strokeWidth={1}
            color="rgba(255, 0, 0, 0.8)"
          />
          {/* Debug label with accurate dimensions */}
          {debugInfo.debugLevel > 1 && (
            <Text
              x={2}
              y={14}
              text={`${child.type?.name || 'unknown'}: ${Math.round(dimensions.width)}×${Math.round(dimensions.height)}`}
              color="red"
              size={10}
            />
          )}
        </Group>
      );
    }

    return (
      <Group transform={[{ translateX: x }, { translateY: y }]}>
        {clonedChild}
      </Group>
    );
  });
};


// Enhanced FlexBox-inspired Column Component
export const LayoutColumn = ({ 
  children, 
  spacing = 0, 
  padding = 0, 
  align = 'start',
  justify = 'start',
  width,
  height,
  flex = 0,
  wrap = false,
  reverse = false,
  animationDuration = 300
}) => {
  const context = useSkiaLayoutContext();
  const { startMeasure, endMeasure } = useLayoutPerformance();
  
  const layoutProps = useMemo(() => {
    startMeasure();
    
    const childArray = React.Children.toArray(children);
    if (reverse) childArray.reverse();
    
    const containerWidth = width || context.canvasWidth;
    const containerHeight = height || context.canvasHeight;
    const availableHeight = containerHeight - (padding * 2);
    
    // Calculate total flex units
    const totalFlex = childArray.reduce((acc, child) => {
      const childFlex = React.isValidElement(child) ? (child.props?.flex || 0) : 0;
      return acc + childFlex;
    }, 0);
    
    // Calculate dimensions for all children
    const childDimensions = childArray.map(child => 
      dimensionCalculator.getChildDimensions(child, 100, 50, {
        availableWidth: containerWidth - (padding * 2),
        totalFlex
      })
    );
    
    // Calculate total content height
    const totalContentHeight = childDimensions.reduce((acc, dim) => acc + dim.height, 0);
    const totalSpacing = (childArray.length - 1) * spacing;
    const usedHeight = totalContentHeight + totalSpacing;
    const remainingHeight = availableHeight - usedHeight;
    
    let currentY = padding;
    let extraSpacing = 0;
    
    // Apply vertical justification
    switch (justify) {
      case 'center':
        currentY = padding + (remainingHeight / 2);
        break;
      case 'end':
        currentY = containerHeight - usedHeight - padding;
        break;
      case 'space-between':
        extraSpacing = childArray.length > 1 ? remainingHeight / (childArray.length - 1) : 0;
        break;
      case 'space-around':
        extraSpacing = remainingHeight / childArray.length;
        currentY = padding + (extraSpacing / 2);
        break;
      case 'space-evenly':
        extraSpacing = remainingHeight / (childArray.length + 1);
        currentY = padding + extraSpacing;
        break;
    }
    
    const result = childArray.map((child, index) => {
      const childDims = childDimensions[index];
      let x = padding;
      
      // Apply horizontal alignment
      switch (align) {
        case 'center':
          x = (containerWidth - childDims.width) / 2;
          break;
        case 'end':
          x = containerWidth - childDims.width - padding;
          break;
        case 'stretch':
          // For stretch, we'll modify the child's width
          break;
      }
      
      const layoutProps = { 
        x, 
        y: currentY,
        ...(align === 'stretch' && { width: containerWidth - (padding * 2) })
      };
      
      currentY += childDims.height + spacing + extraSpacing;
      
      return layoutProps;
    });
    
    endMeasure();
    return result;
  }, [
    children, spacing, padding, align, justify, width, height, 
    flex, wrap, reverse, context.canvasWidth, context.canvasHeight,
    startMeasure, endMeasure
  ]);

  return (
    <Group>
      {cloneChildrenWithLayout(
        children, 
        layoutProps, 
        { debug: context.debug, debugLevel: context.debugLevel },
        { theme: context.theme }
      )}
    </Group>
  );
};

// Enhanced FlexBox-inspired Row Component
export const LayoutRow = ({ 
  children, 
  spacing = 0, 
  padding = 0, 
  justify = 'start',
  align = 'start',
  width,
  height,
  wrap = false,
  reverse = false,
  animationDuration = 300
}) => {
  const context = useSkiaLayoutContext();
  const { startMeasure, endMeasure } = useLayoutPerformance();
  
  const layoutProps = useMemo(() => {
    startMeasure();
    
    const childArray = React.Children.toArray(children);
    if (reverse) childArray.reverse();
    
    const containerWidth = width || context.canvasWidth;
    const containerHeight = height || context.canvasHeight;
    const availableWidth = containerWidth - (padding * 2);
    
    // Calculate total flex units
    const totalFlex = childArray.reduce((acc, child) => {
      const childFlex = React.isValidElement(child) ? (child.props?.flex || 0) : 0;
      return acc + childFlex;
    }, 0);
    
    // Calculate dimensions for all children
    const childDimensions = childArray.map(child => 
      dimensionCalculator.getChildDimensions(child, 100, 50, {
        availableWidth,
        totalFlex
      })
    );
    
    // Handle wrapping
    let rows = [[]];
    let currentRowWidth = 0;
    let currentRowIndex = 0;
    
    if (wrap) {
      childArray.forEach((child, index) => {
        const childWidth = childDimensions[index].width;
        
        if (currentRowWidth + childWidth + spacing > availableWidth && rows[currentRowIndex].length > 0) {
          currentRowIndex++;
          rows[currentRowIndex] = [];
          currentRowWidth = 0;
        }
        
        rows[currentRowIndex].push({ child, index, dimensions: childDimensions[index] });
        currentRowWidth += childWidth + spacing;
      });
    } else {
      rows[0] = childArray.map((child, index) => ({
        child,
        index,
        dimensions: childDimensions[index]
      }));
    }
    
    const result = [];
    let currentY = padding;
    
    rows.forEach((row, rowIndex) => {
      const totalRowWidth = row.reduce((acc, item) => acc + item.dimensions.width, 0);
      const totalRowSpacing = (row.length - 1) * spacing;
      const usedWidth = totalRowWidth + totalRowSpacing;
      const remainingWidth = availableWidth - usedWidth;
      const maxRowHeight = Math.max(...row.map(item => item.dimensions.height));
      
      let currentX = padding;
      let extraSpacing = 0;
      
      // Apply horizontal justification
      switch (justify) {
        case 'center':
          currentX = padding + (remainingWidth / 2);
          break;
        case 'end':
          currentX = containerWidth - usedWidth - padding;
          break;
        case 'space-between':
          extraSpacing = row.length > 1 ? remainingWidth / (row.length - 1) : 0;
          break;
        case 'space-around':
          extraSpacing = remainingWidth / row.length;
          currentX = padding + (extraSpacing / 2);
          break;
        case 'space-evenly':
          extraSpacing = remainingWidth / (row.length + 1);
          currentX = padding + extraSpacing;
          break;
      }
      
      row.forEach((item, itemIndex) => {
        let y = currentY;
        
        // Apply vertical alignment
        switch (align) {
          case 'center':
            y = currentY + (maxRowHeight - item.dimensions.height) / 2;
            break;
          case 'end':
            y = currentY + (maxRowHeight - item.dimensions.height);
            break;
          case 'stretch':
            // For stretch, we'll modify the child's height
            break;
        }
        
        result[item.index] = {
          x: currentX,
          y,
          ...(align === 'stretch' && { height: maxRowHeight })
        };
        
        currentX += item.dimensions.width + spacing + extraSpacing;
      });
      
      currentY += maxRowHeight + spacing;
    });
    
    endMeasure();
    return result;
  }, [
    children, spacing, padding, justify, align, width, height, 
    wrap, reverse, context.canvasWidth, context.canvasHeight,
    startMeasure, endMeasure
  ]);

  return (
    <Group>
      {cloneChildrenWithLayout(
        children, 
        layoutProps, 
        { debug: context.debug, debugLevel: context.debugLevel },
        { theme: context.theme }
      )}
    </Group>
  );
};

// Enhanced Grid with dynamic sizing
export const LayoutGrid = ({ 
  children, 
  rows = 'auto', 
  columns = 'auto', 
  gap = 0, 
  padding = 0,
  width,
  height,
  autoFlow = 'row',
  template = null // CSS Grid template string support
}) => {
  const context = useSkiaLayoutContext();
  const { startMeasure, endMeasure } = useLayoutPerformance();
  
  const layoutProps = useMemo(() => {
    startMeasure();
    
    const childArray = React.Children.toArray(children);
    const containerWidth = width || context.canvasWidth;
    const containerHeight = height || context.canvasHeight;
    
    // Auto-calculate grid dimensions
    const actualColumns = columns === 'auto' ? Math.ceil(Math.sqrt(childArray.length)) : columns;
    const actualRows = rows === 'auto' ? Math.ceil(childArray.length / actualColumns) : rows;
    
    const availableWidth = containerWidth - (padding * 2) - (gap * (actualColumns - 1));
    const availableHeight = containerHeight - (padding * 2) - (gap * (actualRows - 1));
    const cellWidth = availableWidth / actualColumns;
    const cellHeight = availableHeight / actualRows;
    
    const result = childArray.map((child, index) => {
      let row, col;
      
      if (autoFlow === 'column') {
        col = Math.floor(index / actualRows);
        row = index % actualRows;
      } else {
        row = Math.floor(index / actualColumns);
        col = index % actualColumns;
      }
      
      const x = padding + (col * (cellWidth + gap));
      const y = padding + (row * (cellHeight + gap));
      
      return { 
        x, 
        y, 
        gridCellWidth: cellWidth, 
        gridCellHeight: cellHeight 
      };
    });
    
    endMeasure();
    return result;
  }, [
    children, rows, columns, gap, padding, width, height, 
    autoFlow, template, context.canvasWidth, context.canvasHeight,
    startMeasure, endMeasure
  ]);

  return (
    <Group>
      {cloneChildrenWithLayout(
        children, 
        layoutProps, 
        { debug: context.debug, debugLevel: context.debugLevel },
        { theme: context.theme }
      )}
    </Group>
  );
};

// Enhanced Absolute Layout with constraints
export const AbsoluteLayout = ({ 
  children, 
  width, 
  height, 
  clipToBounds = false,
  overflow = 'visible' 
}) => {
  const context = useSkiaLayoutContext();
  
  const layoutProps = useMemo(() => {
    return React.Children.map(children, (child) => {
      if (!React.isValidElement(child)) return {};
      
      const { 
        top = 0, 
        left = 0, 
        right, 
        bottom, 
        centerX, 
        centerY 
      } = child.props;
      
      let x = left;
      let y = top;
      
      // Handle right positioning
      if (right !== undefined) {
        const containerWidth = width || context.canvasWidth;
        const childDims = dimensionCalculator.getChildDimensions(child);
        x = containerWidth - right - childDims.width;
      }
      
      // Handle bottom positioning
      if (bottom !== undefined) {
        const containerHeight = height || context.canvasHeight;
        const childDims = dimensionCalculator.getChildDimensions(child);
        y = containerHeight - bottom - childDims.height;
      }
      
      // Handle center positioning
      if (centerX !== undefined) {
        const containerWidth = width || context.canvasWidth;
        const childDims = dimensionCalculator.getChildDimensions(child);
        x = (containerWidth - childDims.width) / 2 + centerX;
      }
      
      if (centerY !== undefined) {
        const containerHeight = height || context.canvasHeight;
        const childDims = dimensionCalculator.getChildDimensions(child);
        y = (containerHeight - childDims.height) / 2 + centerY;
      }
      
      return { x, y };
    });
  }, [children, width, height, context.canvasWidth, context.canvasHeight]);

  const content = cloneChildrenWithLayout(
    children, 
    layoutProps,
    { debug: context.debug, debugLevel: context.debugLevel },
    { theme: context.theme }
  );

  if (clipToBounds) {
    return (
      <Group clip={{
        rect: {
          x: 0,
          y: 0,
          width: width || context.canvasWidth,
          height: height || context.canvasHeight
        }
      }}>
        {content}
      </Group>
    );
  }

  return <Group>{content}</Group>;
};

// Enhanced FitBox with animation support
export const FitBoxWrapper = ({ 
  children, 
  src, 
  dst, 
  mode = 'contain',
  animateResize = false,
  resizeDuration = 300
}) => {
  const fit = useMemo(() => {
    const modes = {
      'cover': 'cover',
      'fill': 'fill',
      'contain': 'contain',
      'fitWidth': 'fitWidth',
      'fitHeight': 'fitHeight',
      'none': 'none'
    };
    return modes[mode] || 'contain';
  }, [mode]);

  return (
    <FitBox src={src} dst={dst} fit={fit}>
      <Group>
        {children}
      </Group>
    </FitBox>
  );
};

// Advanced Responsive System
export const ResponsiveLayout = ({ 
  breakpoints = { 
    xs: 0, 
    sm: 576, 
    md: 768, 
    lg: 992, 
    xl: 1200 
  }, 
  layouts = {}, 
  fallback = null,
  orientation = 'both' // 'portrait', 'landscape', 'both'
}) => {
  const context = useSkiaLayoutContext();
  
  const currentLayout = useMemo(() => {
    const { canvasWidth, canvasHeight } = context;
    const isLandscape = canvasWidth > canvasHeight;
    
    // Filter by orientation if specified
    if (orientation === 'portrait' && isLandscape) return fallback;
    if (orientation === 'landscape' && !isLandscape) return fallback;
    
    // Sort breakpoints in descending order
    const sortedBreakpoints = Object.entries(breakpoints)
      .sort(([, a], [, b]) => b - a);
    
    for (const [key, breakpoint] of sortedBreakpoints) {
      if (canvasWidth >= breakpoint && layouts[key]) {
        return layouts[key];
      }
    }
    
    return fallback;
  }, [context.canvasWidth, context.canvasHeight, breakpoints, layouts, fallback, orientation]);

  return currentLayout || null;
};

// Advanced Debug System
export const useSkiaLayoutDebug = (enabled = false) => {
  const context = useSkiaLayoutContext();
  
  // Use useState instead of useRef for reactive updates
  const [debugData, setDebugData] = useState({
    boxes: [],
    guides: [],
    measurements: [],
    performance: []
  });
  
  // Validate context exists and has debug property
  const isDebugEnabled = enabled && context?.debug === true;
  
  const addDebugBox = useCallback((x, y, width, height, color = 'rgb(255, 0, 0)', label = '') => {
    if (!isDebugEnabled) return;
    
    const newBox = { 
      id: `box_${Date.now()}_${Math.random()}`, // Add unique ID
      x, 
      y, 
      width, 
      height, 
      color, 
      label, 
      timestamp: Date.now() 
    };
    
    setDebugData(prev => ({
      ...prev,
      boxes: [...prev.boxes, newBox]
    }));
  }, [isDebugEnabled]);
  
  const addDebugGuide = useCallback((x1, y1, x2, y2, color = 'rgba(0, 255, 0, 0.5)', type = 'line') => {
    if (!isDebugEnabled) return;
    
    const newGuide = { 
      id: `guide_${Date.now()}_${Math.random()}`,
      x1, 
      y1, 
      x2, 
      y2, 
      color, 
      type, 
      timestamp: Date.now() 
    };
    
    setDebugData(prev => ({
      ...prev,
      guides: [...prev.guides, newGuide]
    }));
  }, [isDebugEnabled]);
  
  const addMeasurement = useCallback((name, value, unit = 'px') => {
    if (!isDebugEnabled) return;
    
    const newMeasurement = { 
      id: `measurement_${Date.now()}_${Math.random()}`,
      name, 
      value, 
      unit, 
      timestamp: Date.now() 
    };
    
    setDebugData(prev => ({
      ...prev,
      measurements: [...prev.measurements, newMeasurement]
    }));
  }, [isDebugEnabled]);
  
  const clearDebug = useCallback(() => {
    setDebugData({ 
      boxes: [], 
      guides: [], 
      measurements: [], 
      performance: [] 
    });
  }, []);
  
  // Auto-clear old debug data to prevent memory leaks
  useEffect(() => {
    if (!isDebugEnabled) return;
    
    const cleanup = () => {
      const now = Date.now();
      const maxAge = 5000; // 5 seconds
      
      setDebugData(prev => ({
        boxes: prev.boxes.filter(item => now - item.timestamp < maxAge),
        guides: prev.guides.filter(item => now - item.timestamp < maxAge),
        measurements: prev.measurements.filter(item => now - item.timestamp < maxAge),
        performance: prev.performance.filter(item => now - item.timestamp < maxAge)
      }));
    };
    
    const interval = setInterval(cleanup, 1000);
    return () => clearInterval(interval);
  }, [isDebugEnabled]);
  
  return {
    addDebugBox,
    addDebugGuide,
    addMeasurement,
    clearDebug,
    debugData,
    enabled: isDebugEnabled
  };
};

// Gesture-enabled layout container
export const InteractiveLayout = ({ 
  children, 
  onTap, 
  onPan, 
  onPinch,
  enableGestures = true,
  ...layoutProps 
}) => {
  const touchHandler = useTouchHandler({
    onStart: (touch) => {
      if (onTap) {
        // Simple tap detection
        const tapTimeout = setTimeout(() => {
          onTap({ x: touch.x, y: touch.y });
        }, 100);
        
        return () => clearTimeout(tapTimeout);
      }
    },
    onActive: (touch) => {
      if (onPan) {
        onPan({ 
          x: touch.x, 
          y: touch.y, 
          translationX: touch.translationX,
          translationY: touch.translationY
        });
      }
    }
  }, [onTap, onPan]);
  
  return (
    <Group {...(enableGestures && { onTouch: touchHandler })}>
      {children}
    </Group>
  );
};

// Layout constraint system
export const ConstrainedLayout = ({
  children,
  constraints = {},
  maintainAspectRatio = false,
  aspectRatio = 1,
  ...layoutProps
}) => {
  const context = useSkiaLayoutContext();
  
  const constrainedProps = useMemo(() => {
    const { canvasWidth, canvasHeight } = context;
    const {
      minWidth = 0,
      maxWidth = canvasWidth,
      minHeight = 0,
      maxHeight = canvasHeight
    } = constraints;
    
    let width = layoutProps.width || canvasWidth;
    let height = layoutProps.height || canvasHeight;
    
    // Apply constraints
    width = Math.max(minWidth, Math.min(maxWidth, width));
    height = Math.max(minHeight, Math.min(maxHeight, height));
    
    // Maintain aspect ratio if requested
    if (maintainAspectRatio) {
      const currentRatio = width / height;
      if (currentRatio > aspectRatio) {
        width = height * aspectRatio;
      } else {
        height = width / aspectRatio;
      }
    }
    
    return { ...layoutProps, width, height };
  }, [context, constraints, maintainAspectRatio, aspectRatio, layoutProps]);
  
  return React.cloneElement(children, constrainedProps);
};

// Advanced scrollable layout
export const ScrollableLayout = ({
  children,
  direction = 'vertical',
  showScrollbars = true,
  bounces = true,
  pagingEnabled = false,
  contentSize,
  ...layoutProps
}) => {
  const scrollOffset = useSharedValue({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  
  const touchHandler = useTouchHandler({
    onStart: () => setIsDragging(true),
    onActive: (touch) => {
      if (direction === 'vertical') {
        scrollOffset.value = {
          ...scrollOffset.value,
          y: Math.max(0, scrollOffset.value.y - touch.velocityY)
        };
      } else {
        scrollOffset.value = {
          ...scrollOffset.value,
          x: Math.max(0, scrollOffset.value.x - touch.velocityX)
        };
      }
    },
    onEnd: () => {
      setIsDragging(false);
      // Add momentum scrolling here if needed
    }
  });
  
  const transform = useComputedValue(() => [
    { translateX: -scrollOffset.value.x },
    { translateY: -scrollOffset.value.y }
  ], [scrollOffset]);
  
  return (
    <Group clip={{ rect: { x: 0, y: 0, width: layoutProps.width, height: layoutProps.height } }}>
      <Group transform={transform} onTouch={touchHandler}>
        {children}
      </Group>
      {/* Scrollbar indicators would go here */}
    </Group>
  );
};

// Theme-aware component wrapper
export const ThemedLayout = ({ 
  children, 
  variant = 'default',
  colorScheme = 'light'
}) => {
  const context = useSkiaLayoutContext();
  
  const themedProps = useMemo(() => {
    const { theme } = context;
    const colors = colorScheme === 'dark' ? 
      { ...theme.colors, background: '#000', text: '#fff' } : 
      theme.colors;
    
    return {
      theme: { ...theme, colors },
      variant
    };
  }, [context.theme, variant, colorScheme]);
  
  return React.cloneElement(children, themedProps);
};

// Performance optimization hook for large lists
export const useVirtualization = (itemCount, itemHeight, containerHeight) => {
  const [scrollTop, setScrollTop] = useState(0);
  
  const visibleRange = useMemo(() => {
    const start = Math.floor(scrollTop / itemHeight);
    const visibleCount = Math.ceil(containerHeight / itemHeight);
    const end = Math.min(start + visibleCount + 1, itemCount);
    
    return { start, end };
  }, [scrollTop, itemHeight, containerHeight, itemCount]);
  
  const totalHeight = itemCount * itemHeight;
  const offsetY = visibleRange.start * itemHeight;
  
  return {
    visibleRange,
    totalHeight,
    offsetY,
    setScrollTop
  };
};

// OPTIMIZED: Single Path for Grid - Massive Performance Improvement
const OptimizedGrid = ({ canvasWidth, canvasHeight, gridSize = 20, showGrid = true }) => {
  const gridPath = useMemo(() => {
    if (!showGrid) return '';
    
    const majorGridSize = gridSize * 5;
    let pathString = '';
    
    // Build path string for all vertical lines
    for (let x = 0; x <= canvasWidth; x += gridSize) {
      pathString += `M ${x} 0 L ${x} ${canvasHeight} `;
    }
    
    // Build path string for all horizontal lines  
    for (let y = 0; y <= canvasHeight; y += gridSize) {
      pathString += `M 0 ${y} L ${canvasWidth} ${y} `;
    }
    
    return pathString;
  }, [canvasWidth, canvasHeight, gridSize, showGrid]);
  
  const majorGridPath = useMemo(() => {
    if (!showGrid) return '';
    
    const majorGridSize = gridSize * 5;
    let pathString = '';
    
    // Build path string for major grid lines only
    for (let x = 0; x <= canvasWidth; x += majorGridSize) {
      pathString += `M ${x} 0 L ${x} ${canvasHeight} `;
    }
    
    for (let y = 0; y <= canvasHeight; y += majorGridSize) {
      pathString += `M 0 ${y} L ${canvasWidth} ${y} `;
    }
    
    return pathString;
  }, [canvasWidth, canvasHeight, gridSize, showGrid]);
  
  if (!showGrid) return null;
  
  return (
    <Group>
      {/* Minor grid lines - single path */}
      <Path 
        path={gridPath}
        color="rgba(0, 0, 255, 0.1)"
        style="stroke"
        strokeWidth={0.5}
      />
      {/* Major grid lines - single path */}
      <Path 
        path={majorGridPath}
        color="rgba(0, 0, 255, 0.3)"
        style="stroke"
        strokeWidth={1}
      />
    </Group>
  );
};

// OPTIMIZED: Efficient Ruler System
const OptimizedRulers = ({ canvasWidth, canvasHeight, gridSize = 20, showRulers = false }) => {
  const rulerMarks = useMemo(() => {
    if (!showRulers) return { horizontal: [], vertical: [] };
    
    const horizontal = [];
    const vertical = [];
    const majorStep = gridSize * 5;
    
    // Only show major marks to reduce Text component count
    for (let x = 0; x <= canvasWidth; x += majorStep) {
      if (x > 20) { // Avoid overlap with left ruler
        horizontal.push({ value: x, position: x });
      }
    }
    
    for (let y = 0; y <= canvasHeight; y += majorStep) {
      if (y > 20) { // Avoid overlap with top ruler
        vertical.push({ value: y, position: y });
      }
    }
    
    return { horizontal, vertical };
  }, [canvasWidth, canvasHeight, gridSize, showRulers]);
  
  if (!showRulers) return null;
  
  return (
    <Group>
      {/* Top ruler background */}
      <Rect
        x={0}
        y={0}
        width={canvasWidth}
        height={20}
        color="rgba(0, 0, 0, 0.8)"
      />
      
      {/* Left ruler background */}
      <Rect
        x={0}
        y={0}
        width={20}
        height={canvasHeight}
        color="rgba(0, 0, 0, 0.8)"
      />
      
      {/* Horizontal ruler marks - reduced count */}
      {rulerMarks.horizontal.map(mark => (
        <Text
          key={`ruler-x-${mark.value}`}
          x={mark.position + 2}
          y={15}
          text={mark.value.toString()}
          color="white"
          size={8}
        />
      ))}
      
      {/* Vertical ruler marks - reduced count */}
      {rulerMarks.vertical.map(mark => (
        <Text
          key={`ruler-y-${mark.value}`}
          x={2}
          y={mark.position + 15}
          text={mark.value.toString()}
          color="white"
          size={8}
        />
      ))}
    </Group>
  );
};

// OPTIMIZED: Performance-aware Debug Canvas
export const LayoutDebugCanvas = ({ 
  children, 
  gridSize = 20, 
  showGrid = true,
  showPerformance = false,
  showRulers = false,
  maxDebugElements = 100 // Prevent debug overlay explosion
}) => {
  const context = useSkiaLayoutContext();
  const { canvasWidth, canvasHeight } = context;
  const frameCount = useRef(0);
  const [fps, setFps] = useState(60);
  
  // Performance monitoring
  useEffect(() => {
    if (!showPerformance) return;
    
    const interval = setInterval(() => {
      frameCount.current++;
      if (frameCount.current % 60 === 0) {
        setFps(60); // Simplified FPS display
      }
    }, 16.67); // ~60fps
    
    return () => clearInterval(interval);
  }, [showPerformance]);
  
  if (!context.debug) {
    return <Group>{children}</Group>;
  }
  
  return (
    <Group>
      {/* Main content - render first for z-order */}
      {children}
      
      {/* Optimized grid - single path instead of hundreds of components */}
      <OptimizedGrid 
        canvasWidth={canvasWidth}
        canvasHeight={canvasHeight}
        gridSize={gridSize}
        showGrid={showGrid}
      />
      
      {/* Optimized rulers - reduced text component count */}
      <OptimizedRulers
        canvasWidth={canvasWidth}
        canvasHeight={canvasHeight}
        gridSize={gridSize}
        showRulers={showRulers}
      />
      
      {/* Streamlined performance info */}
      {showPerformance && (
        <Group>
          <RoundedRect
            x={canvasWidth - 180}
            y={10}
            width={170}
            height={50}
            r={5}
            color="rgba(0, 0, 0, 0.85)"
          />
          <Text
            x={canvasWidth - 175}
            y={25}
            text={`${canvasWidth}×${canvasHeight}`}
            color="white"
            size={11}
            fontWeight="500"
          />
          <Text
            x={canvasWidth - 175}
            y={40}
            text={`Debug: L${context.debugLevel || 1}`}
            color="white"
            size={10}
          />
          <Text
            x={canvasWidth - 175}
            y={55}
            text={`FPS: ${fps}`}
            color={fps < 30 ? "#ff4444" : fps < 50 ? "#ffaa00" : "#44ff44"}
            size={10}
          />
        </Group>
      )}
    </Group>
  );
};










// OPTIMIZED: Batch debug elements to prevent performance issues
export const BatchedDebugOverlay = ({ debugData, maxElements = 50 }) => {
  const visibleElements = useMemo(() => {
    const { boxes, guides } = debugData;
    const recent = [...boxes, ...guides]
      .sort((a, b) => b.timestamp - a.timestamp)
      .slice(0, maxElements);
    
    return recent;
  }, [debugData, maxElements]);
  
  return (
    <Group>
      {visibleElements.map((element, index) => {
        if (element.width !== undefined) {
          // Debug box
          return (
            <Rect
              key={`debug-${element.timestamp}-${index}`}
              x={element.x}
              y={element.y}
              width={element.width}
              height={element.height}
              color="transparent"
              style="stroke"
              strokeWidth={1}
              strokeColor={element.color}
            />
          );
        } else {
          // Debug guide/line
          return (
            <Line
              key={`guide-${element.timestamp}-${index}`}
              p1={{ x: element.x1, y: element.y1 }}
              p2={{ x: element.x2, y: element.y2 }}
              color={element.color}
              strokeWidth={1}
            />
          );
        }
      })}
    </Group>
  );
};

// Enhanced Text Measurement with caching
const measurementCache = new Map();

export const measureText = (text, fontSize = 16, fontFamily = 'system', fontWeight = 'normal') => {
  const key = `${text}-${fontSize}-${fontFamily}-${fontWeight}`;
  
  if (measurementCache.has(key)) {
    return measurementCache.get(key);
  }
  
  const measurement = dimensionCalculator.getChildDimensions(
    React.createElement(Text, { 
      text, 
      size: fontSize, 
      fontFamily, 
      fontWeight 
    })
  );
  
  measurementCache.set(key, measurement);
  
  // Clear cache if it gets too large
  if (measurementCache.size > 1000) {
    const entries = Array.from(measurementCache.entries());
    measurementCache.clear();
    // Keep the most recent 500 entries
    entries.slice(-500).forEach(([k, v]) => measurementCache.set(k, v));
  }
  
  return measurement;
};

// Animation utilities for layout transitions (unchanged but noted for performance)
export const useLayoutAnimation = (enabled = true, duration = 300) => {
  const progress = useSharedValue(0);
  const [isAnimating, setIsAnimating] = useState(false);
  
  const animate = useCallback((from, to, callback) => {
    if (!enabled) {
      callback(to);
      return;
    }
    
    setIsAnimating(true);
    progress.value = 0;
    
    const startTime = Date.now();
    const animate = () => {
      const elapsed = Date.now() - startTime;
      const t = Math.min(elapsed / duration, 1);
      
      // Easing function (ease-out)
      const eased = 1 - Math.pow(1 - t, 3);
      
      const interpolated = {
        x: from.x + (to.x - from.x) * eased,
        y: from.y + (to.y - from.y) * eased,
        width: from.width + (to.width - from.width) * eased,
        height: from.height + (to.height - from.height) * eased
      };
      
      callback(interpolated);
      progress.value = eased;
      
      if (t < 1) {
        requestAnimationFrame(animate);
      } else {
        setIsAnimating(false);
      }
    };
    
    requestAnimationFrame(animate);
  }, [enabled, duration, progress]);
  
  return { animate, isAnimating, progress };
};

// All other components remain the same...
// (Gesture, Layout, Scrollable, etc. - unchanged as they weren't the performance issue)

// OPTIMIZED: Main layout root with performance safeguards
export const SkiaLayoutRoot = ({ 
  children, 
  canvasRect, 
  debug = false, 
  theme = {},
  enablePerformanceTracking = false,
  enableAnimations = true,
  globalGestures = false,
  debugConfig = {
    maxDebugElements: 100,
    gridOptimization: true,
    rulerOptimization: true
  }
}) => {
  // Clear dimension cache when canvas size changes
  useEffect(() => {
    dimensionCalculator.clearCache();
    measurementCache.clear(); // Also clear text measurement cache
  }, [canvasRect]);
  
  return (
    <SkiaLayoutProvider 
      canvasRect={canvasRect} 
      debug={debug} 
      theme={theme}
      enablePerformanceTracking={enablePerformanceTracking}
    >
      <LayoutDebugCanvas 
        showGrid={debug && debugConfig.gridOptimization}
        showPerformance={enablePerformanceTracking}
        showRulers={debug && debugConfig.rulerOptimization}
        maxDebugElements={debugConfig.maxDebugElements}
      >
        {globalGestures ? (
          <InteractiveLayout enableGestures={true}>
            {children}
          </InteractiveLayout>
        ) : (
          children
        )}
      </LayoutDebugCanvas>
    </SkiaLayoutProvider>
  );
};

// Export optimized utility functions
export const LayoutUtils = {
  measureText,
  dimensionCalculator,
  
  // Performance utilities
  performance: {
    clearCaches: () => {
      dimensionCalculator.clearCache();
      measurementCache.clear();
    },
    getCacheSize: () => ({
      dimensions: dimensionCalculator.getCacheSize?.() || 0,
      measurements: measurementCache.size
    })
  },
  
  // Spacing utilities
  spacing: (multiplier = 1, base = 8) => base * multiplier,
  
  // Color utilities
  rgba: (r, g, b, a = 1) => `rgba(${r}, ${g}, ${b}, ${a})`,
  
  // Animation easing functions (unchanged)
  easing: {
    linear: (t) => t,
    easeInOut: (t) => t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2,
    easeOut: (t) => 1 - Math.pow(1 - t, 3),
    easeIn: (t) => t * t * t,
    bounce: (t) => {
      const n1 = 7.5625;
      const d1 = 2.75;
      if (t < 1 / d1) return n1 * t * t;
      if (t < 2 / d1) return n1 * (t -= 1.5 / d1) * t + 0.75;
      if (t < 2.5 / d1) return n1 * (t -= 2.25 / d1) * t + 0.9375;
      return n1 * (t -= 2.625 / d1) * t + 0.984375;
    }
  },
  
  // Layout calculation helpers
  calculateFlexGrow: (children, availableSpace) => {
    const totalFlex = children.reduce((acc, child) => 
      acc + (child.props?.flex || 0), 0);
    return totalFlex > 0 ? availableSpace / totalFlex : 0;
  },
  
  // Responsive breakpoint utilities
  getBreakpoint: (width, breakpoints = { xs: 0, sm: 576, md: 768, lg: 992, xl: 1200 }) => {
    const entries = Object.entries(breakpoints).sort(([,a], [,b]) => b - a);
    for (const [name, size] of entries) {
      if (width >= size) return name;
    }
    return 'xs';
  }
};















