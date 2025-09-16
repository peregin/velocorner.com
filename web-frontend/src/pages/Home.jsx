import { useState, useEffect } from "react";
import ApiClient from "../service/ApiClient";

import { useOAuth2 } from "@tasoskakour/react-use-oauth2";
import {
  Button,
  Heading,
  Text,
  Tag,
  Separator,
  Image,
  Box,
  Grid,
  GridItem,
  VStack,
  HStack,
  Card,
  Progress,
} from "@chakra-ui/react";
import { toaster } from "@/components/ui/toaster";
import strava from 'super-tiny-icons/images/svg/strava.svg'
import WordCloud from "../components/charts/WordCloud";
import Weather from "@/components/charts/Weather";
import LineSeriesChart from "@/components/charts/LineSeriesChart";
import BarChart from "@/components/charts/BarChart";
import HeatmapChart from "@/components/charts/HeatmapChart";
import CalendarHeatmap from "@/components/charts/CalendarHeatmap";

const Home = () => {
  const [memoryUsage, setMemoryUsage] = useState(0);
  const [wordCloud, setWordCloud] = useState([]);
  const [activityTypes, setActivityTypes] = useState([]);
  const [selectedActivityType, setSelectedActivityType] = useState("Ride");
  const [userStats, setUserStats] = useState(null);
  const [demoStats, setDemoStats] = useState(null);
  const [loading, setLoading] = useState(true);

  const { data, loading: authLoading, error: authError, getAuth } = useOAuth2({
    authorizeUrl: 'https://www.strava.com/api/v3/oauth/authorize',
    clientId: '4486',
    // must be the web host
    // redirectUri: `${ApiClient.apiHost}/oauth/strava`,
    redirectUri: `http://localhost:3000/oauth/strava`,
    scope: 'read,activity:read,profile:read_all',
    responseType: 'code',
    extraQueryParameters: { approval_prompt: 'auto' },
    exchangeCodeForTokenQuery: {
      url: `${ApiClient.apiHost}/api/token/strava`,
      method: "POST",
    },
    onSuccess: (payload) => {
      console.log("Success", payload);
      localStorage.setItem('access_token', payload?.access_token);
      toaster.create({
        title: "Connected to Strava",
        description: "Successfully connected your Strava account!",
        status: "success",
        duration: 5000,
      });
      fetchUserData();
    },
    onError: (error_) => {
      console.error("Error", error_);
      toaster.create({
        title: "Connection Failed",
        description: "Failed to connect to Strava. Please try again.",
        status: "error",
        duration: 5000,
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
      toaster.create({
        title: "Error",
        description: "Failed to load data. Please try again.",
        status: "error",
        duration: 5000,
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

  // fetchers for charts
  const fetchYearlyDistance = () => ApiClient.yearlyStatistics('distance', selectedActivityType);
  const fetchYearlyElevation = () => ApiClient.yearlyStatistics('elevation', selectedActivityType);
  const fetchYearlyTime = () => ApiClient.yearlyStatistics('time', selectedActivityType);
  const fetchYtdDistance = () => ApiClient.ytdStatistics('distance', selectedActivityType);
  const fetchYtdElevation = () => ApiClient.ytdStatistics('elevation', selectedActivityType);
  const fetchYtdTime = () => ApiClient.ytdStatistics('time', selectedActivityType);
  const fetchHistogramDistance = () => ApiClient.yearlyHistogram('distance', selectedActivityType);
  const fetchHistogramElevation = () => ApiClient.yearlyHistogram('elevation', selectedActivityType);
  const fetchDailyDistance = () => ApiClient.dailyStatistics('distance');

  if (loading) {
    return (
      <Box textAlign="center" py={10}>
        <Progress.Root size="lg" value={null}>
          <Progress.Track>
            <Progress.Range />
          </Progress.Track>
        </Progress.Root>
        <Text mt={4}>Loading...</Text>
      </Box>
    );
  }

  return (
    <Box maxW="1200px" mx="auto" p={6}>
      <VStack spacing={8} align="stretch">

        <Card.Root>
          <Card.Body>
            <Weather />
          </Card.Body>
        </Card.Root>

        {/* Header Section */}
        <Box>
          <Heading size="lg" mb={4}>Welcome to Velocorner</Heading>
          <VStack justify="space-between" align="left">
            <Progress.Root value={memoryUsage} defaultValue={90}>
              <Progress.Label>Memory usage</Progress.Label>
              <Progress.Track flex='1'>
                <Progress.Range />
              </Progress.Track>
              <Progress.ValueText>{memoryUsage}%</Progress.ValueText>
            </Progress.Root>
            {/* <Progress value={memoryUsage} width="200px" /> */}
          </VStack>
        </Box>

        {/* Authentication Section */}
        {/* {!isAuthenticated && ( */}
        {/* ( */}
        <Card.Root>
          <Card.Body>
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
          </Card.Body>
        </Card.Root>
        {/* ) */}

        {/* Demo Statistics for Non-Authenticated Users */}
        {!isAuthenticated && demoStats && (
          <Card.Root>
            <Card.Body>
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

              <Separator my={6} />

              <Text fontWeight="bold" mb={4}>Word Cloud (Sample)</Text>
              <Box>
                {wordCloud.slice(0, 10).map((word, index) => (
                  <Tag.Root
                    key={index}
                    size="lg"
                    colorScheme="teal"
                    mr={2}
                    mb={2}
                    fontSize={Math.max(12, word.count / 2)}
                  >
                    <Tag.Label>{word.text}</Tag.Label>
                  </Tag.Root>
                ))}
              </Box>
            </Card.Body>
          </Card.Root>
        )}

        {/* User Statistics for Authenticated Users */}
        {isAuthenticated && (
          <VStack spacing={6} align="stretch">
            {/* Activity Type Tabs */}
            <Card.Root>
              <Card.Body>
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
              </Card.Body>
            </Card.Root>

            {/* User Statistics */}
            {userStats && (
              <Card.Root>
                <Card.Body>
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
                </Card.Body>
              </Card.Root>
            )}

            {/* Charts parity to Play widgets */}
            <LineSeriesChart title="Yearly Heatmap (Distance)" unit={userStats?.units?.distanceLabel || 'km'} fetchSeries={fetchYearlyDistance} seriesToShow={2} height={400} />

            <HStack align="stretch" spacing={4} flexWrap="wrap">
              <BarChart title="Year To Date Distance" unit={userStats?.units?.distanceLabel || 'km'} fetchSeries={fetchYtdDistance} height={350} />
              <LineSeriesChart title="Yearly Distance" unit={userStats?.units?.distanceLabel || 'km'} fetchSeries={fetchYearlyDistance} seriesToShow={4} height={350} />
            </HStack>

            <HStack align="stretch" spacing={4} flexWrap="wrap">
              <BarChart title="Year To Date Elevation" unit={userStats?.units?.elevationLabel || 'm'} fetchSeries={fetchYtdElevation} height={350} />
              <LineSeriesChart title="Yearly Elevation" unit={userStats?.units?.elevationLabel || 'm'} fetchSeries={fetchYearlyElevation} seriesToShow={4} height={350} />
            </HStack>

            <HStack align="stretch" spacing={4} flexWrap="wrap">
              <BarChart title="Year To Date Time" unit={'h'} fetchSeries={fetchYtdTime} height={350} />
              <LineSeriesChart title="Yearly Time" unit={'h'} fetchSeries={fetchYearlyTime} seriesToShow={4} height={350} />
            </HStack>

            <HStack align="stretch" spacing={4} flexWrap="wrap">
              <HeatmapChart title="Activity Distribution for Distance" fetchHeatmap={fetchHistogramDistance} height={250} />
              <HeatmapChart title="Activity Distribution for Elevation" fetchHeatmap={fetchHistogramElevation} height={250} />
            </HStack>

            <CalendarHeatmap title="Latest Activities (Distance)" fetchDaily={fetchDailyDistance} unitName={userStats?.units?.distanceLabel || 'km'} maxMonths={8} />
            

            {/* Word Cloud */}
            <Card.Root>
              <Card.Body>
                <WordCloud words={wordCloud} />
              </Card.Body>
            </Card.Root>
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
