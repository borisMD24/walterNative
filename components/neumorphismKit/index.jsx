import React from 'react';
import {
  Fill,
  RoundedRect,
  Shadow,
  Text,
  useFont,
  Paint,
  Skia,
  Circle,
  Path,
  Group,
} from '@shopify/react-native-skia';

import {themes} from '../themes/neumorphic.js';
import {
  SkiaLayoutRoot,
  LayoutColumn,
  LayoutRow,
  AbsoluteLayout,
  ConstrainedLayout,
  useSkiaLayoutContext,
} from '../skiaLayoutSystem';

// Le require est crucial ici
const fontPath = require('../../assets/Montserrat-VariableFont_wght.ttf');

const Button = ({
  title,
  theme = 'light',
  pressed = false,
  width = 120,
  height = 40,
}) => {
  const colors = themes['light'];

  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const textColor = colors.text;

  const font = useFont(fontPath, 16);
  if (!font) return null;

  // Measure text dimensions for proper centering
  const textWidth = font.measureText(title).width;
  const fontMetrics = font.getMetrics();
  
  // Calculate centered position
  const textX = (width - textWidth) / 2;
  // For vertical centering: use half height minus half the font's effective height
  const textY = height / 2 - (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent;

  // Button positioning - when pressed, we simulate depth by moving slightly
  const offsetX = pressed ? 2 : 0;
  const offsetY = pressed ? 2 : 0;

  return (
    <ConstrainedLayout
      constraints={{
        minWidth: width,
        maxWidth: width,
        minHeight: height,
        maxHeight: height,
      }}>
      <Group>
        <Fill color={'transparent'} />

        <RoundedRect
          x={offsetX}
          y={offsetY}
          width={width}
          height={height}
          r={20}
          color={bg}>
          {pressed ? (
            <>
              <Shadow dx={-3} dy={-3} blur={6} color={shadowDark} />
              <Shadow dx={3} dy={3} blur={6} color={shadowLight} />
            </>
          ) : (
            <>
              <Shadow dx={5} dy={5} blur={10} color={shadowDark} />
              <Shadow dx={-5} dy={-5} blur={10} color={shadowLight} />
            </>
          )}
        </RoundedRect>

        <Text
          x={textX + offsetX} // Apply offset for press effect
          y={textY*4} // Apply offset for press effect
          text={title}
          font={font}
          color={textColor}
        />
      </Group>
    </ConstrainedLayout>
  );
};

// Alternative approach using Group transform for simpler centering
const ButtonAlternative = ({
  title,
  theme = 'light',
  pressed = false,
  width = 120,
  height = 40,
}) => {
  const colors = themes['light'];
  const font = useFont(fontPath, 16);
  if (!font) return null;

  const offsetX = pressed ? 2 : 0;
  const offsetY = pressed ? 2 : 0;

  return (
    <ConstrainedLayout
      constraints={{
        minWidth: width,
        maxWidth: width,
        minHeight: height,
        maxHeight: height,
      }}>
        <Fill color={'transparent'} />

        <RoundedRect
          x={offsetX}
          y={offsetY}
          width={width}
          height={height}
          r={20}
          color={colors.background}>
          {pressed ? (
            <>
              <Shadow dx={-3} dy={-3} blur={6} color={colors.shadowDark} />
              <Shadow dx={3} dy={3} blur={6} color={colors.shadowLight} />
            </>
          ) : (
            <>
              <Shadow dx={5} dy={5} blur={10} color={colors.shadowDark} />
              <Shadow dx={-5} dy={-5} blur={10} color={colors.shadowLight} />
            </>
          )}
        </RoundedRect>

        {/* Use Group with transform for centered positioning */}
          <Text
            x={-font.measureText(title).width / 2}
            y={font.getSize() / 3} // Approximate vertical centering
            text={title}
            font={font}
            color={colors.text}
          transform={[
            { translateX: width / 2 + offsetX },
            { translateY: height / 2 + offsetY }
          ]}
          />
    </ConstrainedLayout>
  );
};
const RadialSlider = ({value = 0, size = 200, theme = 'light'}) => {
  const colors = themes["light"];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;

  // Rayon extérieur du cercle
  const radius = size / 2 - 10;

  // Arc de 300 degrés : commence à -150° et finit à +150° (300° total)
  const startAngle = (-150 * Math.PI) / 180; // -150° en radians
  const endAngle = (150 * Math.PI) / 180; // +150° en radians
  const arcRange = endAngle - startAngle; // 300° en radians

  // Calcul de la position du curseur sur l'arc (basé sur la valeur 0-100)
  const angle = startAngle + (value / 100) * arcRange;

  // Position du curseur
  const centerX = size / 2;
  const centerY = size / 2;
  const cx = centerX + (radius / 8) * 7 * Math.cos(angle);
  const cy = centerY + (radius / 8) * 7 * Math.sin(angle);

  // Création du chemin d'arc de 300°
  const arcPath = Skia.Path.Make();
  arcPath.addArc(
    {
      x: centerX - radius,
      y: centerY - radius,
      width: radius * 2,
      height: radius * 2,
    },
    (startAngle * 180) / Math.PI, // Skia utilise les degrés
    300, // 300 degrés d'arc
  );

  // Arc de progression (de 0 à la valeur actuelle)
  const progressPath = Skia.Path.Make();
  const progressAngle = (value / 100) * 300; // Progression en degrés
  progressPath.addArc(
    {
      x: centerX - (radius / 8) * 7,
      y: centerY - (radius / 8) * 7,
      width: (radius / 8) * 7 * 2,
      height: (radius / 8) * 7 * 2,
    },
    -150, // Commence à -150°
    progressAngle, // Jusqu'à la valeur actuelle
  );

  return (
    <ConstrainedLayout
      constraints={{
        minWidth: size,
        maxWidth: size,
        minHeight: size,
        maxHeight: size,
      }}>
      <AbsoluteLayout width={size} height={size}>
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
      </AbsoluteLayout>
    </ConstrainedLayout>
  );
};

const LinearSlider = ({
  value = 50,
  width = 300,
  height = 60,
  theme = 'light',
}) => {
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const trackHeight = 10;
  const thumbRadius = 15;

  // Shadow configurations
  const bgShadow = {offset: 4, blur: 8};
  const thumbShadow = {offset: 3, blur: 5};

  // Layout calculations
  const thumbCenterY = height / 2;
  const thumbTrackPadding = 10;

  const trackX = thumbTrackPadding;
  const trackY = (height - trackHeight) / 2;
  const trackWidth = width - thumbTrackPadding * 2;

  const thumbX = trackX + trackWidth * (value / 100);
  const thumbY = thumbCenterY;

  return (
    <ConstrainedLayout
      constraints={{
        minWidth: width,
        maxWidth: width,
        minHeight: height,
        maxHeight: height,
      }}>
      <AbsoluteLayout width={width} height={height}>
        {/* Background with neumorphic shadows */}
        <RoundedRect
          x={0}
          y={0}
          width={width}
          height={height}
          r={height / 2}
          color={bg}>
          <Shadow
            dx={bgShadow.offset}
            dy={bgShadow.offset}
            blur={bgShadow.blur}
            color={shadowDark}
          />
          <Shadow
            dx={-bgShadow.offset}
            dy={-bgShadow.offset}
            blur={bgShadow.blur}
            color={shadowLight}
          />
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
        <Circle cx={thumbX} cy={thumbY} r={thumbRadius} color={bg}>
          <Shadow
            dx={thumbShadow.offset}
            dy={thumbShadow.offset}
            blur={thumbShadow.blur}
            color={shadowDark}
          />
          <Shadow
            dx={-thumbShadow.offset}
            dy={-thumbShadow.offset}
            blur={thumbShadow.blur}
            color={shadowLight}
          />
        </Circle>
      </AbsoluteLayout>
    </ConstrainedLayout>
  );
};

const TextInput = ({
  placeholder = 'Entrez du texte',
  width = 300,
  height = 50,
  theme = 'light',
}) => {
  const font = useFont(fontPath, 16);
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const radius = 15;

  return (
    <ConstrainedLayout
      constraints={{
        minWidth: width,
        maxWidth: width,
        minHeight: height,
        maxHeight: height,
      }}>
      <AbsoluteLayout width={width} height={height}>
        {/* Fond + champ */}
        <RoundedRect
          x={0}
          y={0}
          width={width}
          height={height}
          r={radius}
          color={bg}>
          {/* Ombre interne pour effet enfoncé */}
          <Shadow dx={4} dy={4} blur={6} color={shadowDark} inner />
          <Shadow dx={-4} dy={-4} blur={6} color={shadowLight} inner />
        </RoundedRect>
        {/* Texte simulé */}
        <Text
          x={20}
          y={height / 2 + 5}
          text={placeholder}
          font={font}
          color={colors.text}
        />
      </AbsoluteLayout>
    </ConstrainedLayout>
  );
};

const NeumorphicCard = ({
  title = 'Titre',
  content = 'Contenu de la carte...',
  width = 280,
  height = 180,
  theme = 'light',
}) => {
  const font = useFont(fontPath);
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;

  // Shadow configuration
  const shadowConfig = {
    offset: 6,
    blur: 12,
  };

  return (
    <ConstrainedLayout
      constraints={{
        minWidth: width,
        maxWidth: width,
        minHeight: height,
        maxHeight: height,
      }}>
      <AbsoluteLayout width={width} height={height}>
        <RoundedRect
          x={0}
          y={0}
          width={width}
          height={height}
          r={20}
          color={bg}>
          {/* Bordure légère (stroke) */}
          <Paint color="#CCCCCC" style="stroke" strokeWidth={1} />
          {/* Ombres externes pour relief */}
          <Shadow
            dx={shadowConfig.offset}
            dy={shadowConfig.offset}
            blur={shadowConfig.blur}
            color={shadowDark}
          />
          <Shadow
            dx={-shadowConfig.offset}
            dy={-shadowConfig.offset}
            blur={shadowConfig.blur}
            color={shadowLight}
          />
        </RoundedRect>

        {/* Contenu texte de la carte */}
        <Text x={20} y={50} text={title} font={font} color={colors.text} />
        <Text x={20} y={80} text={content} font={font} color={colors.text} />
      </AbsoluteLayout>
    </ConstrainedLayout>
  );
};

const NeumorphicToggle = ({
  on = false,
  label = 'Toggle',
  width = 120,
  height = 50,
  theme = 'light',
}) => {
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const textColor = colors.text;
  const font = useFont(fontPath);

  // Vérifier que la font est chargée
  if (!font) {
    return null; // ou un loader
  }

  return (
    <ConstrainedLayout
      constraints={{
        minWidth: width,
        maxWidth: width,
        minHeight: height,
        maxHeight: height,
      }}>
      <AbsoluteLayout width={width} height={height}>
        <RoundedRect
          x={0}
          y={0}
          width={width}
          height={height}
          r={25}
          color={bg}>
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
          x={width / 2}
          y={height / 2 + 5}
          text={label || ''}
          font={font}
          color={textColor}
          textAlign="center"
        />
      </AbsoluteLayout>
    </ConstrainedLayout>
  );
};

const NeumorphicSwitch = ({
  on = false,
  width = 80,
  height = 40,
  theme = 'light',
}) => {
  const colors = themes[theme];
  const bg = colors.background;
  const shadowLight = colors.shadowLight;
  const shadowDark = colors.shadowDark;
  const r = height / 2;

  // Position du cercle (gauche ou droite)
  const cx = on ? width - r - 5 : r + 5;

  return (
    <ConstrainedLayout
      constraints={{
        minWidth: width,
        maxWidth: width,
        minHeight: height,
        maxHeight: height,
      }}>
      <AbsoluteLayout width={width} height={height}>
        <RoundedRect x={0} y={0} width={width} height={height} r={r} color={bg}>
          {/* Fond apparent */}
          <Shadow dx={4} dy={4} blur={8} color={shadowDark} />
          <Shadow dx={-4} dy={-4} blur={8} color={shadowLight} />
        </RoundedRect>

        <Circle cx={cx} cy={height / 2} r={r - 5} color={bg}>
          {/* Cercle intérieur pour le cadre du switch */}
          <Shadow dx={3} dy={3} blur={5} color={shadowDark} />
          <Shadow dx={-3} dy={-3} blur={5} color={shadowLight} />
        </Circle>
      </AbsoluteLayout>
    </ConstrainedLayout>
  );
};

// Composant utilitaire pour afficher plusieurs composants ensemble
const NeumorphicShowcase = ({theme = 'light'}) => {
  const canvasRect = {width: 400, height: 800};

  return (
    <SkiaLayoutRoot canvasRect={canvasRect} debug={false}>
      <LayoutColumn spacing={20} padding={20} align="center">
        {/* Buttons */}
        <LayoutRow spacing={15}>
          <Button title="Normal" theme={theme} />
          <Button title="Pressed" theme={theme} pressed={true} />
        </LayoutRow>

        {/* Sliders */}
        <LinearSlider value={60} theme={theme} />
        <RadialSlider value={75} size={150} theme={theme} />

        {/* Input */}
        <TextInput placeholder="Tapez ici..." theme={theme} />

        {/* Card */}
        <NeumorphicCard
          title="Carte Exemple"
          content="Contenu de la carte avec style neumorphique"
          theme={theme}
        />

        {/* Toggle and Switch */}
        <LayoutRow spacing={20}>
          <NeumorphicToggle label="Toggle" on={false} theme={theme} />
          <NeumorphicSwitch on={true} theme={theme} />
        </LayoutRow>
      </LayoutColumn>
    </SkiaLayoutRoot>
  );
};

export {
  Button,
  RadialSlider,
  LinearSlider,
  TextInput,
  NeumorphicCard,
  NeumorphicToggle,
  NeumorphicSwitch,
  NeumorphicShowcase,
};
