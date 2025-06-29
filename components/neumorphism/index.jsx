import React from 'react';
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

import { themes } from "../themes/neumorphic.js";

// Le require est crucial ici
const fontPath = require("../../assets/Montserrat-VariableFont_wght.ttf");

const Button = ({ title, theme = 'light', pressed = false }) => {
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const textColor = colors.text;

  const font = useFont(fontPath, 16);
  if (!font) return null;

  // Shadow space calculation using 3-sigma rule
  // Blur of 10 extends approximately 30px in each direction
  const maxBlur = 10;
  const shadowPadding = Math.ceil(maxBlur * 3); // 30px padding
  const canvasWidth = 120 + (2 * shadowPadding);
  const canvasHeight = 40 + (2 * shadowPadding);
  
  // Button positioning - when pressed, we simulate depth by moving slightly
  const buttonX = shadowPadding + (pressed ? 2 : 0); // Slight movement when pressed
  const buttonY = shadowPadding + (pressed ? 2 : 0);
  const buttonWidth = 120;
  const buttonHeight = 40;

  return (
    <Canvas style={{ width: canvasWidth, height: canvasHeight }}>
      {/* Background fill to match the surface the button sits on */}
      <Fill color={bg} />
      
      {pressed ? (
        // Pressed state: create inset appearance with inverted shadows and reduced intensity
        <RoundedRect 
          x={buttonX} 
          y={buttonY} 
          width={buttonWidth} 
          height={buttonHeight} 
          r={20} 
          color={bg}
        >
          {/* When pressed, the button appears sunken - light comes from opposite direction */}
          <Shadow dx={-3} dy={-3} blur={6} color={shadowDark} />
          <Shadow dx={3} dy={3} blur={6} color={shadowLight} />
        </RoundedRect>
      ) : (
        // Normal state: raised appearance with standard neumorphic shadows
        <RoundedRect 
          x={buttonX} 
          y={buttonY} 
          width={buttonWidth} 
          height={buttonHeight} 
          r={20} 
          color={bg}
        >
          {/* Standard neumorphic shadows - light from top-left */}
          <Shadow dx={5} dy={5} blur={10} color={shadowDark} />
          <Shadow dx={-5} dy={-5} blur={10} color={shadowLight} />
        </RoundedRect>
      )}
      
      {/* Text positioning: calculate the exact center without conflicting alignment */}
      <Text
        x={buttonX + (buttonWidth / 2)} // Exact horizontal center
        y={buttonY + (buttonHeight / 2) + 6} // Vertical center with slight adjustment for font baseline
        text={title}
        font={font}
        color={textColor}
        textAlign="center" // This centers the text horizontally around the x coordinate
      />
    </Canvas>
  );
};


const RadialSlider = ({ value = 0, size = 200, theme = 'light' }) => {
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  
  // Calculate required padding based on largest shadow offset + blur + element radius
  // Main circle: radius + 15, shadow offset 8, blur 15 = need 38 padding
  // Cursor: radius 12, shadow offset 4, blur 8 = need 24 padding
  const shadowPadding = 40; // Increased to ensure no clipping
  const canvasSize = size + shadowPadding * 2;
  const centerOffset = shadowPadding;
  
  // Rayon extérieur du cercle
  const radius = size / 2 - 10;
  
  // Arc de 300 degrés : commence à -150° et finit à +150° (300° total)
  const startAngle = -150 * Math.PI / 180; // -150° en radians
  const endAngle = 150 * Math.PI / 180;    // +150° en radians
  const arcRange = endAngle - startAngle;  // 300° en radians
  
  // Calcul de la position du curseur sur l'arc (basé sur la valeur 0-100)
  const angle = startAngle + (value / 100) * arcRange;
  
  // Position du curseur
  const centerX = size / 2 + centerOffset;
  const centerY = size / 2 + centerOffset;
  const cx = centerX + (radius/8*7) * Math.cos(angle);
  const cy = centerY + (radius/8*7) * Math.sin(angle);

  // Création du chemin d'arc de 300°
  const arcPath = Skia.Path.Make();
  arcPath.addArc(
    { x: centerX - radius, y: centerY - radius, width: radius * 2, height: radius * 2 },
    startAngle * 180 / Math.PI, // Skia utilise les degrés
    300 // 300 degrés d'arc
  );

  // Arc de progression (de 0 à la valeur actuelle)
  const progressPath = Skia.Path.Make();
  const progressAngle = (value / 100) * 300; // Progression en degrés
  progressPath.addArc(
    { x: centerX - (radius/8*7), y: centerY - (radius/8*7), width: (radius/8*7) * 2, height: (radius/8*7) * 2 },
    -150, // Commence à -150°
    progressAngle // Jusqu'à la valeur actuelle
  );

  return (
    <Canvas style={{ width: canvasSize, height: canvasSize }}>
      {/* Fond avec effet neumorphique */}
      <Circle cx={centerX} cy={centerY} r={radius + 15} color={bg}>
        <Shadow dx={8} dy={8} blur={15} color={shadowDark} />
        <Shadow dx={-8} dy={-8} blur={15} color={shadowLight} />
      </Circle>

      {/* Arc de fond (300°) */}
      <Path
        path={arcPath}
        style="stroke"
        strokeWidth={8}
        color={'#E0E0E0'}
        strokeCap="round"
      />

      {/* Arc de progression */}
      <Path
        path={progressPath}
        style="stroke"
        strokeWidth={8}
        color={colors.shadowDark}
        strokeCap="round"
      />

      {/* Curseur : un petit cercle positionné sur l'arc */}
      <Circle cx={cx} cy={cy} r={12} color={colors.background}>
        <Shadow dx={4} dy={4} blur={8} color={shadowLight} />
        <Shadow dx={-2} dy={-2} blur={6} color={shadowDark} />
      </Circle>
    </Canvas>
  );
};


const LinearSlider = ({ value = 50, width = 300, height = 60, theme = 'light' }) => {
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
  const bgShadowExtent = Math.abs(bgShadow.offset) + (bgShadow.blur * 3); // 4 + 24 = 28
  const thumbShadowExtent = Math.abs(thumbShadow.offset) + (thumbShadow.blur * 3); // 3 + 15 = 18
  
  // Layout calculations
  const thumbCenterY = height / 2;
  const thumbTrackPadding = 10;
  
  // Horizontal padding: account for thumb movement range + shadows
  const thumbTravelDistance = thumbTrackPadding + thumbRadius; // Max distance from background edge
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
  
  const thumbX = trackX + (trackWidth * (value / 100));
  const thumbY = backgroundY + thumbCenterY;

  return (
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
  );
};


const TextInput = ({ placeholder = 'Entrez du texte', width = 300, height = 50, theme = 'light' }) => {
  const font = useFont(fontPath, 16);
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const radius = 15;

  return (
    <Canvas style={{ width, height }}>
      {/* Fond + champ */}
      <RoundedRect x={0} y={0} width={width} height={height} r={radius} color={bg}>
        {/* Ombre interne pour effet enfoncé */}
        <Shadow dx={4} dy={4} blur={6} color={shadowDark} inner />
        <Shadow dx={-4} dy={-4} blur={6} color={shadowLight} inner />
      </RoundedRect>
      {/* Texte simulé */}
      <Text
        x={20} y={height/2 + 5}
        text={placeholder}
        font={font}
        color={colors.text}
      />
    </Canvas>
  );
};


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