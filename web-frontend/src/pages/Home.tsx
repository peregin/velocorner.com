import { useState, useEffect, useRef } from "react";
import ApiClient from "../service/ApiClient";
import { useAuth } from "../service/auth";
import { getInitials, formatTimestamp } from "../service/formatters";
import {
  Avatar,
  Button,
  Heading,
  Text,
  Image,
  Box,
  Grid,
  GridItem,
  VStack,
  HStack,
  Card,
  Progress,
  Spinner,
} from "@chakra-ui/react";
import { toaster } from "@/components/ui/toaster";
import strava from 'super-tiny-icons/images/svg/strava.svg'
import WordCloud from "../components/charts/WordCloud";
import Weather from "@/components/charts/Weather";
import LineSeriesChart from "@/components/charts/LineSeriesChart";
import BarChart from "@/components/charts/BarChart";
import HeatmapChart from "@/components/charts/HeatmapChart";
import CalendarHeatmap from "@/components/charts/CalendarHeatmap";
import Stats from "@/components/Stats";
import type { AthleteProfile, DemoStats, UserStats } from "../types/athlete";

const showError = (title: string, description: string) => {
  toaster.create({ title, description, type: "error", duration: 5000 });
};

const Home = () => {
  const [wordCloud, setWordCloud] = useState<any[]>([]);
  const [activityTypes, setActivityTypes] = useState<string[]>([]);
  const [selectedActivityType, setSelectedActivityType] = useState("Ride");
  const [userStats, setUserStats] = useState<UserStats | null>(null);
  const [demoStats, setDemoStats] = useState<DemoStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [athleteProfile, setAthleteProfile] = useState<AthleteProfile | null>(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const [logoutLoading, setLogoutLoading] = useState(false);
  const initialLoadRef = useRef(false);

  const { isAuthenticated, authLoading, connect, logout } = useAuth();

  useEffect(() => {
    if (initialLoadRef.current) return;
    initialLoadRef.current = true;
    const fetchData = async () => {
      try {
        setLoading(true);

        if (!isAuthenticated) {
          const [demoWc, demoYtdDistance, demoYearlyElevation] = await Promise.all([
            ApiClient.demoWordcloud(),
            ApiClient.demoYtdStatistics('distance', 'Ride'),
            ApiClient.demoYearlyStatistics('elevation', 'Ride')
          ]);
          setWordCloud(demoWc);
          setDemoStats({ ytdDistance: demoYtdDistance, yearlyElevation: demoYearlyElevation });
        } else {
          const [wc, types, stats] = await Promise.all([
            ApiClient.wordcloud(),
            ApiClient.activityTypes(),
            ApiClient.profileStatistics(selectedActivityType, new Date().getFullYear().toString())
          ]);
          setWordCloud(wc);
          setActivityTypes(types);
          setUserStats(stats);
        }
      } catch (error) {
        console.error('Error fetching data:', error);
        showError("Error", "Failed to load data. Please try again.");
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  useEffect(() => {
    if (!initialLoadRef.current) return;
    const fetchUserData = async () => {
      if (!isAuthenticated) return;
      try {
        const stats = await ApiClient.profileStatistics(selectedActivityType, new Date().getFullYear().toString());
        setUserStats(stats);
      } catch (error) {
        console.error('Error fetching user stats:', error);
      }
    };
    fetchUserData();
  }, [selectedActivityType, isAuthenticated]);

  const handleActivityTypeChange = (activityType: string) => {
    setSelectedActivityType(activityType);
  };

  const lastUpdatedLabel = formatTimestamp(athleteProfile?.lastUpdate);

  useEffect(() => {
    let isActive = true;

    const cleanup = () => {
      isActive = false;
    };

    if (!isAuthenticated) {
      setAthleteProfile(null);
      setProfileLoading(false);
      return cleanup;
    }

    const fetchAthleteProfile = async () => {
      try {
        setProfileLoading(true);
        const profile = await ApiClient.athleteProfile();
        if (isActive) {
          setAthleteProfile(profile);
        }
      } catch (error) {
        console.error('Error fetching athlete profile:', error);
        if (isActive) {
          showError("Unable to load profile", "We couldn't load your profile information right now.");
        }
      } finally {
        if (isActive) {
          setProfileLoading(false);
        }
      }
    };

    fetchAthleteProfile();

    return cleanup;
  }, [isAuthenticated]);

  const handleLogout = async () => {
    setLogoutLoading(true);
    await logout();
    setLogoutLoading(false);
  };

  // fetchers for charts
  const fetchYearlyHeatmap = () => ApiClient.yearlyHeatmap(selectedActivityType);
  const fetchYearlyDistance = () => ApiClient.yearlyStatistics('distance', selectedActivityType);
  const fetchYearlyElevation = () => ApiClient.yearlyStatistics('elevation', selectedActivityType);
  const fetchYearlyTime = () => ApiClient.yearlyStatistics('time', selectedActivityType);
  const fetchYtdDistance = () => ApiClient.ytdStatistics('distance', selectedActivityType);
  const fetchYtdElevation = () => ApiClient.ytdStatistics('elevation', selectedActivityType);
  const fetchYtdTime = () => ApiClient.ytdStatistics('time', selectedActivityType);
  const fetchHistogramDistance = () => ApiClient.yearlyHistogram('distance', selectedActivityType);
  const fetchHistogramElevation = () => ApiClient.yearlyHistogram('elevation', selectedActivityType);
  const fetchDailyDistance = () => ApiClient.dailyStatistics('distance');

  return (
    <Box maxW="1200px" mx="auto" p={6}>

      <VStack gap={8} align="stretch">

        <Card.Root>
          <Card.Body>
            <Weather />
          </Card.Body>
        </Card.Root>

        {/* Mandatory Strava logo */}
        <Box display="flex" justifyContent="flex-end">
          <Image width='169px' height='31px' src='/images/powered-by-strava1.png' />
        </Box>

        {loading && (
          <Box textAlign="center" py={10}>
            <Progress.Root size="lg" value={null}>
              <Progress.Track>
                <Progress.Range />
              </Progress.Track>
            </Progress.Root>
            <Text mt={4}>Loading...</Text>
          </Box>
        )}

        {/* Authentication Section */}
        {!isAuthenticated && (
          <>
            <Card.Root>
              <Card.Body>
                <VStack gap={4}>
                  <Text fontSize="lg" textAlign="center">
                    Login with your Strava account to see your personal statistics.
                    <Text fontSize="sm" color="gray.600" textAlign="center">
                      See your cycling data come to life with interactive charts and insights that help you understand your performance.
                    </Text>
                  </Text>
                  <Button
                    colorPalette="orange"
                    size="md"
                    fontWeight="medium"
                    _hover={{ boxShadow: 'lg', transform: 'scale(1.05)' }}
                    transition="all 0.2s"
                    onClick={connect}
                    loading={authLoading}
                  >
                    <Image src={strava} boxSize="20px" mr={2} />
                    Connect with Strava
                  </Button>
                  <Text fontSize="sm" color="gray.600" textAlign="center">
                    You will be able to see various statistics of your activities such as year to date progress,
                    yearly achievements, daily heatmap based on distance and elevation and much more!
                  </Text>
                </VStack>
              </Card.Body>
            </Card.Root>


            <HStack align="stretch" gap={4} flexWrap="wrap">
              <Box flex="0 0 30%">
                <WordCloud words={wordCloud} />
              </Box>
              <Box flex="0 0 30%">
                <BarChart title="Year To Date Distance (Sample)" unit={userStats?.units?.distanceLabel || 'km'}
                  fetchSeries={() => ApiClient.demoYearlyStatistics('distance', 'Ride')} height={350} /></Box>
              <Box flex={1}>
                <LineSeriesChart title="Yearly Elevation (Sample)" unit={userStats?.units?.elevationLabel || 'm'}
                  fetchSeries={() => ApiClient.demoYearlyStatistics('elevation', 'Ride')} seriesToShow={5} height={350} /></Box>
            </HStack>

            <Stats />

            <Card.Root>
              <Card.Body>
                <CalendarHeatmap title="Latest Activities (Sample)"
                  fetchDaily={() => ApiClient.demoDailyStatistics('distance')} unitName={userStats?.units?.distanceLabel || 'km'} maxMonths={8} />
              </Card.Body>
            </Card.Root>

            <HStack align="stretch" gap={4} flexWrap="wrap">
              <Box flex={1}><HeatmapChart title="Distance Distribution (Sample)"
                fetchHeatmap={() => ApiClient.demoYearlyHistogram('distance', 'Ride')} height={250} /></Box>
              <Box flex={1}><HeatmapChart title="Elevation Distribution (Sample)"
                fetchHeatmap={() => ApiClient.demoYearlyHistogram('elevation', 'Ride')} height={250} /></Box>
            </HStack>
          </>
        )}

        {/* Profile Section for Authenticated Users */}
        {isAuthenticated && (
          <>
            <Card.Root>
              <Card.Body>
                <WordCloud words={wordCloud} />
              </Card.Body>
            </Card.Root>

            <Card.Root>
              <Card.Body>
                <VStack gap={4} align="stretch">
                  <Heading size="md">Your Profile</Heading>
                  {profileLoading ? (
                    <HStack gap={3}>
                      <Spinner />
                      <Text>Loading your profile...</Text>
                    </HStack>
                  ) : athleteProfile ? (
                    <VStack gap={4} align="stretch">
                      <HStack gap={4} align="center">
                        <Avatar.Root size="xl">
                          {athleteProfile.avatarUrl && (
                            <Avatar.Image
                              src={athleteProfile.avatarUrl}
                              alt={athleteProfile.displayName}
                            />
                          )}
                          <Avatar.Fallback>
                            {getInitials(athleteProfile.displayName) || athleteProfile.displayName.charAt(0).toUpperCase()}
                          </Avatar.Fallback>
                        </Avatar.Root>
                        <Box>
                          <Heading size="md">{athleteProfile.displayName}</Heading>
                          {athleteProfile.displayLocation && (
                            <Text color="gray.500">{athleteProfile.displayLocation}</Text>
                          )}
                          {athleteProfile.role && (
                            <Text fontSize="sm" color="purple.500">
                              {typeof athleteProfile.role === 'string'
                                ? athleteProfile.role
                                : athleteProfile.role?.name || athleteProfile.role?.description || 'Member'}
                            </Text>
                          )}
                          {lastUpdatedLabel && (
                            <Text fontSize="xs" color="gray.500">
                              Last updated: {lastUpdatedLabel}
                            </Text>
                          )}
                        </Box>
                      </HStack>

                      {athleteProfile.unit && (
                        <Grid templateColumns="repeat(auto-fit, minmax(150px, 1fr))" gap={4}>
                          <GridItem>
                            <Text fontWeight="bold">Speed</Text>
                            <Text>{athleteProfile.unit.speedLabel || '—'}</Text>
                          </GridItem>
                          <GridItem>
                            <Text fontWeight="bold">Distance</Text>
                            <Text>{athleteProfile.unit.distanceLabel || '—'}</Text>
                          </GridItem>
                          <GridItem>
                            <Text fontWeight="bold">Elevation</Text>
                            <Text>{athleteProfile.unit.elevationLabel || '—'}</Text>
                          </GridItem>
                          <GridItem>
                            <Text fontWeight="bold">Temperature</Text>
                            <Text>{athleteProfile.unit.temperatureLabel || '—'}</Text>
                          </GridItem>
                        </Grid>
                      )}

                      <HStack justify="flex-end">
                        <Button variant="outline" onClick={handleLogout} loading={logoutLoading}>
                          Logout
                        </Button>
                      </HStack>
                    </VStack>
                  ) : (
                    <Text>No profile information available.</Text>
                  )}
                </VStack>
              </Card.Body>
            </Card.Root>

     // {/* User Statistics for Authenticated Users */}
            <VStack gap={6} align="stretch">
              {/* Activity Type Tabs */}
              <Card.Root>
                <Card.Body>
                  <Text fontWeight="bold" mb={4}>Activity Types</Text>
                  <HStack gap={2} wrap="wrap">
                    {activityTypes.map((type) => (
                      <Button
                        key={type}
                        size="sm"
                        variant={selectedActivityType === type ? "solid" : "outline"}
                        colorPalette="blue"
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
              <LineSeriesChart title="Yearly Heatmap (Distance)" unit={userStats?.units?.distanceLabel || 'km'} fetchSeries={fetchYearlyHeatmap} seriesToShow={2} height={400} />

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex="0 0 30%"><BarChart title="Year To Date Distance" unit={userStats?.units?.distanceLabel || 'km'} fetchSeries={fetchYtdDistance} height={350} /></Box>
                <Box flex={1}><LineSeriesChart title="Yearly Distance" unit={userStats?.units?.distanceLabel || 'km'} fetchSeries={fetchYearlyDistance} seriesToShow={4} height={350} /></Box>
              </HStack>

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex="0 0 30%"><BarChart title="Year To Date Elevation" unit={userStats?.units?.elevationLabel || 'm'} fetchSeries={fetchYtdElevation} height={350} /></Box>
                <Box flex={1}><LineSeriesChart title="Yearly Elevation" unit={userStats?.units?.elevationLabel || 'm'} fetchSeries={fetchYearlyElevation} seriesToShow={4} height={350} /></Box>
              </HStack>

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex="0 0 30%"><BarChart title="Year To Date Time" unit={'h'} fetchSeries={fetchYtdTime} height={350} /></Box>
                <Box flex={1}><LineSeriesChart title="Yearly Time" unit={'h'} fetchSeries={fetchYearlyTime} seriesToShow={4} height={350} /></Box>
              </HStack>

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex={1}><HeatmapChart title="Activity Distribution for Distance" fetchHeatmap={fetchHistogramDistance} height={250} /></Box>
                <Box flex={1}><HeatmapChart title="Activity Distribution for Elevation" fetchHeatmap={fetchHistogramElevation} height={250} /></Box>
              </HStack>

              <Card.Root>
                <Card.Body>
                  <CalendarHeatmap title="Latest Activities (Distance)" fetchDaily={fetchDailyDistance} unitName={userStats?.units?.distanceLabel || 'km'} maxMonths={8} />
                </Card.Body>
              </Card.Root>

            </VStack>
          </>
        )}

      </VStack>

      <Box py='1rem'>
        <iframe width="100%" height="400" src="https://embed.windy.com/embed.html?type=map&location=coordinates&metricRain=mm&metricTemp=°C&metricWind=km/h&zoom=5&overlay=wind&product=ecmwf&level=surface&lat=47.31&lon=8.527&detailLat=47.31&detailLon=8.527000000000044&marker=true" frameborder="0"></iframe>
      </Box>
    </Box>
  );
};

export default Home;
