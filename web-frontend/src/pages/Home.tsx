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
  GridItem,
  VStack,
  HStack,
  Card,
  Progress,
  Spinner,
  SegmentGroup,
  Stat,
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
import type { AchievementEntry, AchievementMetric, AthleteAchievements, AthleteProfile, UserStats } from "../types/athlete";

const showError = (title: string, description: string) => {
  toaster.create({ title, description, type: "error", duration: 5000 });
};

const Home = () => {
  const [wordCloud, setWordCloud] = useState<any[]>([]);
  const [activityTypes, setActivityTypes] = useState<string[]>(['Ride']);
  const [selectedActivityType, setSelectedActivityType] = useState('Ride');
  const [userStats, setUserStats] = useState<UserStats | null>(null);
  const [achievements, setAchievements] = useState<AthleteAchievements | null>(null);
  const [achievementsLoading, setAchievementsLoading] = useState(false);
  const [achievementsError, setAchievementsError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [athleteProfile, setAthleteProfile] = useState<AthleteProfile | null>(null);
  const [profileLoading, setProfileLoading] = useState(false);
  const [logoutLoading, setLogoutLoading] = useState(false);
  const initialLoadRef = useRef(false);
  const statsFetchKeyRef = useRef<string | null>(null);

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

  useEffect(() => {
    if (!isAuthenticated) {
      statsFetchKeyRef.current = null;
      setUserStats(null);
      setAchievements(null);
      return;
    }

    const currentYear = new Date().getFullYear().toString();
    const currentKey = `${selectedActivityType}-${currentYear}`;
    if (statsFetchKeyRef.current === currentKey) return;
    statsFetchKeyRef.current = currentKey;

    const fetchUserData = async () => {
      try {
        const stats = await ApiClient.profileStatistics(selectedActivityType, currentYear);
        setUserStats(stats);
      } catch (error) {
        console.error('Error fetching user stats:', error);
      }
    };
    fetchUserData();
  }, [selectedActivityType, isAuthenticated]);

  useEffect(() => {
    if (!isAuthenticated) {
      setAchievements(null);
      setAchievementsLoading(false);
      setAchievementsError(null);
      return;
    }

    let isActive = true;
    setAchievementsLoading(true);
    setAchievementsError(null);

    ApiClient.achievements(selectedActivityType)
      .then((data) => {
        if (!isActive) return;
        setAchievements(data);
      })
      .catch((error) => {
        console.error('Error fetching achievements:', error);
        if (!isActive) return;
        setAchievements(null);
        setAchievementsError("Unable to load achievements.");
      })
      .finally(() => {
        if (isActive) {
          setAchievementsLoading(false);
        }
      });

    return () => {
      isActive = false;
    };
  }, [selectedActivityType, isAuthenticated]);

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

  const formatStatNumber = (value?: number, fractionDigits = 0) => {
    if (typeof value !== "number" || Number.isNaN(value)) {
      return "0";
    }
    return value.toFixed(fractionDigits);
  };

  const formatStatHours = (seconds?: number) => {
    if (typeof seconds !== "number" || Number.isNaN(seconds)) {
      return "0";
    }
    return (seconds / 3600).toFixed(1);
  };

  const formatAchievementDate = (dateString?: string) => {
    if (!dateString) return null;
    const date = new Date(dateString);
    if (Number.isNaN(date.getTime())) return null;
    return date.toLocaleDateString();
  };

  const formatDistanceValue = (meters?: number) => {
    if (typeof meters !== "number" || Number.isNaN(meters)) {
      return { value: "—", unit: athleteProfile?.unit?.distanceLabel || "km" };
    }
    const unit = athleteProfile?.unit?.distanceLabel || "km";
    const isMiles = unit.toLowerCase().includes("mi");
    const formatted = isMiles
      ? meters / 1609.344
      : meters / 1000;
    const decimals = formatted >= 100 ? 0 : 1;
    return { value: formatted.toFixed(decimals), unit };
  };

  const formatElevationValue = (meters?: number) => {
    if (typeof meters !== "number" || Number.isNaN(meters)) {
      return { value: "—", unit: athleteProfile?.unit?.elevationLabel || "m" };
    }
    const unit = athleteProfile?.unit?.elevationLabel || "m";
    const isFeet = unit.toLowerCase().includes("ft");
    const formatted = isFeet ? meters * 3.28084 : meters;
    return { value: formatted.toFixed(formatted >= 1000 ? 0 : 1), unit };
  };

  const formatSpeedValue = (metersPerSecond?: number) => {
    if (typeof metersPerSecond !== "number" || Number.isNaN(metersPerSecond)) {
      return { value: "—", unit: athleteProfile?.unit?.speedLabel || "km/h" };
    }
    const unit = athleteProfile?.unit?.speedLabel || "km/h";
    const isMph = unit.toLowerCase().includes("mph");
    const formatted = isMph
      ? metersPerSecond * 2.23693629
      : metersPerSecond * 3.6;
    return { value: formatted.toFixed(1), unit };
  };

  const formatDurationValue = (seconds?: number) => {
    if (typeof seconds !== "number" || Number.isNaN(seconds)) {
      return "—";
    }
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const remainingSeconds = seconds % 60;
    if (hours > 0) {
      return `${hours}h ${minutes.toString().padStart(2, '0')}m`;
    }
    if (minutes > 0) {
      return `${minutes}m ${remainingSeconds.toString().padStart(2, '0')}s`;
    }
    return `${remainingSeconds}s`;
  };

  const formatTemperatureValue = (celsius?: number) => {
    if (typeof celsius !== "number" || Number.isNaN(celsius)) {
      return { value: "—", unit: athleteProfile?.unit?.temperatureLabel || "°C" };
    }
    const unit = athleteProfile?.unit?.temperatureLabel || "°C";
    const isFahrenheit = unit.toLowerCase().includes("f");
    const formatted = isFahrenheit ? (celsius * 9) / 5 + 32 : celsius;
    return { value: formatted.toFixed(0), unit };
  };

  const formatPowerValue = (watts?: number) => {
    if (typeof watts !== "number" || Number.isNaN(watts)) {
      return { value: "—", unit: "W" };
    }
    return { value: watts.toFixed(0), unit: "W" };
  };

  const formatHeartRateValue = (bpm?: number) => {
    if (typeof bpm !== "number" || Number.isNaN(bpm)) {
      return { value: "—", unit: "bpm" };
    }
    return { value: bpm.toFixed(0), unit: "bpm" };
  };

  const achievementDefinitions: {
    key: AchievementMetric;
    label: string;
    formatter: (entry?: AchievementEntry) => { value: string; unit?: string };
  }[] = [
    {
      key: "maxDistance",
      label: "Longest Distance",
      formatter: (entry) => formatDistanceValue(entry?.value),
    },
    {
      key: "maxElevation",
      label: "Maximum Elevation",
      formatter: (entry) => formatElevationValue(entry?.value),
    },
    {
      key: "maxTimeInSec",
      label: "Longest Moving Time",
      formatter: (entry) => ({ value: formatDurationValue(entry?.value) }),
    },
    {
      key: "maxAverageSpeed",
      label: "Fastest Average Speed",
      formatter: (entry) => formatSpeedValue(entry?.value),
    },
    {
      key: "maxAveragePower",
      label: "Highest Average Power",
      formatter: (entry) => formatPowerValue(entry?.value),
    },
    {
      key: "maxHeartRate",
      label: "Maximum Heart Rate",
      formatter: (entry) => formatHeartRateValue(entry?.value),
    },
    {
      key: "maxAverageHeartRate",
      label: "Highest Average Heart Rate",
      formatter: (entry) => formatHeartRateValue(entry?.value),
    },
    {
      key: "maxAverageTemperature",
      label: "Warmest Average Temperature",
      formatter: (entry) => formatTemperatureValue(entry?.value),
    },
    {
      key: "minAverageTemperature",
      label: "Coldest Average Temperature",
      formatter: (entry) => formatTemperatureValue(entry?.value),
    },
  ];

  const visibleAchievements = achievements
    ? achievementDefinitions.filter((definition) => achievements?.[definition.key])
    : [];

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
              {userStats && (
                <Card.Root>
                  <Card.Body>
                    <Heading size="md" mb={4}>Your Statistics</Heading>
                    <Grid templateColumns="repeat(auto-fit, minmax(250px, 1fr))" gap={6}>
                      <GridItem>
                        <Text fontWeight="bold" mb={2}>Total Distance</Text>
                        <Text fontSize="2xl" color="blue.500">
                          {formatStatNumber(userStats.progress?.distance, 1)} {athleteProfile?.unit?.distanceLabel || 'km'}
                        </Text>
                      </GridItem>
                      <GridItem>
                        <Text fontWeight="bold" mb={2}>Total Elevation</Text>
                        <Text fontSize="2xl" color="green.500">
                          {formatStatNumber(userStats.progress?.elevation, 0)} {athleteProfile?.unit?.elevationLabel || 'm'}
                        </Text>
                      </GridItem>
                      <GridItem>
                        <Text fontWeight="bold" mb={2}>Total Time</Text>
                        <Text fontSize="2xl" color="purple.500">
                          {formatStatHours(userStats.progress?.movingTime)} h
                        </Text>
                      </GridItem>
                      <GridItem>
                        <Text fontWeight="bold" mb={2}>Activities</Text>
                        <Text fontSize="2xl" color="orange.500">
                          {userStats.progress?.rides ?? 0}
                        </Text>
                      </GridItem>
                    </Grid>
                  </Card.Body>
                </Card.Root>
              )}

              <Card.Root>
                <Card.Body>
                  <Heading size="md" mb={4}>Best Achievements</Heading>
                  {achievementsLoading ? (
                    <HStack gap={3}>
                      <Spinner />
                      <Text>Loading achievements...</Text>
                    </HStack>
                  ) : achievements ? (
                    visibleAchievements.length ? (
                      <Grid
                        display="grid"
                        gridTemplateColumns="repeat(auto-fit, minmax(220px, 1fr))"
                        gap={5}
                      >
                        {visibleAchievements.map((definition) => {
                          const achievement = achievements?.[definition.key];
                          const formatted = definition.formatter(achievement);
                          const activityDate = formatAchievementDate(achievement?.activityTime);
                          const valueWithUnit = formatted.unit
                            ? `${formatted.value} ${formatted.unit}`
                            : formatted.value;
                          return (
                            <Stat.Root
                              key={definition.key}
                              borderWidth="1px"
                              borderRadius="md"
                              p={4}
                              bg="gray.50"
                              shadow="sm"
                            >
                              <Stat.Label fontWeight="bold" color="gray.600">
                                {definition.label}
                              </Stat.Label>
                              <Stat.ValueText fontSize="2xl" color="blue.500">
                                {valueWithUnit}
                              </Stat.ValueText>
                              {achievement?.activityName && (
                                <Stat.HelpText color="gray.600" mt={2}>
                                  {achievement.activityName}
                                </Stat.HelpText>
                              )}
                              {activityDate && (
                                <Stat.HelpText color="gray.500">
                                  {activityDate}
                                </Stat.HelpText>
                              )}
                            </Stat.Root>
                          );
                        })}
                      </Grid>
                    ) : (
                      <Text color="gray.500">
                        No achievements available yet for {selectedActivityType}.
                      </Text>
                    )
                  ) : (
                    <Text color="gray.500">
                      {achievementsError || `No achievements available yet for ${selectedActivityType}.`}
                    </Text>
                  )}
                </Card.Body>
              </Card.Root>

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
