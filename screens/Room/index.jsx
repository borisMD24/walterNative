
// screens/RoomsScreen.js

import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

function RoomsContent() {
  return (
    <View style={styles.roomsContent}>
      <Text style={styles.title}>Rooms Screen</Text>
      <Text style={styles.subtitle}>This content will be captured</Text>
      {/* Add your rooms UI here */}
    </View>
  );
}

export default function RoomsScreen() {
  return (
      <RoomsContent />
  );
}

const styles = StyleSheet.create({
  roomsContent: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  title: {
    fontSize: 32,
    fontWeight: '700',
    marginBottom: 10,
  },
  subtitle: {
    fontSize: 16,
    color: '#666',
  },
});
