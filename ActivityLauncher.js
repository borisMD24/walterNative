import { NativeModules } from 'react-native';

const { ActivityLauncher } = NativeModules;

/**
 * Helper module to launch native activities from React Native
 */
export default {
  /**
   * Open the Fragment with React Native overlay activity
   * @returns {void}
   */
  openFragmentOverlayActivity: () => {
    console.log("openFragmentOverlayActivity");
    
    ActivityLauncher.openFragmentOverlayActivity();
  },
};