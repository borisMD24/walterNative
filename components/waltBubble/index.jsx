import {StyleSheet, View} from 'react-native';
import {Canvas, BackdropBlur, Group} from '@shopify/react-native-skia';
import Animated, {
  useAnimatedStyle,
  useSharedValue,
} from 'react-native-reanimated';
import {Gesture, GestureDetector} from 'react-native-gesture-handler';
import WaltSolo from '../waltSolo';

export default function WaltBubble() {
  const translateX = useSharedValue(50);
  const translateY = useSharedValue(50);

  const panGesture = Gesture.Pan()
    .onBegin(() => {
      // Optional: Add haptic feedback or other start behavior
    })
    .onUpdate(event => {
      translateX.value = event.translationX;
      translateY.value = event.translationY;
    })
    .onEnd(() => {
      // Optional: Add end behavior like snapping
    });

  const animatedStyle = useAnimatedStyle(() => {
    return {
      transform: [
        {translateX: translateX.value},
        {translateY: translateY.value},
      ],
    };
  });

  return (
    <View style={styles.wrapper}>
      {/* Background content that will be blurred */}
      <View style={styles.backgroundContent}>
        {/* Add your background content here */}
      </View>
      
      <GestureDetector gesture={panGesture}>
        <Animated.View style={[styles.container, animatedStyle]}>
          <Canvas style={styles.canvas}>
            <BackdropBlur 
              blur={10} 
              clip={{
                x: 0,
                y: 0,
                width: 200,
                height: 200,
              }}
            />
          </Canvas>
          <View style={styles.content}>
            <WaltSolo />
          </View>
        </Animated.View>
      </GestureDetector>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    flex: 1,
  },
  backgroundContent: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    // Add your background styling here
  },
  container: {
    position: 'absolute',
    top: 50,
    right: 50,
    width: 200,
    height: 200,
    zIndex: 1000,
  },
  canvas: {
    position: 'absolute',
    top: 0,
    left: 0,
    right: 0,
    bottom: 0,
    width: '100%',
    height: '100%',
  },
  content: {
    position: 'absolute',
    width: 200,
    height: 200,
    borderRadius: 25,
    justifyContent: 'center',
    alignItems: 'center',
    pointerEvents: 'none',
  },
});