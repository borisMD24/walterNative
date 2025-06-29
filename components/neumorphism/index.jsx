import React, { useState } from 'react';
import { ScrollView, View, StyleSheet, Text } from 'react-native';
import { 
  Button, 
  RadialSlider, 
  LinearSlider, 
  TextInput, 
  NeumorphicCard, 
  NeumorphicToggle, 
  NeumorphicSwitch 
} from '../neumorphismKit';

export default function NeumorphicUIKit() {
  const [switchState, setSwitchState] = useState(false);
  const [toggleState, setToggleState] = useState(false);
  const [buttonPressed, setButtonPressed] = useState(false);
  const [radialValue, setRadialValue] = useState(35);
  const [linearValue, setLinearValue] = useState(65);

  return (
    <ScrollView contentContainerStyle={styles.container}>
      {/* Section Title */}
      <Text style={styles.sectionTitle}>Neumorphic UI Components</Text>
      
      {/* Buttons Section */}
      <View style={styles.section}>
        <Text style={styles.componentLabel}>Buttons</Text>
        <View style={styles.buttonGroup}>
          <View style={styles.buttonWrapper}>
            <Button 
              title="Default" 
              theme="light" 
              pressed={false} 
            />
          </View>
          <View style={styles.buttonWrapper}>
            <Button 
              title="Pressed" 
              theme="light" 
              pressed={true} 
            />
          </View>
        </View>
      </View>

      {/* Switches Section */}
      <View style={styles.section}>
        <Text style={styles.componentLabel}>Switches</Text>
        <View style={styles.switchGroup}>
          <View style={styles.componentWrapper}>
            <Text style={styles.subLabel}>Switch Off</Text>
            <NeumorphicSwitch 
              on={false} 
              theme="light" 
            />
          </View>
          <View style={styles.componentWrapper}>
            <Text style={styles.subLabel}>Switch On</Text>
            <NeumorphicSwitch 
              on={true} 
              theme="light" 
            />
          </View>
        </View>
      </View>

      {/* Toggles Section */}
      <View style={styles.section}>
        <Text style={styles.componentLabel}>Toggles</Text>
        <View style={styles.toggleGroup}>
          <View style={styles.componentWrapper}>
            <NeumorphicToggle 
              on={false} 
              label="Inactive" 
              theme="light" 
            />
          </View>
          <View style={styles.componentWrapper}>
            <NeumorphicToggle 
              on={true} 
              label="Active" 
              theme="light" 
            />
          </View>
        </View>
      </View>

      {/* Sliders Section */}
      <View style={styles.section}>
        <Text style={styles.componentLabel}>Sliders</Text>
        
        <View style={styles.componentWrapper}>
          <Text style={styles.subLabel}>Radial Slider (35%)</Text>
          <RadialSlider 
            value={35} 
            size={180} 
            theme="light" 
          />
        </View>

        <View style={styles.componentWrapper}>
          <Text style={styles.subLabel}>Linear Slider (65%)</Text>
          <LinearSlider 
            value={65} 
            width={280} 
            height={50} 
            theme="light" 
          />
        </View>
      </View>

      {/* Text Input Section */}
      <View style={styles.section}>
        <Text style={styles.componentLabel}>Text Input</Text>
        <View style={styles.componentWrapper}>
          <TextInput 
            placeholder="Enter your text here..." 
            width={280} 
            height={50} 
            theme="light" 
          />
        </View>
      </View>

      {/* Cards Section */}
      <View style={styles.section}>
        <Text style={styles.componentLabel}>Cards</Text>
        <View style={styles.cardGroup}>
          <View style={styles.componentWrapper}>
            <NeumorphicCard 
              title="Sample Card" 
              content="This is a neumorphic card component with shadows and rounded corners." 
              width={280} 
              height={140} 
              theme="light" 
            />
          </View>
          <View style={styles.componentWrapper}>
            <NeumorphicCard 
              title="Another Card" 
              content="Cards can contain various types of content and maintain the neumorphic aesthetic." 
              width={280} 
              height={140} 
              theme="light" 
            />
          </View>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingVertical: 30,
    paddingHorizontal: 20,
    alignItems: 'center',
    backgroundColor: '#E0E0E0',
    minHeight: '100%',
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#666',
    marginBottom: 30,
    textAlign: 'center',
  },
  section: {
    width: '100%',
    alignItems: 'center',
    marginBottom: 35,
  },
  componentLabel: {
    fontSize: 18,
    fontWeight: '600',
    color: '#555',
    marginBottom: 20,
    textAlign: 'center',
  },
  subLabel: {
    fontSize: 14,
    color: '#777',
    marginBottom: 10,
    textAlign: 'center',
  },
  componentWrapper: {
    alignItems: 'center',
    marginVertical: 15,
  },
  buttonGroup: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
    flexWrap: 'wrap',
  },
  buttonWrapper: {
    marginHorizontal: 10,
    marginVertical: 10,
  },
  switchGroup: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
    flexWrap: 'wrap',
  },
  toggleGroup: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    width: '100%',
    flexWrap: 'wrap',
  },
  cardGroup: {
    width: '100%',
    alignItems: 'center',
  },
});