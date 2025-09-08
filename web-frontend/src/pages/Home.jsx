import React, { useState, useEffect } from "react";
import ApiClient from "../service/ApiClient";
import ConnectWithStravaIcon from "../icons/ConnectWithStrava.tsx";

import { useOAuth2 } from "@tasoskakour/react-use-oauth2";
import { 
  Button, 
  Heading, 
  Text, 
  Tag, 
  Divider, 
  Image, 
  IconButton,
  Box,
  Grid,
  GridItem,
  VStack,
  HStack,
  Card,
  CardBody,
  Progress,
  Alert,
  AlertIcon,
  useToast
} from "@chakra-ui/react";
import strava from 'super-tiny-icons/images/svg/strava.svg'

const Home = () => {
  const [memoryUsage, setMemoryUsage] = useState(0);
  const [wordCloud, setWordCloud] = useState([]);
  const [activityTypes, setActivityTypes] = useState([]);
  const [selectedActivityType, setSelectedActivityType] = useState("Ride");
  const [userStats, setUserStats] = useState(null);
  const [demoStats, setDemoStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const toast = useToast();

  const { data, loading: authLoading, error: authError, getAuth } = useOAuth2({
    authorizeUrl: 'https://www.strava.com/api/v3/oauth/authorize',
    clientId: '4486',
    redirectUri: `${ApiClient.apiHost}/fe/oauth/strava`,
    scope: 'read,activity:read,profile:read_all',
    responseType: 'code',
    extraQueryParameters: { approval_prompt: 'auto' },
    exchangeCodeForTokenServerURL: `${ApiClient.apiHost}/api/token/strava`,
    exchangeCodeForTokenMethod: "POST",
    onSuccess: (payload) => {
      console.log("Success", payload);
      localStorage.setItem('access_token', payload?.access_token);
      toast({
        title: "Connected to Strava",
        description: "Successfully connected your Strava account!",
        status: "success",
        duration: 5000,
        isClosable: true,
      });
      fetchUserData();
    },
    onError: (error_) => {
      console.error("Error", error_);
      toast({
        title: "Connection Failed",
        description: "Failed to connect to Strava. Please try again.",
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    }
  });

  const isAuthenticated = !!localStorage.getItem('access_token');

  useEffect(() => {
    fetchData();
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      fetchUserData();
    }
  }, [isAuthenticated]);

  const fetchData = async () => {
    try {
      setLoading(true);
      
      // Fetch system status
      const status = await ApiClient.status();
      setMemoryUsage(status.memoryUsedPercentile);

      // Fetch demo data for non-authenticated users
      if (!isAuthenticated) {
        const [demoWc, demoYtdDistance, demoYearlyElevation] = await Promise.all([
          ApiClient.demoWordcloud(),
          ApiClient.demoYtdStatistics('distance', 'Ride'),
          ApiClient.demoYearlyStatistics('elevation', 'Ride')
        ]);
        
        setWordCloud(demoWc);
        setDemoStats({
          ytdDistance: demoYtdDistance,
          yearlyElevation: demoYearlyElevation
        });
      } else {
        // Fetch user data
        const [wc, types] = await Promise.all([
          ApiClient.wordcloud(),
          ApiClient.activityTypes()
        ]);
        
        setWordCloud(wc);
        setActivityTypes(types);
      }
    } catch (error) {
      console.error('Error fetching data:', error);
      toast({
        title: "Error",
        description: "Failed to load data. Please try again.",
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    } finally {
      setLoading(false);
    }
  };

  const fetchUserData = async () => {
    try {
      const [wc, types, stats] = await Promise.all([
        ApiClient.wordcloud(),
        ApiClient.activityTypes(),
        ApiClient.profileStatistics(selectedActivityType, new Date().getFullYear().toString())
      ]);
      
      setWordCloud(wc);
      setActivityTypes(types);
      setUserStats(stats);
    } catch (error) {
      console.error('Error fetching user data:', error);
    }
  };

  const handleConnect = () => getAuth();

  const handleActivityTypeChange = (activityType) => {
    setSelectedActivityType(activityType);
    if (isAuthenticated) {
      fetchUserData();
    }
  };

  if (loading) {
    return (
      <Box textAlign="center" py={10}>
        <Progress size="lg" isIndeterminate />
        <Text mt={4}>Loading...</Text>
      </Box>
    );
  }

  return (
    <Box maxW="1200px" mx="auto" p={6}>
      <VStack spacing={8} align="stretch">
        {/* Header Section */}
        <Box>
          <Heading size="lg" mb={4}>Welcome to Velocorner</Heading>
          <HStack justify="space-between" align="center">
            <Text>System Status: Memory usage {memoryUsage}%</Text>
            <Progress value={memoryUsage} width="200px" />
          </HStack>
        </Box>

        {/* Authentication Section */}
        {!isAuthenticated && (
          <Card>
            <CardBody>
              <VStack spacing={4}>
                <Text fontSize="lg" textAlign="center">
                  Login with your Strava account to see your personal statistics
                </Text>
                <Button 
                  leftIcon={<Image src={strava} boxSize="20px" />}
                  colorScheme="orange" 
                  size="lg"
                  onClick={handleConnect}
                  isLoading={authLoading}
                >
                  Connect with Strava
                </Button>
                <Text fontSize="sm" color="gray.600" textAlign="center">
                  You will be able to see various statistics of your activities such as year to date progress, 
                  yearly achievements, daily heatmap based on distance and elevation and much more!
                </Text>
              </VStack>
            </CardBody>
          </Card>
        )}

        {/* Demo Statistics for Non-Authenticated Users */}
        {!isAuthenticated && demoStats && (
          <Card>
            <CardBody>
              <Heading size="md" mb={4}>Sample Statistics</Heading>
              <Grid templateColumns="repeat(auto-fit, minmax(300px, 1fr))" gap={6}>
                <GridItem>
                  <Text fontWeight="bold" mb={2}>YTD Distance (Sample)</Text>
                  <Text fontSize="2xl" color="blue.500">
                    {demoStats.ytdDistance?.total?.toFixed(1) || 0} km
                  </Text>
                </GridItem>
                <GridItem>
                  <Text fontWeight="bold" mb={2}>Yearly Elevation (Sample)</Text>
                  <Text fontSize="2xl" color="green.500">
                    {demoStats.yearlyElevation?.total?.toFixed(0) || 0} m
                  </Text>
                </GridItem>
              </Grid>
              
              <Divider my={6} />
              
              <Text fontWeight="bold" mb={4}>Word Cloud (Sample)</Text>
              <Box>
                {wordCloud.slice(0, 10).map((word, index) => (
                  <Tag 
                    key={index} 
                    size="lg" 
                    colorScheme="teal" 
                    mr={2} 
                    mb={2}
                    fontSize={Math.max(12, word.count / 2)}
                  >
                    {word.text}
                  </Tag>
                ))}
              </Box>
            </CardBody>
          </Card>
        )}

        {/* User Statistics for Authenticated Users */}
        {isAuthenticated && (
          <VStack spacing={6} align="stretch">
            {/* Activity Type Tabs */}
            <Card>
              <CardBody>
                <Text fontWeight="bold" mb={4}>Activity Types</Text>
                <HStack spacing={2} wrap="wrap">
                  {activityTypes.map((type) => (
                    <Button
                      key={type}
                      size="sm"
                      variant={selectedActivityType === type ? "solid" : "outline"}
                      colorScheme="blue"
                      onClick={() => handleActivityTypeChange(type)}
                    >
                      {type}
                    </Button>
                  ))}
                </HStack>
              </CardBody>
            </Card>

            {/* User Statistics */}
            {userStats && (
              <Card>
                <CardBody>
                  <Heading size="md" mb={4}>Your Statistics</Heading>
                  <Grid templateColumns="repeat(auto-fit, minmax(250px, 1fr))" gap={6}>
                    <GridItem>
                      <Text fontWeight="bold" mb={2}>Total Distance</Text>
                      <Text fontSize="2xl" color="blue.500">
                        {userStats.totalDistance?.toFixed(1) || 0} km
                      </Text>
                    </GridItem>
                    <GridItem>
                      <Text fontWeight="bold" mb={2}>Total Elevation</Text>
                      <Text fontSize="2xl" color="green.500">
                        {userStats.totalElevation?.toFixed(0) || 0} m
                      </Text>
                    </GridItem>
                    <GridItem>
                      <Text fontWeight="bold" mb={2}>Total Time</Text>
                      <Text fontSize="2xl" color="purple.500">
                        {userStats.totalTime?.toFixed(1) || 0} h
                      </Text>
                    </GridItem>
                    <GridItem>
                      <Text fontWeight="bold" mb={2}>Activities</Text>
                      <Text fontSize="2xl" color="orange.500">
                        {userStats.activityCount || 0}
                      </Text>
                    </GridItem>
                  </Grid>
                </CardBody>
              </Card>
            )}

            {/* Word Cloud */}
            <Card>
              <CardBody>
                <Text fontWeight="bold" mb={4}>Your Activity Word Cloud</Text>
                <Box>
                  {wordCloud.slice(0, 15).map((word, index) => (
                    <Tag 
                      key={index} 
                      size="lg" 
                      colorScheme="teal" 
                      mr={2} 
                      mb={2}
                      fontSize={Math.max(12, word.count / 2)}
                    >
                      {word.text}
                    </Tag>
                  ))}
                </Box>
              </CardBody>
            </Card>
          </VStack>
        )}

        {/* Footer */}
        <Box textAlign="center" pt={8}>
          <HStack justify="center" spacing={4}>
            <Text fontSize="sm" color="gray.600">
              Powered by Strava
            </Text>
            <Image src={strava} boxSize="20px" />
          </HStack>
        </Box>
      </VStack>
    </Box>
  );
};

export default Home;
