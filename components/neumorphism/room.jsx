// components/NeumorphicControlPanel.js
import React from 'react';
import { Canvas, Text, useFonts, matchFont } from '@shopify/react-native-skia';
import { StyleSheet, View } from 'react-native';
import { useNeumorphicStyle } from './useNeumorphicStyle.js';
import NeumorphicPanel from './NeumorphicPanel';
import NeumorphicSlider from './NeumorphicSlider';
import NeumorphicToggle from './NeumorphicToggle';
import NeumorphicButton from './NeumorphicButton';
import ThemeButtonIcon from './ThemeButtonIcon';

const NeumorphicControlPanel = ({
  roomName = "Living Room",
  theme = 'light',
  onSliderChange,
  onThemeSelect,
  initialSliderValue = 0.5,
  initialToggleState = false,
  transitionDuration = 200
}) => {
  const colors = useNeumorphicStyle(theme);
  
  // Load fonts using useFonts
  const fontMgr = useFonts({
    Montserrat: [
      require("../../assets/montserrat/Montserrat-Regular.ttf"),
    ]
  });

  if (!fontMgr) {
    return null;
  }
  const onToggleChange = (v) => {
    console.log(v);
    
  }
  // Create font styles and match fonts
  const titleFontStyle = {
    fontFamily: "Montserrat",
    fontWeight: "800", // or "bold" if 800 doesn't work
    fontSize: 20
  };

  const labelFontStyle = {
    fontFamily: "Montserrat",
    fontWeight: "400", // or "normal"
    fontSize: 14
  };

  const titleFont = matchFont(titleFontStyle, fontMgr);
  const labelFont = matchFont(labelFontStyle, fontMgr);

  // Layout constants
  const maxBlur = 12;
  const shadowPadding = Math.ceil(maxBlur * 3);
  const panelWidth = 320;
  const panelHeight = 200;
  const canvasWidth = panelWidth + (2 * shadowPadding);
  const canvasHeight = panelHeight + (2 * shadowPadding);

  // Component positions
  const panelX = shadowPadding;
  const panelY = shadowPadding;
  const roomNameX = panelX + 20;
  const roomNameY = panelY + 60;
  const toggleX = panelX + panelWidth - 70;
  const toggleY = panelY + 60;
  const sliderX = panelX + 30;
  const sliderY = panelY + 150;
  const themeButtonX = panelX + panelWidth - 35;
  const themeButtonY = panelY + panelHeight - 40;

  return (
    <View style={styles.container}>
      <Canvas style={{ width: canvasWidth, height: canvasHeight }}>
        {/* Main Panel */}
        <NeumorphicPanel
          x={panelX}
          y={panelY}
          width={panelWidth}
          height={panelHeight}
          borderRadius={20}
          theme={theme}
        />

        {/* Room Name Title with font weight 800 */}
        <Text
          x={roomNameX}
          y={roomNameY}
          text={roomName}
          font={titleFont}
          color={colors.textPrimary || "black"}
        />

        {/* Toggle */}
        <NeumorphicToggle
          x={toggleX}
          y={roomNameY-15}
          width={50}
          height={24}
          thumbSize={20}
          value={initialToggleState}
          onValueChange={onToggleChange}
          theme={theme}
          transitionDuration={transitionDuration}
        />

        {/* Slider */}
        <NeumorphicSlider
          x={sliderX}
          y={sliderY}
          width={215}
          height={16}
          thumbRadius={12}
          value={initialSliderValue}
          onValueChange={onSliderChange}
          theme={theme}
          transitionDuration={transitionDuration}
        />

        {/* Theme Button */}
        <NeumorphicButton
          x={themeButtonX}
          y={themeButtonY}
          radius={15}
          onPress={onThemeSelect}
          theme={theme}
          transitionDuration={transitionDuration}
        >
          <ThemeButtonIcon
            centerX={themeButtonX}
            centerY={themeButtonY}
            theme={theme}
          />
        </NeumorphicButton>
      </Canvas>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'relative',
  },
});

export default NeumorphicControlPanel;
