
// Step 2: Update your individual screen components
// screens/HomeScreen.js

import React from 'react';
import CaptureableScreen from '../components/CaptureableScreen';
import NeumorphicUIKit from '../components/neumorphism';

export default function Home() {
  return (
    <CaptureableScreen 
      screenName="Home"
      showCaptureButton={true}
      captureOnMount={false} // Set to true if you want auto-capture when screen loads
    >
      <NeumorphicUIKit />
    </CaptureableScreen>
  );
}
