# React Native Skia Layout System Documentation

## Overview

The React Native Skia Layout System is a comprehensive, high-performance layout engine built on top of `@shopify/react-native-skia`. It provides FlexBox-inspired components with advanced features including responsive design, animations, debugging tools, and performance optimization.

## Table of Contents

- [Installation & Setup](#installation--setup)
- [Core Concepts](#core-concepts)
- [API Reference](#api-reference)
- [Layout Components](#layout-components)
- [Advanced Features](#advanced-features)
- [Performance & Debugging](#performance--debugging)
- [Examples](#examples)
- [Best Practices](#best-practices)

## Installation & Setup

### Prerequisites

```bash
npm install @shopify/react-native-skia
# or
yarn add @shopify/react-native-skia
```

### Basic Setup

```jsx
import { Canvas } from '@shopify/react-native-skia';
import { SkiaLayoutRoot, LayoutColumn, LayoutRow } from './skia-layout-system';

export default function App() {
  const canvasRect = { width: 400, height: 600 };
  
  return (
    <Canvas style={{ flex: 1 }}>
      <SkiaLayoutRoot 
        canvasRect={canvasRect}
        debug={false}
        enablePerformanceTracking={false}
      >
        {/* Your layout components here */}
      </SkiaLayoutRoot>
    </Canvas>
  );
}
```

## Core Concepts

### Layout Context

The system uses React Context to provide canvas dimensions, theme, and debug information to all child components.

```jsx
const context = useSkiaLayoutContext();
// Access: canvasWidth, canvasHeight, debug, theme, animations, performance
```

### Theme System

Built-in theming with customizable colors, spacing, and typography:

```jsx
const theme = {
  colors: {
    primary: '#007AFF',
    secondary: '#34C759',
    background: '#F2F2F7',
    text: '#000000'
  },
  spacing: {
    xs: 4, sm: 8, md: 16, lg: 24, xl: 32
  },
  typography: {
    small: 12, body: 16, title: 20, heading: 24
  }
};
```

### Performance Tracking

Monitor render performance with built-in metrics:

```jsx
const { startMeasure, endMeasure, stats } = useLayoutPerformance();
// stats: { avgRenderTime, totalRenders, lastFrameDuration }
```

## API Reference

### SkiaLayoutRoot

The root container that provides context and debugging capabilities.

```jsx
<SkiaLayoutRoot
  canvasRect={{ width: 400, height: 600 }}
  debug={boolean}
  theme={object}
  enablePerformanceTracking={boolean}
  enableAnimations={boolean}
  globalGestures={boolean}
>
  {children}
</SkiaLayoutRoot>
```

**Props:**
- `canvasRect` (required): Canvas dimensions object
- `debug`: Enable debug visualization
- `theme`: Custom theme object
- `enablePerformanceTracking`: Enable performance monitoring
- `enableAnimations`: Enable layout animations
- `globalGestures`: Enable global gesture handling

### SkiaLayoutProvider

Context provider for layout system (used internally by SkiaLayoutRoot).

```jsx
<SkiaLayoutProvider
  canvasRect={object}
  debug={boolean}
  theme={object}
  enablePerformanceTracking={boolean}
>
  {children}
</SkiaLayoutProvider>
```

## Layout Components

### LayoutColumn

Vertical layout container with FlexBox-inspired properties.

```jsx
<LayoutColumn
  spacing={16}
  padding={20}
  align="start" // 'start' | 'center' | 'end' | 'stretch'
  justify="start" // 'start' | 'center' | 'end' | 'space-between' | 'space-around' | 'space-evenly'
  width={300}
  height={400}
  flex={0}
  wrap={false}
  reverse={false}
  animationDuration={300}
>
  {children}
</LayoutColumn>
```

**Properties:**
- `spacing`: Space between children (number)
- `padding`: Internal padding (number)
- `align`: Horizontal alignment of children
- `justify`: Vertical distribution of children
- `width/height`: Container dimensions
- `flex`: Flex grow factor
- `wrap`: Enable wrapping (experimental)
- `reverse`: Reverse child order
- `animationDuration`: Animation transition time

### LayoutRow

Horizontal layout container with advanced wrapping and justification.

```jsx
<LayoutRow
  spacing={12}
  padding={16}
  justify="start" // 'start' | 'center' | 'end' | 'space-between' | 'space-around' | 'space-evenly'
  align="start" // 'start' | 'center' | 'end' | 'stretch'
  width={400}
  height={100}
  wrap={true}
  reverse={false}
  animationDuration={300}
>
  {children}
</LayoutRow>
```

**Wrap Behavior:**
When `wrap={true}`, children that exceed container width automatically flow to the next row.

### LayoutGrid

CSS Grid-inspired layout with automatic sizing.

```jsx
<LayoutGrid
  rows="auto" // number | 'auto'
  columns={3} // number | 'auto'
  gap={16}
  padding={20}
  width={400}
  height={300}
  autoFlow="row" // 'row' | 'column'
  template={null} // Future: CSS Grid template support
>
  {children}
</LayoutGrid>
```

**Auto Sizing:**
- `rows="auto"` or `columns="auto"` automatically calculates optimal grid dimensions
- `autoFlow` determines whether children fill by row or column first

### AbsoluteLayout

Absolute positioning with constraint-based positioning.

```jsx
<AbsoluteLayout
  width={400}
  height={600}
  clipToBounds={true}
  overflow="visible" // 'visible' | 'hidden'
>
  <SomeComponent 
    top={20} 
    left={30} 
    right={40}     // Distance from right edge
    bottom={50}    // Distance from bottom edge
    centerX={0}    // Offset from horizontal center
    centerY={10}   // Offset from vertical center
  />
</AbsoluteLayout>
```

**Positioning Properties:**
- `top/left`: Standard positioning
- `right/bottom`: Distance from container edges
- `centerX/centerY`: Center-based positioning with offset

### FitBoxWrapper

Enhanced FitBox with animation support.

```jsx
<FitBoxWrapper
  src={{ x: 0, y: 0, width: 200, height: 150 }}
  dst={{ x: 50, y: 50, width: 300, height: 200 }}
  mode="contain" // 'cover' | 'fill' | 'contain' | 'fitWidth' | 'fitHeight' | 'none'
  animateResize={true}
  resizeDuration={300}
>
  {children}
</FitBoxWrapper>
```

## Advanced Features

### ResponsiveLayout

Responsive design with breakpoint-based layouts.

```jsx
<ResponsiveLayout
  breakpoints={{ xs: 0, sm: 576, md: 768, lg: 992, xl: 1200 }}
  layouts={{
    xs: <MobileLayout />,
    md: <TabletLayout />,
    lg: <DesktopLayout />
  }}
  fallback={<DefaultLayout />}
  orientation="both" // 'portrait' | 'landscape' | 'both'
/>
```

### InteractiveLayout

Gesture-enabled layout container.

```jsx
<InteractiveLayout
  onTap={(event) => console.log('Tapped at:', event.x, event.y)}
  onPan={(event) => console.log('Pan:', event.translationX, event.translationY)}
  onPinch={(event) => console.log('Pinch:', event.scale)}
  enableGestures={true}
>
  {children}
</InteractiveLayout>
```

### ConstrainedLayout

Layout with size and aspect ratio constraints.

```jsx
<ConstrainedLayout
  constraints={{
    minWidth: 200,
    maxWidth: 500,
    minHeight: 100,
    maxHeight: 300
  }}
  maintainAspectRatio={true}
  aspectRatio={16/9}
>
  {children}
</ConstrainedLayout>
```

### ScrollableLayout

Scrollable container with momentum and bounce effects.

```jsx
<ScrollableLayout
  direction="vertical" // 'vertical' | 'horizontal'
  showScrollbars={true}
  bounces={true}
  pagingEnabled={false}
  contentSize={{ width: 400, height: 1200 }}
>
  {children}
</ScrollableLayout>
```

### ThemedLayout

Theme-aware component wrapper.

```jsx
<ThemedLayout
  variant="primary" // Custom variant
  colorScheme="dark" // 'light' | 'dark'
>
  {children}
</ThemedLayout>
```

## Performance & Debugging

### Debug System

Enable comprehensive debugging visualization:

```jsx
<SkiaLayoutRoot debug={true} enablePerformanceTracking={true}>
  <LayoutDebugCanvas 
    showGrid={true}
    showPerformance={true}
    showRulers={true}
    gridSize={20}
  >
    {children}
  </LayoutDebugCanvas>
</SkiaLayoutRoot>
```

**Debug Features:**
- Bounding box visualization
- Grid overlay with major/minor lines
- Performance metrics display
- Measurement rulers
- Component labels

### Debug Hook

Advanced debugging utilities:

```jsx
const {
  addDebugBox,
  addDebugGuide,
  addMeasurement,
  clearDebug,
  debugData,
  enabled
} = useSkiaLayoutDebug(true);

// Add debug visualizations
addDebugBox(x, y, width, height, 'rgba(255, 0, 0, 0.3)', 'Button');
addDebugGuide(x1, y1, x2, y2, 'rgba(0, 255, 0, 0.5)', 'line');
addMeasurement('Button Width', 120, 'px');
```

### Performance Optimization

#### Virtualization for Large Lists

```jsx
const { visibleRange, totalHeight, offsetY, setScrollTop } = useVirtualization(
  1000, // itemCount
  50,   // itemHeight
  400   // containerHeight
);

// Render only visible items
const visibleItems = items.slice(visibleRange.start, visibleRange.end);
```

#### Animation Performance

```jsx
const { animate, isAnimating, progress } = useLayoutAnimation(true, 300);

// Animate layout changes
animate(
  { x: 0, y: 0, width: 100, height: 100 },
  { x: 50, y: 50, width: 150, height: 150 },
  (interpolated) => {
    // Update layout with interpolated values
  }
);
```

### Layout Calculations

The system includes intelligent dimension calculation:

```jsx
// Automatic text measurement
const textDims = measureText("Hello World", 16, 'system');
// Returns: { width, height, lines }

// Child dimension detection
const childDims = dimensionCalculator.getChildDimensions(
  child, 
  defaultWidth, 
  defaultHeight, 
  parentContext
);
```

## Examples

### Basic Layout Structure

```jsx
<SkiaLayoutRoot canvasRect={{ width: 400, height: 600 }}>
  <LayoutColumn spacing={16} padding={20}>
    
    {/* Header */}
    <Rect width={360} height={60} color="#007AFF" />
    
    {/* Content Row */}
    <LayoutRow spacing={12} justify="space-between">
      <Rect width={170} height={100} color="#34C759" />
      <Rect width={170} height={100} color="#FF9500" />
    </LayoutRow>
    
    {/* Grid Section */}
    <LayoutGrid columns={3} gap={8}>
      <Circle r={25} color="#FF3B30" />
      <Circle r={25} color="#007AFF" />
      <Circle r={25} color="#34C759" />
      <Circle r={25} color="#FF9500" />
      <Circle r={25} color="#5856D6" />
      <Circle r={25} color="#FF2D92" />
    </LayoutGrid>
    
  </LayoutColumn>
</SkiaLayoutRoot>
```

### Responsive Design Example

```jsx
<SkiaLayoutRoot canvasRect={canvasRect} debug={true}>
  <ResponsiveLayout
    breakpoints={{ xs: 0, md: 768 }}
    layouts={{
      xs: (
        <LayoutColumn spacing={12} padding={16}>
          <Text text="Mobile Layout" size={18} />
          {/* Mobile-specific content */}
        </LayoutColumn>
      ),
      md: (
        <LayoutRow spacing={20} padding={32}>
          <LayoutColumn flex={1} spacing={16}>
            <Text text="Desktop Sidebar" size={20} />
          </LayoutColumn>
          <LayoutColumn flex={2} spacing={16}>
            <Text text="Desktop Main Content" size={20} />
          </LayoutColumn>
        </LayoutRow>
      )
    }}
    fallback={<Text text="Loading..." />}
  />
</SkiaLayoutRoot>
```

### Interactive Dashboard

```jsx
<SkiaLayoutRoot 
  canvasRect={{ width: 800, height: 600 }} 
  enablePerformanceTracking={true}
>
  <InteractiveLayout
    onTap={(e) => console.log('Dashboard tapped')}
    enableGestures={true}
  >
    <LayoutColumn spacing={20} padding={24}>
      
      {/* Header with metrics */}
      <LayoutRow justify="space-between" align="center">
        <Text text="Dashboard" size={24} color="#000" />
        <Text text="Live Data" size={14} color="#666" />
      </LayoutRow>
      
      {/* Stats Grid */}
      <LayoutGrid columns={4} gap={16} height={120}>
        {stats.map((stat, i) => (
          <ConstrainedLayout 
            key={i}
            constraints={{ minWidth: 120, minHeight: 100 }}
          >
            <Rect 
              width={120} 
              height={100} 
              color={stat.color} 
              rx={8}
            />
            <Text 
              x={60} 
              y={30} 
              text={stat.value} 
              size={20} 
              color="white"
              textAlign="center"
            />
            <Text 
              x={60} 
              y={55} 
              text={stat.label} 
              size={12} 
              color="white"
              textAlign="center"
            />
          </ConstrainedLayout>
        ))}
      </LayoutGrid>
      
      {/* Chart Area */}
      <FitBoxWrapper
        src={{ x: 0, y: 0, width: 600, height: 300 }}
        dst={{ x: 0, y: 0, width: 752, height: 300 }}
        mode="contain"
      >
        {/* Chart components */}
      </FitBoxWrapper>
      
    </LayoutColumn>
  </InteractiveLayout>
</SkiaLayoutRoot>
```

## Best Practices

### Performance Optimization

1. **Use Dimension Caching**: The system automatically caches child dimensions, but clear cache when needed:
   ```jsx
   useEffect(() => {
     dimensionCalculator.clearCache();
   }, [dataChanged]);
   ```

2. **Enable Performance Tracking in Development**:
   ```jsx
   <SkiaLayoutRoot enablePerformanceTracking={__DEV__}>
   ```

3. **Virtualize Large Lists**: Use `useVirtualization` for lists with >100 items.

4. **Optimize Re-renders**: Use `useMemo` for expensive layout calculations:
   ```jsx
   const layoutProps = useMemo(() => computeLayout(), [dependencies]);
   ```

### Layout Design

1. **Use Semantic Layout Names**: Name your layouts descriptively:
   ```jsx
   <LayoutColumn> {/* Header */}
   <LayoutRow>    {/* Navigation */}
   <LayoutGrid>   {/* Content Grid */}
   ```

2. **Leverage Flex Properties**: Use `flex` prop for responsive sizing:
   ```jsx
   <LayoutRow>
     <SidebarComponent flex={1} />
     <MainContentComponent flex={3} />
   </LayoutRow>
   ```

3. **Combine Layout Types**: Mix different layout components:
   ```jsx
   <LayoutColumn>
     <LayoutRow justify="space-between">
       {/* Header content */}
     </LayoutRow>
     <LayoutGrid columns={3}>
       {/* Grid items */}
     </LayoutGrid>
   </LayoutColumn>
   ```

### Debugging

1. **Use Debug Mode During Development**:
   ```jsx
   <SkiaLayoutRoot debug={__DEV__}>
   ```

2. **Layer Debug Information**: Adjust debug levels for different detail:
   ```jsx
   // debugLevel 1: Basic bounding boxes
   // debugLevel 2: Detailed measurements and labels
   ```

3. **Performance Monitoring**: Track render performance:
   ```jsx
   const { stats } = useLayoutPerformance();
   console.log(`Avg render time: ${stats.avgRenderTime}ms`);
   ```

### Theme Management

1. **Define Global Themes**: Create consistent theme objects:
   ```jsx
   const lightTheme = {
     colors: { /* light colors */ },
     spacing: { /* consistent spacing */ }
   };
   
   const darkTheme = {
     colors: { /* dark colors */ },
     spacing: { /* same spacing */ }
   };
   ```

2. **Use Theme Context**: Access theme throughout components:
   ```jsx
   const { theme } = useSkiaLayoutContext();
   const primaryColor = theme.colors.primary;
   ```

### Animation Guidelines

1. **Keep Animations Smooth**: Use appropriate durations:
   - Quick feedback: 150-200ms
   - Standard transitions: 300ms
   - Slow reveals: 500ms+

2. **Use Easing Functions**: Leverage built-in easing:
   ```jsx
   const eased = LayoutUtils.easing.easeOut(progress);
   ```

3. **Batch Layout Changes**: Combine multiple updates for smooth animations.

## Utility Functions

The `LayoutUtils` object provides helpful utilities:

```jsx
import { LayoutUtils } from './skia-layout-system';

// Spacing utilities
const margin = LayoutUtils.spacing(2); // 16px (2 * 8px base)

// Color utilities
const color = LayoutUtils.rgba(255, 0, 0, 0.5);

// Easing functions
const eased = LayoutUtils.easing.bounce(0.5);

// Responsive utilities
const breakpoint = LayoutUtils.getBreakpoint(width);

// Flex calculations
const flexSpace = LayoutUtils.calculateFlexGrow(children, availableSpace);

// Text measurement
const textSize = LayoutUtils.measureText("Hello", 16, "system");
```

## Migration Guide

### From Basic Skia Components

Replace manual positioning:

```jsx
// Before
<Group>
  <Rect x={0} y={0} width={100} height={50} />
  <Rect x={0} y={60} width={100} height={50} />
  <Rect x={0} y={120} width={100} height={50} />
</Group>

// After  
<LayoutColumn spacing={10}>
  <Rect width={100} height={50} />
  <Rect width={100} height={50} />
  <Rect width={100} height={50} />
</LayoutColumn>
```

### From React Native StyleSheet

Map StyleSheet properties to layout props:

```jsx
// StyleSheet equivalent
const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16
  }
});

// Skia Layout equivalent
<LayoutRow 
  justify="space-between" 
  align="center" 
  padding={16}
>
```

## Troubleshooting

### Common Issues

1. **Children Not Positioning Correctly**
   - Ensure `SkiaLayoutRoot` wraps all layout components
   - Check `canvasRect` dimensions are correct
   - Verify child components accept `x` and `y` props

2. **Performance Issues**
   - Enable performance tracking to identify bottlenecks
   - Use virtualization for large lists
   - Clear dimension cache when data changes significantly

3. **Debug Visualization Not Showing**
   - Ensure `debug={true}` on `SkiaLayoutRoot`
   - Check that `LayoutDebugCanvas` is rendered
   - Verify canvas dimensions are positive

4. **Responsive Layouts Not Switching**
   - Check breakpoint values match canvas width
   - Ensure layout objects are defined for breakpoints
   - Verify `canvasRect` updates when screen size changes

### Performance Tuning

Monitor these metrics for optimal performance:
- Average render time: < 16ms (60 FPS)
- Layout calculation time: < 5ms
- Memory usage: Stable (no continuous growth)

Use the performance tracking tools to identify and resolve bottlenecks:

```jsx
const { stats } = useLayoutPerformance();
if (stats.avgRenderTime > 16) {
  console.warn('Layout performance degraded');
}
```

---

This documentation covers the comprehensive React Native Skia Layout System. For additional examples and advanced use cases, refer to the component implementations and experiment with the debug tools to understand layout behavior.