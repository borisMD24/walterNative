import React, { createContext, useContext, useState } from 'react';

// Create the context
const ImageContext = createContext();

// Context provider component
export const ImageProvider = ({ children }) => {
  const [imageBlob, setImageBlob] = useState(null);
  const [viewShotRef, setViewShotRef] = useState(null);

  // Setter function to update the image blob
  const setImage = (blob) => {
    setImageBlob(blob);
  };

  // Clear the image
  const clearImage = () => {
    setImageBlob(null);
  };

  // Take screenshot function
  const takeScreenshot = async () => {
    if (viewShotRef?.current) {
      try {
        const uri = await viewShotRef.current.capture();
        setImage(uri);
        return uri;
      } catch (error) {
        console.error('Screenshot failed:', error);
        throw error;
      }
    } else {
      console.warn('ViewShot ref not available');
    }
  };

  const value = {
    imageBlob,
    setImage,
    clearImage,
    takeScreenshot,
    setViewShotRef,
    hasImage: imageBlob !== null
  };

  return (
    <ImageContext.Provider value={value}>
      {children}
    </ImageContext.Provider>
  );
};

// Custom hook to use the image context
export const useImage = () => {
  const context = useContext(ImageContext);
  
  if (!context) {
    throw new Error('useImage must be used within an ImageProvider');
  }
  
  return context;
};

// Export the context for direct access if needed
export default ImageContext;