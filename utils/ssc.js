// ./utils/canvasUtils.js - React Native Compatible
import { Dimensions } from 'react-native';

/**
 * React Native utility function to check if element coordinates are on screen
 * @param {Object} coordinates - {x, y, width, height}
 * @returns {boolean}
 */
export const isElementOnScreen = (coordinates) => {
  const { width: screenWidth, height: screenHeight } = Dimensions.get('window');
  const { x, y, width, height } = coordinates;
  
  return (
    x < screenWidth &&
    (x + width) > 0 &&
    y < screenHeight &&
    (y + height) > 0
  );
};

/**
 * Creates a default canvas child object
 * @param {Object} props 
 * @returns {Object}
 */
export const createCanvasChild = (props) => {
  const {
    id,
    type = 'rectangle',
    x = 0,
    y = 0,
    width = 50,
    height = 50,
    visible = true,
    zIndex = 0,
    color = '#3b82f6',
    opacity = 1,
    ...otherProps
  } = props;

  return {
    id,
    type,
    x,
    y,
    width,
    height,
    visible,
    zIndex,
    color,
    opacity,
    coordinates: { x, y, width, height, screenX: x, screenY: y },
    props: { color, opacity, ...otherProps }
  };
};

/**
 * Updates canvas coordinates for React Native
 * @param {Object} layoutEvent - React Native layout event
 * @returns {Object}
 */
export const getCanvasCoordinates = (layoutEvent) => {
  if (!layoutEvent) return { x: 0, y: 0, width: 0, height: 0, screenX: 0, screenY: 0 };
  
  const { x, y, width, height } = layoutEvent.nativeEvent.layout;
  return {
    x,
    y,
    width,
    height,
    screenX: x,
    screenY: y
  };
};
