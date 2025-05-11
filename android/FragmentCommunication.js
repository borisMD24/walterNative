import { NativeModules, NativeEventEmitter } from 'react-native';

const { FragmentCommunication } = NativeModules;
const fragmentEventEmitter = new NativeEventEmitter(FragmentCommunication);

/**
 * Helper module to communicate with the Android Fragment
 */
export default {
  /**
   * Send data from React Native to Android Fragment
   * @param {string} data - Data to send to the Fragment
   * @returns {Promise} - Promise that resolves with response from native code
   */
  sendDataToFragment: (data) => {
    return FragmentCommunication.sendDataToFragment(data);
  },
  
  /**
   * Listen for events from Android Fragment
   * @param {string} eventName - Name of the event to listen for
   * @param {function} callback - Function to call when event is received
   * @returns {object} - Subscription object that can be used to unsubscribe
   */
  addFragmentListener: (eventName, callback) => {
    return fragmentEventEmitter.addListener(eventName, callback);
  },
  
  /**
   * Remove event listener
   * @param {object} subscription - Subscription object returned from addFragmentListener
   */
  removeFragmentListener: (subscription) => {
    if (subscription) {
      subscription.remove();
    }
  }
};