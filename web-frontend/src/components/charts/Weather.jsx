import React, { useState, useEffect } from 'react';
import {
  Card,
  Input,
  Button,
  Text,
  VStack,
  HStack,
  Progress,
  Box
} from '@chakra-ui/react';

const Weather = ({ defaultLocation = '' }) => {
  const [location, setLocation] = useState(defaultLocation);
  const [isLoading, setIsLoading] = useState(false);
  const [currentWeather, setCurrentWeather] = useState(null);

  // Initialize weather on component mount
  useEffect(() => {
    if (!location && !defaultLocation) {
      detectLocation();
    } else if (location || defaultLocation) {
      loadWeather(location || defaultLocation);
    }
  }, []);

  const detectLocation = async () => {
    try {
      const response = await fetch('https://weather.velocorner.com/location/ip');
      const data = await response.json();
      console.info(`location data is ${JSON.stringify(data)}`);
      const detectedLocation = `${data.city}, ${data.country}`;
      setLocation(detectedLocation);
      loadWeather(detectedLocation);
    } catch (error) {
      console.error('Failed to detect location:', error);
    }
  };

  const loadWeather = async (place) => {
    console.log(`load weather for ${place}`);
    if (!place) return;

    setIsLoading(true);
    try {
      const response = await fetch(`https://weather.velocorner.com/weather/current/${encodeURIComponent(place)}`);
      const data = await response.json();
      console.info(`current weather is ${JSON.stringify(data)}`);
      //setCurrentWeather(data);
    } catch (error) {
      console.error('Failed to load weather data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleLocationSubmit = () => {
    if (location.trim()) {
      loadWeather(location.trim());
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleLocationSubmit();
    }
  };

  return (
    <Card.Root>
      <Card.Body>
        <VStack spacing={4} align="stretch">
          {/* Location Input */}
          <HStack>
            <Input
              placeholder="Enter location"
              value={location}
              onChange={(e) => setLocation(e.target.value)}
              onKeyPress={handleKeyPress}
            />
            <Button
              onClick={handleLocationSubmit}
              isLoading={isLoading}
              colorScheme="blue"
            >
              Weather
            </Button>
          </HStack>

          {/* Loading Progress */}
          {isLoading && (
            <Progress size="sm" isIndeterminate colorScheme="blue" />
          )}

          {/* Current Weather Display */}
          {currentWeather && (
            <Box textAlign="center">
              <Text fontSize="3xl" fontWeight="bold">
                {currentWeather.info.temp.toFixed(1)}°C
              </Text>
              <Text fontSize="lg">
                {currentWeather.current.description}
              </Text>
            </Box>
          )}
        </VStack>
      </Card.Body>
    </Card.Root>
  );
};

export default Weather;
