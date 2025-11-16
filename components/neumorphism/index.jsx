import React, {useState, useEffect, useRef, useMemo} from 'react';
import { Canvas, 
    Fill, 
    RoundedRect, 
    Shadow, 
    Text, 
    useFont, 
    Paint, 
    Skia,
    Circle,
    Path
} from '@shopify/react-native-skia';
import { StyleSheet, View, RNTextInput } from 'react-native';
import { themes } from "../themes/neumorphic.js";
import { Gesture, GestureDetector } from 'react-native-gesture-handler';
import { runOnJS, useSharedValue, Easing, withTiming, useDerivedValue, interpolate } from 'react-native-reanimated';
// Le require est crucial ici
const fontPath = require("../../assets/Montserrat-VariableFont_wght.ttf");

const Button = ({ title, theme = 'light', onPress, disabled = false, transitionDuration = 150 }) => {
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const textColor = colors.text;

  const font = useFont(fontPath, 16);
  const pressedProgress = useSharedValue(0);
  const gestureActive = useSharedValue(false);

  const maxBlur = 10;
  const shadowPadding = Math.ceil(maxBlur * 3);
  const canvasWidth = 120 + (2 * shadowPadding);
  const canvasHeight = 40 + (2 * shadowPadding);
  const baseButtonX = shadowPadding;
  const baseButtonY = shadowPadding;
  const buttonWidth = 120;
  const buttonHeight = 40;
const paragraphStyle = useMemo(() => {
  return {
    textAlign: 'center',
    fontSize: 16,
    fontFamilies: [fontPath],
  };
}, []);
  const animateToPressed = (isPressed) => {
    'worklet';
    if (gestureActive.value === isPressed) return; // Prevent redundant animations
    
    gestureActive.value = isPressed;
    pressedProgress.value = withTiming(isPressed ? 1 : 0, {
      duration: transitionDuration,
      easing: Easing.out(Easing.cubic),
    });
  };

  const animatedProps = useDerivedValue(() => {
    const progress = pressedProgress.value;

    return {
      x: baseButtonX,
      y: baseButtonY,
      shadowDarkDx: interpolate(progress, [0, 1], [5, -3]),
      shadowDarkDy: interpolate(progress, [0, 1], [5, -3]),
      shadowDarkBlur: interpolate(progress, [0, 1], [10, 6]),
      shadowLightDx: interpolate(progress, [0, 1], [-5, 3]),
      shadowLightDy: interpolate(progress, [0, 1], [-5, 3]),
      shadowLightBlur: interpolate(progress, [0, 1], [10, 6]),
      shadowOpacity: interpolate(progress, [0, 1], [1, 0.6]),
      textX: baseButtonX + (buttonWidth / 2),
      textY: baseButtonY + (buttonHeight / 2) + 6,
    };
  });

  const shadowDarkWithAlpha = useDerivedValue(() => {
    const alpha = Math.round(animatedProps.value.shadowOpacity * 255);
    return `${shadowDark}${alpha.toString(16).padStart(2, '0')}`;
  });

  const shadowLightWithAlpha = useDerivedValue(() => {
    const alpha = Math.round(animatedProps.value.shadowOpacity * 255);
    return `${shadowLight}${alpha.toString(16).padStart(2, '0')}`;
  });

  // Android-specific: Use single tap gesture to avoid race conditions
  const tapGesture = Gesture.Tap()
    .enabled(!disabled)
    .onTouchesDown(() => {
      'worklet';
      animateToPressed(true);
    })
    .onTouchesUp(() => {
      'worklet';
      animateToPressed(false);
    })
    .onEnd(() => {
      'worklet';
      animateToPressed(false);
      if (onPress) {
        runOnJS(onPress)();
      }
    })
    .onTouchesCancelled(() => {
      'worklet';
      animateToPressed(false);
    });

  // Simplified pan gesture for Android
  const panGesture = Gesture.Pan()
    .enabled(!disabled)
    .onStart(() => {
      'worklet';
      animateToPressed(true);
    })
    .onUpdate((event) => {
      'worklet';
      const isWithinBounds =
        event.x >= 0 &&
        event.x <= canvasWidth &&
        event.y >= 0 &&
        event.y <= canvasHeight;

      // Only animate if state actually changes
      if (gestureActive.value !== isWithinBounds) {
        animateToPressed(isWithinBounds);
      }
    })
    .onEnd((event) => {
      'worklet';
      const isWithinBounds =
        event.x >= 0 &&
        event.x <= canvasWidth &&
        event.y >= 0 &&
        event.y <= canvasHeight;

      animateToPressed(false);
      
      if (isWithinBounds && onPress) {
        runOnJS(onPress)();
      }
    })
    .onFinalize(() => {
      'worklet';
      animateToPressed(false);
    });

  const combinedGesture = Gesture.Race(tapGesture, panGesture);

  if (!font) return null;

  return (
    <GestureDetector gesture={combinedGesture}>
      <Canvas style={{
        width: canvasWidth,
        height: canvasHeight,
        opacity: disabled ? 0.5 : 1
      }}>

        <RoundedRect
          x={animatedProps.value.x}
          y={animatedProps.value.y}
          width={buttonWidth}
          height={buttonHeight}
          r={20}
          color={bg}
        >
          <Shadow
            dx={animatedProps.value.shadowDarkDx}
            dy={animatedProps.value.shadowDarkDy}
            blur={animatedProps.value.shadowDarkBlur}
            color={shadowDarkWithAlpha}
          />
          <Shadow
            dx={animatedProps.value.shadowLightDx}
            dy={animatedProps.value.shadowLightDy}
            blur={animatedProps.value.shadowLightBlur}
            color={shadowLightWithAlpha}
          />
        </RoundedRect>

        <Text
  x={0}
  y={0}
  text={title}
  font={font}
  color={textColor}
  transform={[
    { translateX: animatedProps.value.textX/2 },
    { translateY: animatedProps.value.textY }
  ]}
  origin={{ x: 0, y: 0 }}
/>
      </Canvas>
    </GestureDetector>
  );
};

const RadialSlider = ({ value: initialValue = 0, size = 200, theme = 'light', onValueChange }) => {
  const [value, setValue] = useState(initialValue);
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;

  const shadowPadding = 40;
  const canvasSize = size + shadowPadding * 2;
  const centerOffset = shadowPadding;

  const radius = size / 2 - 10;
  const startAngle = -150 * Math.PI / 180;
  const endAngle = 150 * Math.PI / 180;
  const arcRange = endAngle - startAngle;

  const centerX = size / 2 + centerOffset;
  const centerY = size / 2 + centerOffset;

  // Use shared value for internal slider state
  const sliderValue = useSharedValue(initialValue);
  // Track the last valid value to prevent jumps
  const lastValidValue = useSharedValue(initialValue);

  const angle = useDerivedValue(() => {
    return startAngle + (sliderValue.value / 100) * arcRange;
  });

  const cx = useDerivedValue(() => {
    return centerX + (radius / 8 * 7) * Math.cos(angle.value);
  });

  const cy = useDerivedValue(() => {
    return centerY + (radius / 8 * 7) * Math.sin(angle.value);
  });

  const arcPath = Skia.Path.Make();
  arcPath.addArc(
    { x: centerX - radius, y: centerY - radius, width: radius * 2, height: radius * 2 },
    startAngle * 180 / Math.PI,
    300
  );

  const progressPath = useDerivedValue(() => {
    const progressAngle = (sliderValue.value / 100) * 300;
    const path = Skia.Path.Make();
    path.addArc(
      { x: centerX - (radius / 8 * 7), y: centerY - (radius / 8 * 7), width: (radius / 8 * 7) * 2, height: (radius / 8 * 7) * 2 },
      -150,
      progressAngle
    );
    return path;
  });

  const calculateAngle = (x, y) => {
    'worklet';
    const dx = x - centerX;
    const dy = y - centerY;
    const distance = Math.sqrt(dx * dx + dy * dy);
    const normalizedX = dx / distance;
    const normalizedY = dy / distance;
    let calculatedAngle = Math.atan2(normalizedY, normalizedX);
    
    // Handle angle wrapping for anti-jump protection
    if (calculatedAngle < startAngle) {
      calculatedAngle = startAngle;
    } else if (calculatedAngle > endAngle) {
      calculatedAngle = endAngle;
    }
    
    return calculatedAngle;
  };

  const calculateValue = (angle) => {
    'worklet';
    const normalizedAngle = (angle - startAngle) / arcRange;
    return Math.min(Math.max(normalizedAngle * 100, 0), 100);
  };

  const isValidValueTransition = (currentValue, newValue) => {
    'worklet';
    const JUMP_THRESHOLD = 50; // Prevent jumps larger than 50%
    const valueDiff = Math.abs(newValue - currentValue);
    
    // Allow transition if difference is reasonable
    if (valueDiff <= JUMP_THRESHOLD) {
      return true;
    }
    
    // Check if we're at the boundaries and the jump is expected
    const isAtMinBoundary = currentValue <= 5 && newValue >= 95;
    const isAtMaxBoundary = currentValue >= 95 && newValue <= 5;
    
    // Reject jumps at boundaries (anti-jump protection)
    return !isAtMinBoundary && !isAtMaxBoundary;
  };

  const panGesture = Gesture.Pan()
    .onUpdate((event) => {
      const angle = calculateAngle(event.x, event.y);
      const newValue = calculateValue(angle);
      
      // Apply anti-jump protection
      if (isValidValueTransition(lastValidValue.value, newValue)) {
        sliderValue.value = newValue;
        lastValidValue.value = newValue;
      }
      // If invalid transition, keep current value (no update)
    })
    .onEnd(() => {
      // Use runOnJS to bridge back to JS thread for React state updates
      runOnJS(setValue)(sliderValue.value);
      if (onValueChange) {
        runOnJS(onValueChange)(sliderValue.value);
      }
    });

  // Sync external value changes to internal shared value
  useEffect(() => {
    sliderValue.value = initialValue;
    lastValidValue.value = initialValue;
  }, [initialValue]);

  return (
    <GestureDetector gesture={panGesture}>
      <Canvas style={{ width: canvasSize, height: canvasSize }}>
        <Circle cx={centerX} cy={centerY} r={radius + 15} color={bg}>
          <Shadow dx={8} dy={8} blur={15} color={shadowDark} />
          <Shadow dx={-8} dy={-8} blur={15} color={shadowLight} />
        </Circle>

        <Path
          path={arcPath}
          style="stroke"
          strokeWidth={8}
          color={'#E0E0E0'}
          strokeCap="round"
        />

        <Path
          path={progressPath}
          style="stroke"
          strokeWidth={8}
          color={colors.shadowDark}
          strokeCap="round"
        />

        <Circle cx={cx} cy={cy} r={12} color={colors.background}>
          <Shadow dx={4} dy={4} blur={8} color={shadowLight} />
          <Shadow dx={-2} dy={-2} blur={6} color={shadowDark} />
        </Circle>
      </Canvas>
    </GestureDetector>
  );
};


const LinearSlider = ({ 
  value: initialValue = 50, 
  width = 300, 
  height = 60, 
  theme = 'light',
  onValueChange 
}) => {
  const [value, setValue] = useState(initialValue);
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const trackHeight = 10;
  const thumbRadius = 15;
  
  // Shadow configurations
  const bgShadow = { offset: 4, blur: 8 };
  const thumbShadow = { offset: 3, blur: 5 };
  
  // Calculate shadow extents (using 3x blur for proper coverage)
  const bgShadowExtent = Math.abs(bgShadow.offset) + (bgShadow.blur * 3);
  const thumbShadowExtent = Math.abs(thumbShadow.offset) + (thumbShadow.blur * 3);
  
  // Layout calculations
  const thumbCenterY = height / 2;
  const thumbTrackPadding = 10;
  
  // Horizontal padding: account for thumb movement range + shadows
  const thumbTravelDistance = thumbTrackPadding + thumbRadius;
  const paddingHorizontal = Math.max(bgShadowExtent, thumbTravelDistance + thumbShadowExtent);
  
  // Vertical padding: account for thumb overhang + shadows
  const thumbOverhangTop = Math.max(0, thumbRadius - thumbCenterY);
  const thumbOverhangBottom = Math.max(0, thumbRadius - (height - thumbCenterY));
  
  const paddingTop = Math.max(bgShadowExtent, thumbOverhangTop + thumbShadowExtent);
  const paddingBottom = Math.max(bgShadowExtent, thumbOverhangBottom + thumbShadowExtent);
  
  // Canvas and element positions
  const canvasWidth = width + (paddingHorizontal * 2);
  const canvasHeight = height + paddingTop + paddingBottom;
  
  const backgroundX = paddingHorizontal;
  const backgroundY = paddingTop;
  
  const trackX = backgroundX + thumbTrackPadding;
  const trackY = backgroundY + (height - trackHeight) / 2;
  const trackWidth = width - (thumbTrackPadding * 2);
  
  // Gesture-related shared values
  const sliderValue = useSharedValue(initialValue);
  const lastValidValue = useSharedValue(initialValue);

  // Separate shared value for immediate thumb position updates
  const thumbPosition = useSharedValue(trackX + (trackWidth * (initialValue / 100)));

  const thumbX = useDerivedValue(() => {
    return thumbPosition.value;
  });

  const progressWidth = useDerivedValue(() => {
    return (thumbPosition.value - trackX);
  });

  const thumbY = backgroundY + thumbCenterY;

  const calculateValue = (x) => {
    'worklet';
    // Clamp x to track bounds
    const clampedX = Math.max(trackX, Math.min(x, trackX + trackWidth));
    
    // Calculate normalized position (0-1)
    const normalizedPosition = (clampedX - trackX) / trackWidth;
    
    // Convert to percentage (0-100)
    return Math.min(Math.max(normalizedPosition * 100, 0), 100);
  };

  const calculateThumbPosition = (value) => {
    'worklet';
    return trackX + (trackWidth * (value / 100));
  };

  const isValidValueTransition = (currentValue, newValue) => {
    'worklet';
    const JUMP_THRESHOLD = 50; // Prevent jumps larger than 50%
    const valueDiff = Math.abs(newValue - currentValue);
    
    // Allow transition if difference is reasonable
    if (valueDiff <= JUMP_THRESHOLD) {
      return true;
    }
    
    // For linear slider, boundary jumps are less likely but still protect
    const isAtMinBoundary = currentValue <= 5 && newValue >= 95;
    const isAtMaxBoundary = currentValue >= 95 && newValue <= 5;
    
    return !isAtMinBoundary && !isAtMaxBoundary;
  };

  const panGesture = Gesture.Pan()
    .onUpdate((event) => {
      const newValue = calculateValue(event.x);
      
      // Apply anti-jump protection
      if (isValidValueTransition(lastValidValue.value, newValue)) {
        sliderValue.value = newValue;
        lastValidValue.value = newValue;
        // Update thumb position immediately for visual feedback
        thumbPosition.value = calculateThumbPosition(newValue);
      }
    })
    .onEnd(() => {
      // Bridge back to JS thread for React state updates
      runOnJS(setValue)(sliderValue.value);
      if (onValueChange) {
        runOnJS(onValueChange)(sliderValue.value);
      }
    });

  // Sync external value changes to internal shared values
  useEffect(() => {
    sliderValue.value = initialValue;
    lastValidValue.value = initialValue;
    thumbPosition.value = calculateThumbPosition(initialValue);
  }, [initialValue]);

  return (
    <GestureDetector gesture={panGesture}>
      <Canvas style={{ width: canvasWidth, height: canvasHeight }}>
        {/* Background with neumorphic shadows */}
        <RoundedRect 
          x={backgroundX} 
          y={backgroundY} 
          width={width} 
          height={height} 
          r={height / 2} 
          color={bg}
        >
          <Shadow dx={bgShadow.offset} dy={bgShadow.offset} blur={bgShadow.blur} color={shadowDark} />
          <Shadow dx={-bgShadow.offset} dy={-bgShadow.offset} blur={bgShadow.blur} color={shadowLight} />
        </RoundedRect>

        {/* Track */}
        <RoundedRect
          x={trackX} 
          y={trackY}
          width={trackWidth} 
          height={trackHeight}
          r={trackHeight / 2} 
          color={'#CCCCCC'}
        />

        {/* Progress track */}
        <RoundedRect
          x={trackX} 
          y={trackY}
          width={progressWidth} 
          height={trackHeight}
          r={trackHeight / 2} 
          color={shadowDark}
          opacity={0.7}
        />

        {/* Thumb with neumorphic shadows */}
        <Circle 
          cx={thumbX} 
          cy={thumbY} 
          r={thumbRadius} 
          color={bg}
        >
          <Shadow dx={thumbShadow.offset} dy={thumbShadow.offset} blur={thumbShadow.blur} color={shadowDark} />
          <Shadow dx={-thumbShadow.offset} dy={-thumbShadow.offset} blur={thumbShadow.blur} color={shadowLight} />
        </Circle>
      </Canvas>
    </GestureDetector>
  );
};


const TextInput = ({ 
  placeholder = 'Entrez du texte', 
  value: externalValue = '',
  width = 300, 
  height = 50, 
  theme = 'light',
  onChangeText,
  onFocus,
  onBlur,
  ...props
}) => {
  const font = useFont(fontPath, 16); // Make sure fontPath is imported/defined
  const colors = themes[theme]; // Make sure themes is imported/defined
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const textColor = colors.text;
  const radius = 15;
  
  const [value, setValue] = useState(externalValue);
  const [isFocused, setIsFocused] = useState(false);
  const inputRef = useRef(null);

  useEffect(() => {
    setValue(externalValue);
  }, [externalValue]);

  const handleChangeText = (text) => {
    setValue(text);
    if (onChangeText) {
      onChangeText(text);
    }
  };

  const handleFocus = (e) => {
    setIsFocused(true);
    if (onFocus) {
      onFocus(e);
    }
  };

  const handleBlur = (e) => {
    setIsFocused(false);
    if (onBlur) {
      onBlur(e);
    }
  };

  const handlePress = () => {
    if (inputRef.current) {
      inputRef.current.focus();
    }
  };

  return (
    <View style={[styles.container, { width, height }]}>
      {/* Skia Canvas for neumorphic styling */}
      <Canvas style={[StyleSheet.absoluteFillObject]} onTouchStart={handlePress}>
        <RoundedRect x={0} y={0} width={width} height={height} r={radius} color={bg}>
          <Shadow dx={4} dy={4} blur={6} color={shadowDark} inner />
          <Shadow dx={-4} dy={-4} blur={6} color={shadowLight} inner />
        </RoundedRect>
        
        {/* Show placeholder only when no value */}
        {!value && font && (
          <Text
            x={20} 
            y={height/2 + 5}
            text={placeholder}
            font={font}
            color={textColor}
            opacity={0.5}
          />
        )}
      </Canvas>

      {/* Functional TextInput */}
      <RNTextInput
        ref={inputRef}
        style={[
          styles.textInput,
          {
            width: width - 40,
            height: height - 20,
            color: textColor,
          }
        ]}
        value={value}
        onChangeText={handleChangeText}
        onFocus={handleFocus}
        onBlur={handleBlur}
        placeholder=""
        placeholderTextColor="transparent"
        {...props}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'relative',
  },
  textInput: {
    position: 'absolute',
    top: 10,
    left: 20,
    backgroundColor: 'transparent',
    borderWidth: 0,
    fontSize: 16,
    padding: 0,
    margin: 0,
  },
});

export default TextInput;


const NeumorphicCard = ({ title = 'Titre', content = 'Contenu de la carte...', width = 280, height = 180, theme = 'light' }) => {
  const font = useFont(fontPath);
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;

  // Shadow configuration
  const shadowConfig = {
    offset: 6,
    blur: 12
  };

  // Calculate the maximum extent of shadows in each direction
  // A shadow extends by: |offset| + (blur * 3) for proper coverage
  // The blur spreads approximately 3x its radius for full visual coverage
  const shadowExtent = Math.abs(shadowConfig.offset) + (shadowConfig.blur * 3); // 6 + (12 * 3) = 42
  
  // Canvas needs to accommodate the card plus shadow extent on all sides
  const canvasWidth = width + (shadowExtent * 2);
  const canvasHeight = height + (shadowExtent * 2);
  
  // Center the card within the canvas
  const cardX = shadowExtent;
  const cardY = shadowExtent;

  return (
    <Canvas style={{ width: canvasWidth, height: canvasHeight }}>
      <RoundedRect x={cardX} y={cardY} width={width} height={height} r={20} color={bg}>
        {/* Bordure légère (stroke) */}
        <Paint color="#CCCCCC" style="stroke" strokeWidth={1} />
        {/* Ombres externes pour relief */}
        <Shadow dx={shadowConfig.offset} dy={shadowConfig.offset} blur={shadowConfig.blur} color={shadowDark} />
        <Shadow dx={-shadowConfig.offset} dy={-shadowConfig.offset} blur={shadowConfig.blur} color={shadowLight} />
      </RoundedRect>

      {/* Contenu texte de la carte */}
      <Text x={cardX + 20} y={cardY + 50} text={title} font={font} color={colors.text} />
      <Text x={cardX + 20} y={cardY + 80} text={content} font={font} color={colors.text} />
    </Canvas>
  );
};

const NeumorphicToggle = ({ 
  on = false, 
  label = 'Toggle', 
  width = 120, 
  height = 50, 
  theme = 'light' 
}) => {
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const textColor = colors.text;
  const font = useFont(fontPath);

  // Calcul de l'extension du blur (3-sigma rule)
  const blurRadius = 6;
  const blurExtension = blurRadius * 3;
  const shadowOffset = 4;
  
  // Dimensions du canvas pour éviter le clipping
  const canvasWidth = width + (blurExtension * 2) + shadowOffset;
  const canvasHeight = height + (blurExtension * 2) + shadowOffset;
  
  // Position ajustée du rectangle principal
  const rectX = blurExtension;
  const rectY = blurExtension;

  // Vérifier que la font est chargée
  if (!font) {
    return null; // ou un loader
  }

  return (
    <Canvas style={{ width: canvasWidth, height: canvasHeight }}>
      <RoundedRect x={rectX} y={rectY} width={width} height={height} r={25} color={bg}>
        {/* Ombres variables selon état */}
        {on ? (
          <>
            <Shadow dx={-4} dy={-4} blur={6} color={shadowLight} />
            <Shadow dx={4} dy={4} blur={6} color={shadowDark} />
          </>
        ) : (
          <>
            <Shadow dx={4} dy={4} blur={6} color={shadowDark} />
            <Shadow dx={-4} dy={-4} blur={6} color={shadowLight} />
          </>
        )}
      </RoundedRect>
      <Text 
        x={rectX + (width / 2)} 
        y={rectY + (height / 2) + 5} 
        text={label || ''} 
        font={font} 
        color={textColor}
      />
    </Canvas>
  );
};

const NeumorphicSwitch = ({ on = false, width = 80, height = 40, theme = 'light' }) => {
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const r = height / 2;
  
  // Calculate padding needed for shadows
  // Using 3-sigma rule: blur of 8 extends ~24px, blur of 5 extends ~15px
  const maxBlur = 8;
  const shadowPadding = Math.ceil(maxBlur * 3); // 24px padding
  
  // Canvas dimensions with shadow padding
  const canvasWidth = width + (shadowPadding * 2);
  const canvasHeight = height + (shadowPadding * 2);
  
  // Offset positions to account for padding
  const offsetX = shadowPadding;
  const offsetY = shadowPadding;
  
  // Position du cercle (gauche ou droite) - adjusted for offset
  const cx = on ? offsetX + width - r - 5 : offsetX + r + 5;
  
  return (
    <Canvas style={{ width: canvasWidth, height: canvasHeight }}>
      <RoundedRect 
        x={offsetX} 
        y={offsetY} 
        width={width} 
        height={height} 
        r={r} 
        color={bg}
      >
        {/* Fond apparent */}
        <Shadow dx={4} dy={4} blur={8} color={shadowDark} />
        <Shadow dx={-4} dy={-4} blur={8} color={shadowLight} />
      </RoundedRect>
      
      <Circle 
        cx={cx} 
        cy={offsetY + height/2} 
        r={r - 5} 
        color={bg}
      >
        {/* Cercle intérieur pour le cadre du switch */}
        <Shadow dx={3} dy={3} blur={5} color={shadowDark} />
        <Shadow dx={-3} dy={-3} blur={5} color={shadowLight} />
      </Circle>
    </Canvas>
  );
};

export {Button, RadialSlider, LinearSlider, TextInput, NeumorphicCard, NeumorphicToggle, NeumorphicSwitch }