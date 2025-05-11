import React, { useEffect } from 'react';
import { SafeAreaView, StatusBar } from 'react-native';
import FloatingBubble from './FloatingBubble';
import ActivityLauncher from './ActivityLauncher';
import { Button, Text} from 'react-native';
import useAppExit from './useAppExit';
const App = () => {
  const handleOpenOverlay = () => {
    ActivityLauncher.openFragmentOverlayActivity();
  };
  return (
    <SafeAreaView style={{ flex: 1 }}>
      <StatusBar barStyle="dark-content" />
      <FloatingBubble />
      <Text>yayy</Text>
      <Text>yayy</Text>
      <Text>yayy</Text>
      <Text>yayy</Text>
      <Text>yayy</Text>
      <Text>yayy</Text>
    </SafeAreaView>
  );
};

export default App;