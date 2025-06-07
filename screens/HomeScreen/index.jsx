// screens/HomeScreen.jsx
import React from 'react';
import { Text } from 'react-native';
import Animated, { FadeIn } from 'react-native-reanimated';

export default function HomeScreen() {
  return (
    <Animated.View
      entering={FadeIn.duration(400)}
      style={{ flex: 1, justifyContent: 'center', alignItems: 'center' }}
    >
      <Text style={{ fontSize: 24 }}>🏠 Bienvenue sur Home, poto</Text>
    </Animated.View>
  );
}
