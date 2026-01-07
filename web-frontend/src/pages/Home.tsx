import { useState, useEffect, useRef, useCallback } from "react";
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
  VStack,
  HStack,
  Card,
  Progress,
  Spinner,
  SegmentGroup
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
import AchievementsWidget from "@/components/AchievementsWidget";
import ActivityStatsWidget from "@/components/ActivityStatsWidget";
import TopActivitiesWidget from "@/components/TopActivitiesWidget";
import type { AthleteProfile } from "../types/athlete";

const showError = (title: string, description: string) => {
  toaster.create({ title, description, type: "error", duration: 5000 });
};

const Home = () => {
  const [wordCloud, setWordCloud] = useState<any[]>([]);
  const [activityTypes, setActivityTypes] = useState<string[]>(['Ride']);
  const [selectedActivityType, setSelectedActivityType] = useState('Ride');
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
          const demoWc = await ApiClient.demoWordcloud();
          setWordCloud(demoWc);
        } else {
          const [wc, types] = await Promise.all([
            ApiClient.wordcloud(),
            ApiClient.activityTypes()
          ]);
          setWordCloud(wc);
          setActivityTypes(types);
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

  const handleActivityTypeChange = (activityType: string) => {
    setSelectedActivityType(activityType);
  };

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
  const fetchYearlyHeatmap = useCallback(
    () => ApiClient.yearlyHeatmap(selectedActivityType),
    [selectedActivityType]
  );
  const fetchYearlyDistance = useCallback(
    () => ApiClient.yearlyStatistics('distance', selectedActivityType),
    [selectedActivityType]
  );
  const fetchYearlyElevation = useCallback(
    () => ApiClient.yearlyStatistics('elevation', selectedActivityType),
    [selectedActivityType]
  );
  const fetchYearlyTime = useCallback(
    () => ApiClient.yearlyStatistics('time', selectedActivityType),
    [selectedActivityType]
  );
  const fetchYtdDistance = useCallback(
    () => ApiClient.ytdStatistics('distance', selectedActivityType),
    [selectedActivityType]
  );
  const fetchYtdElevation = useCallback(
    () => ApiClient.ytdStatistics('elevation', selectedActivityType),
    [selectedActivityType]
  );
  const fetchYtdTime = useCallback(
    () => ApiClient.ytdStatistics('time', selectedActivityType),
    [selectedActivityType]
  );
  const fetchHistogramDistance = useCallback(
    () => ApiClient.yearlyHistogram('distance', selectedActivityType),
    [selectedActivityType]
  );
  const fetchHistogramElevation = useCallback(
    () => ApiClient.yearlyHistogram('elevation', selectedActivityType),
    [selectedActivityType]
  );
  const fetchDailyDistance = useCallback(
    () => ApiClient.dailyStatistics('distance'),
    []
  );

  const fetchDemoYtdDistance = useCallback(
    () => ApiClient.demoYearlyStatistics('distance', 'Ride'),
    []
  );
  const fetchDemoYearlyElevation = useCallback(
    () => ApiClient.demoYearlyStatistics('elevation', 'Ride'),
    []
  );
  const fetchDemoDailyDistance = useCallback(
    () => ApiClient.demoDailyStatistics('distance'),
    []
  );
  const fetchDemoHistogramDistance = useCallback(
    () => ApiClient.demoYearlyHistogram('distance', 'Ride'),
    []
  );
  const fetchDemoHistogramElevation = useCallback(
    () => ApiClient.demoYearlyHistogram('elevation', 'Ride'),
    []
  );


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
                <BarChart title="Year To Date Distance (Sample)" unit={athleteProfile?.unit?.distanceLabel || 'km'}
                  fetchSeries={fetchDemoYtdDistance} height={350} /></Box>
              <Box flex={1}>
                <LineSeriesChart title="Yearly Elevation (Sample)" unit={athleteProfile?.unit?.elevationLabel || 'm'}
                  fetchSeries={fetchDemoYearlyElevation} seriesToShow={5} height={350} /></Box>
            </HStack>

            <Stats />

            <Card.Root>
              <Card.Body>
                <CalendarHeatmap title="Latest Activities (Sample)"
                  fetchDaily={fetchDemoDailyDistance} unitName={athleteProfile?.unit?.distanceLabel || 'km'} maxMonths={8} />
              </Card.Body>
            </Card.Root>

            <HStack align="stretch" gap={4} flexWrap="wrap">
              <Box flex={1}><HeatmapChart title="Distance Distribution (Sample)"
                fetchHeatmap={fetchDemoHistogramDistance} height={250} /></Box>
              <Box flex={1}><HeatmapChart title="Elevation Distribution (Sample)"
                fetchHeatmap={fetchDemoHistogramElevation} height={250} /></Box>
            </HStack>
          </>
        )}

        {/* Profile Section for Authenticated Users */}
        {isAuthenticated && (
          <>
            <Card.Root>
              <Card.Body>
                <Grid templateColumns={{ base: "1fr", lg: "3fr 2fr" }} gap={4}>
                  <Box>
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
                              {athleteProfile?.lastUpdate && (
                                <Text fontSize="xs" color="gray.500">
                                  Last updated: {formatTimestamp(athleteProfile?.lastUpdate)}
                                </Text>
                              )}
                            </Box>
                          </HStack>

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
                  </Box>

                  <Box>
                    <WordCloud words={wordCloud} />
                  </Box>
                </Grid>
              </Card.Body>
            </Card.Root>

            {/* User Statistics for Authenticated Users */}
            <VStack gap={6} align="stretch">
              {/* Activity Type Tabs */}
              <Card.Root>
                <Card.Body>
                  <Text fontWeight="bold" mb={4}>Activity Types</Text>
                  <SegmentGroup.Root
                    value={selectedActivityType}
                    onValueChange={(e) => e.value && handleActivityTypeChange(e.value)}
                    colorPalette="green"
                    bg="colorPalette.50"
                    css={{
                      "--segment-indicator-bg": "colors.green.400",
                      "--segment-indicator-shadow": "shadows.md",
                    }}
                  >
                    <SegmentGroup.Indicator />
                    <SegmentGroup.Items items={activityTypes} />
                  </SegmentGroup.Root>
                </Card.Body>
              </Card.Root>

              {/* User Statistics */}
              <ActivityStatsWidget 
                selectedActivityType={selectedActivityType}
                isAuthenticated={isAuthenticated}
                athleteProfile={athleteProfile}
              />

              {/* Best Achievements */}
              <AchievementsWidget
                selectedActivityType={selectedActivityType}
                athleteProfile={athleteProfile}
                isAuthenticated={isAuthenticated}
              />

              {/* Charts parity to Play widgets */}
              <LineSeriesChart title="Yearly Heatmap (Distance)" unit={athleteProfile?.unit?.distanceLabel || 'km'} fetchSeries={fetchYearlyHeatmap} seriesToShow={2} height={400} />

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex="0 0 30%"><BarChart title="Year To Date Distance" unit={athleteProfile?.unit?.distanceLabel || 'km'} fetchSeries={fetchYtdDistance} height={350} /></Box>
                <Box flex={1}><LineSeriesChart title="Yearly Distance" unit={athleteProfile?.unit?.distanceLabel || 'km'} fetchSeries={fetchYearlyDistance} seriesToShow={4} height={350} /></Box>
              </HStack>

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex="0 0 30%"><BarChart title="Year To Date Elevation" unit={athleteProfile?.unit?.elevationLabel || 'm'} fetchSeries={fetchYtdElevation} height={350} /></Box>
                <Box flex={1}><LineSeriesChart title="Yearly Elevation" unit={athleteProfile?.unit?.elevationLabel || 'm'} fetchSeries={fetchYearlyElevation} seriesToShow={4} height={350} /></Box>
              </HStack>

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex="0 0 30%"><BarChart title="Year To Date Time" unit={'h'} fetchSeries={fetchYtdTime} height={350} /></Box>
                <Box flex={1}><LineSeriesChart title="Yearly Time" unit={'h'} fetchSeries={fetchYearlyTime} seriesToShow={4} height={350} /></Box>
              </HStack>

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex={1}><HeatmapChart title="Activity Distribution for Distance" fetchHeatmap={fetchHistogramDistance} height={250} /></Box>
                <Box flex={1}><HeatmapChart title="Activity Distribution for Elevation" fetchHeatmap={fetchHistogramElevation} height={250} /></Box>
              </HStack>

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex={1}>
                  <TopActivitiesWidget
                    title="Longest Activities"
                    action="distance"
                    selectedActivityType={selectedActivityType}
                    athleteProfile={athleteProfile}
                  />
                </Box>
                <Box flex={1}>
                  <TopActivitiesWidget
                    title="Most Elevation"
                    action="elevation"
                    selectedActivityType={selectedActivityType}
                    athleteProfile={athleteProfile}
                  />
                </Box>
              </HStack>

              <Card.Root>
                <Card.Body>
                  <CalendarHeatmap title="All Activities" fetchDaily={fetchDailyDistance} unitName={athleteProfile?.unit?.distanceLabel || 'km'} maxMonths={8} />
                </Card.Body>
              </Card.Root>

            </VStack>
          </>
        )}

      </VStack>

      <Box py='1rem'>
        <iframe
          width="100%" height="400"
          src="https://embed.windy.com/embed.html?type=map&location=coordinates&metricRain=mm&metricTemp=°C&metricWind=km/h&zoom=5&overlay=wind&product=ecmwf&level=surface&lat=47.31&lon=8.527&detailLat=47.31&detailLon=8.527000000000044&marker=true"
          title="Windy Map" style={{ border: 'none' }}>
        </iframe>
      </Box>
    </Box>
  );
};

export default Home;
