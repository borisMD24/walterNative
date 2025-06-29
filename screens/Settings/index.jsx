import React from 'react';
import { View, Text, StyleSheet } from 'react-native';


function SettingsContent() {
  return (
    <View style={styles.settingsContent}>
      <Text style={styles.title}>Settings Screen</Text>
      <Text style={styles.subtitle}>Configuration options here</Text>
      {/* Add your settings UI here */}
    </View>
  );
}

export default function SettingsScreen() {
  return (
      <SettingsContent />
  );
}

const styles = StyleSheet.create({
  settingsContent: {
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