import { AppState } from 'react-native';
import { useEffect, useRef } from 'react';

const useAppExit = (onExit) => {
  const appState = useRef(AppState.currentState);

  useEffect(() => {
    const handleAppStateChange = (nextAppState) => {
      if (appState.current === 'active' && nextAppState.match(/inactive|background/)) {
        onExit(); // L'app passe en arrière-plan
      }
      appState.current = nextAppState;
    };

    const subscription = AppState.addEventListener('change', handleAppStateChange);

    return () => subscription.remove();
  }, [onExit]);
};

export default useAppExit;