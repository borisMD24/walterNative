import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';

/**
 * React Native component that will be overlaid on top of an Android Fragment
 */
const OverlayComponent = (props) => {
  return (
    <View style={styles.container}>
      <View style={styles.overlay}>
        <Text style={styles.overlayText}>
          React Native Overlay
        </Text>
        
        <TouchableOpacity 
          style={styles.button}
          onPress={() => {
            // You can communicate with native code here
            // For example using Native Modules
            console.log('Button pressed in React Native overlay');
          }}
        >
          <Text style={styles.buttonText}>Overlay Button</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    // Use transparent background to see through to the native view
    backgroundColor: 'transparent',
    justifyContent: 'center',
    alignItems: 'center',
  },
  overlay: {
    width: '80%',
    padding: 20,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
    borderRadius: 10,
    alignItems: 'center',
  },
  overlayText: {
    color: 'white',
    fontSize: 18,
    marginBottom: 20,
  },
  button: {
    backgroundColor: '#4CAF50',
    paddingVertical: 10,
    paddingHorizontal: 20,
    borderRadius: 5,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
  }
});

export default OverlayComponent;