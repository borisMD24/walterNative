/**
 * @format
 */
import { enableScreens } from 'react-native-screens';
enableScreens();
import {AppRegistry} from 'react-native';
import App from './App';

import OverlayComponent from './OverlayComponent';
import {name as appName} from './app.json';
AppRegistry.registerComponent('OverlayComponent', () => OverlayComponent);
AppRegistry.registerComponent(appName, () => App);
