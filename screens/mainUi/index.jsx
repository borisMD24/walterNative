// Step 3: Update your MainUI.js to use the new screen components
// screens/MainUI.js

import React, {useCallback} from 'react';
import { NavigationContainer } from '@react-navigation/native';
import Nav from '../../components/nav';
import { SafeAreaView } from 'react-native-safe-area-context';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { View, TouchableOpacity, Text, StyleSheet } from 'react-native';

// Import your new screen components
import HomeScreen from '../HomeScreen';
import RoomsScreen from '../Room';
import SettingsScreen from '../Settings';

const Tab = createBottomTabNavigator();

function TabBarButtons({ state, navigation }) {
  const handleNavigation = useCallback((routeName) => {
    // Add safety check for navigation
    if (navigation && navigation.navigate) {
      navigation.navigate(routeName);
    }
  }, [navigation]);

  return (
    <View style={styles.buttonsContainer}>
      {state.routes.map((route, idx) => (
        <TouchableOpacity
          key={route.key}
          onPress={() => handleNavigation(route.name)}
          style={[
            styles.tabButton,
            idx === state.index && styles.activeTabButton
          ]}
          activeOpacity={0.7}
        >
          <Text style={[
            styles.tabButtonText,
            { color: idx === state.index ? '#fff' : 'rgba(255,255,255,0.7)' }
          ]}>
            {route.name}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );
}

export default function MainUI() {
  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaView style={styles.container}>
        <NavigationContainer>
          <Tab.Navigator
            screenOptions={{
              headerShown: false,
              tabBarStyle: { display: 'none' },
              tabBarShowLabel: false,
              // Add animation config to reduce UI frame issues
              animationEnabled: true,
              animationTypeForReplace: 'push',
            }}
            tabBar={props => (
              <View style={styles.tabBarContainer}>
                <Nav />
                <TabBarButtons {...props} />
              </View>
            )}
          >
            <Tab.Screen 
              name="Home" 
              component={HomeScreen}
              options={{
                unmountOnBlur: false, // Keep screens mounted to avoid ref issues
              }}
            />
            <Tab.Screen 
              name="Rooms" 
              component={RoomsScreen}
              options={{
                unmountOnBlur: false,
              }}
            />
            <Tab.Screen 
              name="Settings" 
              component={SettingsScreen}
              options={{
                unmountOnBlur: false,
              }}
            />
          </Tab.Navigator>
        </NavigationContainer>
      </SafeAreaView>
    </GestureHandlerRootView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#E0E0E0"
  },
  tabBarContainer: {
    position: 'relative',
  },
  buttonsContainer: {
    position: 'absolute',
    bottom: 20,
    left: 20,
    right: 20,
    height: 70,
    flexDirection: 'row',
    justifyContent: 'space-around',
    alignItems: 'center',
    backgroundColor: 'rgba(0,0,0,0.1)',
    borderRadius: 35,
    zIndex: 1000,
  },
  tabButton: {
    padding: 10,
    borderRadius: 8,
    minWidth: 60,
    alignItems: 'center',
  },
  activeTabButton: {
    backgroundColor: 'rgba(255,255,255,0.1)',
  },
  tabButtonText: {
    fontSize: 14,
    fontWeight: '500',
  },
});