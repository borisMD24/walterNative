const {getDefaultConfig, mergeConfig} = require('@react-native/metro-config');

/**
 * Metro configuration
 * https://reactnative.dev/docs/metro
 *
 * @type {import('@react-native/metro-config').MetroConfig}
 */
const config = getDefaultConfig(__dirname);

config.resolver.alias = {
  ...config.resolver.alias,
  'react-native-svg': 'react-native-svg/lib/module/index.js',
};

config.resolver.assetExts.push('obj', 'mtl', 'fbx', 'gltf', 'glb');

module.exports = config;