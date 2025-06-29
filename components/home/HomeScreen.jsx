import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, ScrollView, Dimensions, Animated } from 'react-native';
import { Canvas, RoundedRect, Group, DropShadow, LinearGradient, vec, Circle, Path, Skia, RadialGradient, Blur } from '@shopify/react-native-skia';

const { width: screenWidth, height: screenHeight } = Dimensions.get('window');

// Enhanced color palette with neon accents
const colors = {
  background: '#0A0E1A',
  cardBg: '#1A1F2E',
  lightShadow: '#2A3142',
  darkShadow: '#000000',
  primary: '#00D4FF',
  secondary: '#FF006E',
  accent: '#8B5CF6',
  success: '#00FF88',
  warning: '#FFB800',
  text: '#FFFFFF',
  textSecondary: '#8B949E',
  neon: '#00FFFF'
};

// Animated Neumorphic Button with Glow
const GlowButton = ({ 
  width = 120, 
  height = 50, 
  text = 'Button', 
  color = colors.primary,
  icon = '',
  onPress 
}) => {
  const [pressed, setPressed] = useState(false);
  
  const handlePress = () => {
    setPressed(true);
    setTimeout(() => setPressed(false), 150);
    onPress && onPress();
  };

  return (
    <View style={[styles.buttonContainer, { width, height }]}>
      <Canvas style={{ width, height }}>
        <Group>
          {/* Glow effect */}
          <DropShadow
            dx={0}
            dy={0}
            blur={pressed ? 8 : 15}
            color={color}
            shadowOnly
          >
            <RoundedRect
              x={5}
              y={5}
              width={width - 10}
              height={height - 10}
              r={height / 4}
            />
          </DropShadow>
          
          {/* Outer shadow */}
          <DropShadow
            dx={pressed ? 1 : 3}
            dy={pressed ? 1 : 3}
            blur={pressed ? 3 : 8}
            color={colors.darkShadow}
            shadowOnly
          >
            <RoundedRect
              x={0}
              y={0}
              width={width}
              height={height}
              r={height / 4}
            />
          </DropShadow>
          
          {/* Inner highlight */}
          <DropShadow
            dx={pressed ? -1 : -2}
            dy={pressed ? -1 : -2}
            blur={pressed ? 2 : 4}
            color={colors.lightShadow}
            shadowOnly
          >
            <RoundedRect
              x={0}
              y={0}
              width={width}
              height={height}
              r={height / 4}
            />
          </DropShadow>
          
          {/* Button surface with gradient */}
          <RoundedRect
            x={pressed ? 1 : 0}
            y={pressed ? 1 : 0}
            width={width - (pressed ? 2 : 0)}
            height={height - (pressed ? 2 : 0)}
            r={height / 4}
          >
            <LinearGradient
              start={vec(0, 0)}
              end={vec(width, height)}
              colors={[colors.cardBg, colors.background]}
            />
          </RoundedRect>
          
          {/* Inner glow border */}
          <RoundedRect
            x={2}
            y={2}
            width={width - 4}
            height={height - 4}
            r={height / 4}
            style="stroke"
            strokeWidth={1}
            color={color}
            opacity={0.3}
          />
        </Group>
      </Canvas>
      
      <View style={styles.buttonContent}>
        {icon && <Text style={[styles.buttonIcon, { color }]}>{icon}</Text>}
        <Text style={[styles.buttonText, { color: colors.text }]}>{text}</Text>
      </View>
      
      <View 
        style={[StyleSheet.absoluteFill, styles.buttonTouchArea]}
        onTouchStart={handlePress}
      />
    </View>
  );
};

// Holographic Card Component
const HoloCard = ({ 
  width = 200, 
  height = 150, 
  children,
  glowColor = colors.primary 
}) => {
  return (
    <View style={[styles.cardContainer, { width, height }]}>
      <Canvas style={{ width, height }}>
        <Group>
          {/* Outer glow */}
          <DropShadow
            dx={0}
            dy={0}
            blur={20}
            color={glowColor}
            shadowOnly
          >
            <RoundedRect
              x={10}
              y={10}
              width={width - 20}
              height={height - 20}
              r={16}
            />
          </DropShadow>
          
          {/* Main shadow */}
          <DropShadow
            dx={4}
            dy={4}
            blur={12}
            color={colors.darkShadow}
            shadowOnly
          >
            <RoundedRect
              x={0}
              y={0}
              width={width}
              height={height}
              r={20}
            />
          </DropShadow>
          
          {/* Light reflection */}
          <DropShadow
            dx={-2}
            dy={-2}
            blur={6}
            color={colors.lightShadow}
            shadowOnly
          >
            <RoundedRect
              x={0}
              y={0}
              width={width}
              height={height}
              r={20}
            />
          </DropShadow>
          
          {/* Card surface */}
          <RoundedRect
            x={0}
            y={0}
            width={width}
            height={height}
            r={20}
          >
            <RadialGradient
              c={vec(width / 2, height / 2)}
              r={Math.max(width, height) / 2}
              colors={[colors.cardBg, colors.background]}
            />
          </RoundedRect>
          
          {/* Holographic border */}
          <RoundedRect
            x={1}
            y={1}
            width={width - 2}
            height={height - 2}
            r={19}
            style="stroke"
            strokeWidth={1}
            color={glowColor}
            opacity={0.4}
          />
        </Group>
      </Canvas>
      
      <View style={styles.cardContent}>
        {children}
      </View>
    </View>
  );
};

// Animated Stats Widget
const StatsWidget = ({ title, value, percentage, color, icon }) => {
  const [animatedValue] = useState(new Animated.Value(0));
  
  useEffect(() => {
    Animated.timing(animatedValue, {
      toValue: percentage / 100,
      duration: 2000,
      useNativeDriver: false,
    }).start();
  }, []);

  return (
    <HoloCard width={160} height={120} glowColor={color}>
      <View style={styles.statsContent}>
        <View style={styles.statsHeader}>
          <Text style={styles.statsIcon}>{icon}</Text>
          <Text style={styles.statsTitle}>{title}</Text>
        </View>
        <Text style={[styles.statsValue, { color }]}>{value}</Text>
        <View style={styles.statsProgress}>
          <View style={[styles.progressTrack, { backgroundColor: colors.cardBg }]}>
            <Animated.View 
              style={[
                styles.progressFill, 
                { 
                  backgroundColor: color,
                  width: animatedValue.interpolate({
                    inputRange: [0, 1],
                    outputRange: ['0%', '100%']
                  })
                }
              ]} 
            />
          </View>
          <Text style={styles.percentageText}>{percentage}%</Text>
        </View>
      </View>
    </HoloCard>
  );
};

// Cyber Toggle Switch
const CyberToggle = ({ 
  width = 80, 
  height = 40, 
  isOn = false,
  color = colors.primary,
  onToggle 
}) => {
  const [toggled, setToggled] = useState(isOn);
  const thumbSize = height - 8;
  const thumbPosition = toggled ? width - thumbSize - 4 : 4;
  
  const handleToggle = () => {
    setToggled(!toggled);
    onToggle && onToggle(!toggled);
  };

  return (
    <View style={[styles.toggleContainer, { width, height }]}>
      <Canvas style={{ width, height }}>
        <Group>
          {/* Glow effect when on */}
          {toggled && (
            <DropShadow
              dx={0}
              dy={0}
              blur={12}
              color={color}
              shadowOnly
            >
              <RoundedRect
                x={2}
                y={2}
                width={width - 4}
                height={height - 4}
                r={height / 2}
              />
            </DropShadow>
          )}
          
          {/* Track inset shadow */}
          <DropShadow
            dx={2}
            dy={2}
            blur={4}
            color={colors.darkShadow}
            inner
          >
            <RoundedRect
              x={0}
              y={0}
              width={width}
              height={height}
              r={height / 2}
            />
          </DropShadow>
          
          {/* Track surface */}
          <RoundedRect
            x={0}
            y={0}
            width={width}
            height={height}
            r={height / 2}
          >
            <LinearGradient
              start={vec(0, 0)}
              end={vec(width, 0)}
              colors={toggled ? [color, colors.secondary] : [colors.background, colors.cardBg]}
            />
          </RoundedRect>
          
          {/* Thumb glow */}
          <DropShadow
            dx={0}
            dy={0}
            blur={8}
            color={toggled ? color : colors.lightShadow}
            shadowOnly
          >
            <Circle
              cx={thumbPosition + thumbSize / 2}
              cy={height / 2}
              r={thumbSize / 2}
            />
          </DropShadow>
          
          {/* Thumb shadow */}
          <DropShadow
            dx={2}
            dy={2}
            blur={4}
            color={colors.darkShadow}
            shadowOnly
          >
            <Circle
              cx={thumbPosition + thumbSize / 2}
              cy={height / 2}
              r={thumbSize / 2}
            />
          </DropShadow>
          
          {/* Thumb surface */}
          <Circle
            cx={thumbPosition + thumbSize / 2}
            cy={height / 2}
            r={thumbSize / 2}
          >
            <RadialGradient
              c={vec(thumbPosition + thumbSize / 2, height / 2)}
              r={thumbSize / 2}
              colors={[colors.lightShadow, colors.cardBg]}
            />
          </Circle>
        </Group>
      </Canvas>
      
      <View 
        style={[StyleSheet.absoluteFill, styles.toggleTouchArea]}
        onTouchStart={handleToggle}
      />
    </View>
  );
};

// Main Dashboard Component
export default function SkiaNeumorphicHomeScreen() {
  const [darkMode, setDarkMode] = useState(true);
  const [notifications, setNotifications] = useState(true);
  const [autoMode, setAutoMode] = useState(false);
  
  return (
    <View style={styles.container}>
      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.title}>CYBER DASHBOARD</Text>
          <Text style={styles.subtitle}>Neural Interface v2.1</Text>
          <View style={styles.headerLine} />
        </View>
        
        {/* System Status */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>🔮 SYSTEM STATUS</Text>
          <View style={styles.statsGrid}>
            <StatsWidget 
              title="CPU" 
              value="68%" 
              percentage={68} 
              color={colors.primary} 
              icon="⚡" 
            />
            <StatsWidget 
              title="Memory" 
              value="4.2GB" 
              percentage={84} 
              color={colors.warning} 
              icon="🧠" 
            />
            <StatsWidget 
              title="Storage" 
              value="256GB" 
              percentage={45} 
              color={colors.success} 
              icon="💾" 
            />
            <StatsWidget 
              title="Network" 
              value="1.2GB/s" 
              percentage={92} 
              color={colors.secondary} 
              icon="🌐" 
            />
          </View>
        </View>
        
        {/* Control Panel */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>⚙️ CONTROL MATRIX</Text>
          <HoloCard width={screenWidth - 40} height={200} glowColor={colors.accent}>
            <View style={styles.controlPanel}>
              <View style={styles.controlRow}>
                <View style={styles.controlItem}>
                  <Text style={styles.controlLabel}>Dark Mode</Text>
                  <CyberToggle 
                    isOn={darkMode} 
                    onToggle={setDarkMode}
                    color={colors.primary}
                  />
                </View>
                <View style={styles.controlItem}>
                  <Text style={styles.controlLabel}>Notifications</Text>
                  <CyberToggle 
                    isOn={notifications} 
                    onToggle={setNotifications}
                    color={colors.success}
                  />
                </View>
              </View>
              <View style={styles.controlRow}>
                <View style={styles.controlItem}>
                  <Text style={styles.controlLabel}>Auto Mode</Text>
                  <CyberToggle 
                    isOn={autoMode} 
                    onToggle={setAutoMode}
                    color={colors.secondary}
                  />
                </View>
                <View style={styles.controlItem}>
                  <Text style={styles.systemTime}>
                    {new Date().toLocaleTimeString()}
                  </Text>
                </View>
              </View>
            </View>
          </HoloCard>
        </View>
        
        {/* Action Center */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>🚀 ACTION CENTER</Text>
          <View style={styles.actionGrid}>
            <GlowButton 
              text="SCAN" 
              icon="🔍" 
              color={colors.primary} 
              width={140} 
              height={60}
            />
            <GlowButton 
              text="DEPLOY" 
              icon="🚀" 
              color={colors.success} 
              width={140} 
              height={60}
            />
            <GlowButton 
              text="ANALYZE" 
              icon="📊" 
              color={colors.warning} 
              width={140} 
              height={60}
            />
            <GlowButton 
              text="SECURE" 
              icon="🛡️" 
              color={colors.secondary} 
              width={140} 
              height={60}
            />
          </View>
        </View>
        
        {/* Neural Network Visualization */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>🧬 NEURAL NETWORK</Text>
          <HoloCard width={screenWidth - 40} height={160} glowColor={colors.neon}>
            <View style={styles.neuralViz}>
              <Text style={styles.neuralTitle}>AI Processing Active</Text>
              <View style={styles.neuralNodes}>
                {[...Array(8)].map((_, i) => (
                  <View key={i} style={[styles.neuralNode, { 
                    backgroundColor: [colors.primary, colors.secondary, colors.accent, colors.success][i % 4],
                    animationDelay: `${i * 200}ms`
                  }]} />
                ))}
              </View>
              <Text style={styles.neuralStatus}>
                Processing 1,247 data nodes • 98.7% accuracy
              </Text>
            </View>
          </HoloCard>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  scrollContent: {
    padding: 20,
  },
  header: {
    alignItems: 'center',
    marginBottom: 30,
  },
  title: {
    fontSize: 32,
    fontWeight: 'bold',
    color: colors.primary,
    letterSpacing: 2,
    textShadowColor: colors.primary,
    textShadowOffset: { width: 0, height: 0 },
    textShadowRadius: 10,
  },
  subtitle: {
    fontSize: 14,
    color: colors.textSecondary,
    marginTop: 5,
    fontFamily: 'monospace',
  },
  headerLine: {
    width: 100,
    height: 2,
    backgroundColor: colors.primary,
    marginTop: 15,
    shadowColor: colors.primary,
    shadowOffset: { width: 0, height: 0 },
    shadowOpacity: 1,
    shadowRadius: 10,
  },
  section: {
    marginBottom: 30,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: colors.text,
    marginBottom: 15,
    fontFamily: 'monospace',
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    gap: 10,
  },
  buttonContainer: {
    position: 'relative',
    margin: 5,
  },
  buttonContent: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    gap: 8,
  },
  buttonIcon: {
    fontSize: 16,
  },
  buttonText: {
    fontSize: 14,
    fontWeight: 'bold',
    fontFamily: 'monospace',
  },
  buttonTouchArea: {
    // For touch handling
  },
  cardContainer: {
    position: 'relative',
    margin: 5,
  },
  cardContent: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    padding: 15,
    justifyContent: 'center',
  },
  statsContent: {
    flex: 1,
    justifyContent: 'space-between',
  },
  statsHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  statsIcon: {
    fontSize: 16,
  },
  statsTitle: {
    fontSize: 12,
    color: colors.textSecondary,
    fontFamily: 'monospace',
  },
  statsValue: {
    fontSize: 24,
    fontWeight: 'bold',
    fontFamily: 'monospace',
  },
  statsProgress: {
    gap: 5,
  },
  progressTrack: {
    height: 4,
    borderRadius: 2,
    overflow: 'hidden',
  },
  progressFill: {
    height: '100%',
    borderRadius: 2,
  },
  percentageText: {
    fontSize: 10,
    color: colors.textSecondary,
    textAlign: 'right',
    fontFamily: 'monospace',
  },
  controlPanel: {
    flex: 1,
    justifyContent: 'space-around',
  },
  controlRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  controlItem: {
    alignItems: 'center',
    gap: 10,
  },
  controlLabel: {
    fontSize: 14,
    color: colors.text,
    fontFamily: 'monospace',
  },
  systemTime: {
    fontSize: 16,
    color: colors.primary,
    fontFamily: 'monospace',
    fontWeight: 'bold',
  },
  toggleContainer: {
    position: 'relative',
  },
  toggleTouchArea: {
    // For touch handling
  },
  actionGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
    gap: 10,
  },
  neuralViz: {
    flex: 1,
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  neuralTitle: {
    fontSize: 16,
    color: colors.neon,
    fontWeight: 'bold',
    fontFamily: 'monospace',
  },
  neuralNodes: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    gap: 8,
    paddingVertical: 10,
  },
  neuralNode: {
    width: 12,
    height: 12,
    borderRadius: 6,
    opacity: 0.8,
  },
  neuralStatus: {
    fontSize: 12,
    color: colors.textSecondary,
    textAlign: 'center',
    fontFamily: 'monospace',
  },
});