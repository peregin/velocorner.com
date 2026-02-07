import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { useNavigate } from "react-router-dom";
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
  SegmentGroup,
  Select,
  createListCollection,
  Portal
} from "@chakra-ui/react";
import AutocompleteCombobox from "../components/ui/AutocompleteCombobox";
import { toaster } from "@/components/ui/toaster";
import strava from 'super-tiny-icons/images/svg/strava.svg';
import { FaSignOutAlt, FaSearch } from 'react-icons/fa';
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
import QuickStats from "@/components/QuickStats";
import type { AthleteProfile } from "../types/athlete";
import { getAthleteUnits } from "../types/athlete";
import { HiRefresh } from "react-icons/hi";

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
  const [refreshLoading, setRefreshLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchSuggestions, setSearchSuggestions] = useState<Array<{ value: string; label: string; activity: any }>>([]);
  const [suggestionsLoading, setSuggestionsLoading] = useState(false);
  const searchTimeoutRef = useRef<number | null>(null);
  const initialLoadRef = useRef(false);

  const { isAuthenticated, authLoading, connect, logout } = useAuth();
  const navigate = useNavigate();

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

  const handleRefresh = async () => {
    // Navigate to /refresh endpoint which handles the refresh and redirects back
    // This matches the old implementation behavior
    setRefreshLoading(true);
    window.location.href = '/refresh';
  };

  const handleUnitsChange = async (selectedUnit: "metric" | "imperial") => {
    try {
      await ApiClient.updateUnits(selectedUnit);
      if (athleteProfile) {
        setAthleteProfile({
          ...athleteProfile,
          unit: selectedUnit
        });
      }
      // TODO: Reload the page to update the unit of measurement, similar to the old implementation, the conversion is calculated on the BE side
      // For now we just reload the page to get the updated units
      window.location.reload();

    } catch (error) {
      console.error('Error updating units:', error);
      showError("Update Failed", "Unable to update units. Please try again.");
    }
  };

  const fetchSuggestions = useCallback(async (query: string) => {
    if (!query.trim() || !isAuthenticated) {
      setSearchSuggestions([]);
      return;
    }

    try {
      setSuggestionsLoading(true);
      const response = await ApiClient.suggestActivities(query);
      // API returns { suggestions: [{ value: string, data: string (JSON) }] }
      if (response && response.suggestions) {
        const suggestions = response.suggestions.map((s: any) => {
          try {
            const activity = typeof s.data === 'string' ? JSON.parse(s.data) : s.data;
            return {
              value: s.value || activity.name,
              label: s.value || activity.name,
              activity: activity
            };
          } catch (e) {
            return {
              value: s.value,
              label: s.value,
              activity: null
            };
          }
        });
        setSearchSuggestions(suggestions);
      } else {
        // Handle case where API might return activities directly
        setSearchSuggestions([]);
      }
    } catch (error) {
      console.error('Error fetching suggestions:', error);
      setSearchSuggestions([]);
    } finally {
      setSuggestionsLoading(false);
    }
  }, [isAuthenticated]);

  const handleSearchInputChange = (value: string) => {
    setSearchQuery(value);
    
    // Clear existing timeout
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }
    
    // Debounce API calls
    if (value.trim().length >= 2) {
      searchTimeoutRef.current = window.setTimeout(() => {
        fetchSuggestions(value);
      }, 300);
    } else {
      setSearchSuggestions([]);
    }
  };

  const handleSuggestionSelect = (selectedValue: string) => {
    const suggestion = searchSuggestions.find(s => s.value === selectedValue);
    if (suggestion && suggestion.activity && suggestion.activity.id) {
      // Navigate to search page with activity ID (similar to old implementation)
      navigate(`/search?aid=${suggestion.activity.id}`);
    } else {
      // Fallback to query search
      handleSearch();
    }
  };

  const handleSearch = () => {
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
    }
  };

  const handleSearchKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);


  const getProfileUnits = () => getAthleteUnits(athleteProfile?.unit);

  // Create collection for Select component
  const unitsCollection = useMemo(() => {
    return createListCollection({
      items: [
        { value: 'metric', label: 'Metric' },
        { value: 'imperial', label: 'Imperial' }
      ],
    });
  }, []);

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
                <BarChart title="Year To Date Distance (Sample)" unit={getProfileUnits().distanceLabel}
                  fetchSeries={fetchDemoYtdDistance} height={350} /></Box>
              <Box flex={1}>
                <LineSeriesChart title="Yearly Elevation (Sample)" unit={getProfileUnits().elevationLabel}
                  fetchSeries={fetchDemoYearlyElevation} seriesToShow={5} height={350} /></Box>
            </HStack>

            <Stats />

            <Card.Root>
              <Card.Body>
                <CalendarHeatmap title="Latest Activities (Sample)"
                  fetchDaily={fetchDemoDailyDistance} unitName={getProfileUnits().distanceLabel} maxMonths={8} />
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
                      {profileLoading ? (
                        <HStack gap={3}>
                          <Spinner />
                          <Text>Loading your profile...</Text>
                        </HStack>
                      ) : athleteProfile ? (
                        <VStack gap={4} align="stretch">
                          <HStack gap={4} align="stretch">
                            <HStack gap={4} align="top">
                              <Avatar.Root size="2xl">
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
                                <Heading size="lg">Hello, {athleteProfile.displayName}</Heading>
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

                            <VStack gap={2} align="stretch">
                              <Button
                                colorPalette="orange"
                                onClick={handleRefresh}
                                loading={refreshLoading}
                              >
                                <HiRefresh />Refresh
                              </Button>
                              <Select.Root
                                collection={unitsCollection}
                                value={[athleteProfile?.unit ?? 'metric']}
                                onValueChange={(e) => {
                                  if (e.value && e.value[0]) {
                                    const selectedUnit = e.value[0] as "metric" | "imperial";
                                    handleUnitsChange(selectedUnit);
                                  }
                                }}
                                size="sm"
                                width="150px"
                              >
                                <Select.HiddenSelect />
                                <Select.Control>
                                  <Select.Trigger>
                                    <Select.ValueText />
                                    <Select.IndicatorGroup>
                                      <Select.Indicator />
                                    </Select.IndicatorGroup>
                                  </Select.Trigger>
                                </Select.Control>
                                <Portal>
                                  <Select.Positioner>
                                    <Select.Content>
                                      <Select.Item item={{ value: 'metric', label: 'Metric' }}>
                                        Metric
                                        <Select.ItemIndicator />
                                      </Select.Item>
                                      <Select.Item item={{ value: 'imperial', label: 'Imperial' }}>
                                        Imperial
                                        <Select.ItemIndicator />
                                      </Select.Item>
                                    </Select.Content>
                                  </Select.Positioner>
                                </Portal>
                              </Select.Root>
                              <Button variant="outline" onClick={handleLogout} loading={logoutLoading}>
                                <FaSignOutAlt style={{ marginRight: '8px' }} />
                                Logout
                              </Button>
                            </VStack>
                          </HStack>
                          <QuickStats athleteProfile={athleteProfile} selectedActivityType={selectedActivityType} />
                          <HStack gap={2} width="100%" maxW="600px">
                            <Box flex={1}>
                              <AutocompleteCombobox
                                value={searchQuery}
                                items={searchSuggestions as any}
                                placeholder="Search for activities ..."
                                emptyMessage={suggestionsLoading ? "Loading..." : "No activities found"}
                                onInputValueChange={handleSearchInputChange}
                                onSelect={handleSuggestionSelect}
                                onKeyPress={handleSearchKeyPress}
                                itemToString={(item: any) => item?.label || item?.value || item || ''}
                                itemToValue={(item: any) => item?.value || item || ''}
                              />
                            </Box>
                            <Button
                              colorPalette="blue"
                              onClick={handleSearch}
                            >
                              <FaSearch style={{ marginRight: '8px' }} />
                              Search
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
              <LineSeriesChart title="Yearly Heatmap (Distance)" unit={getProfileUnits().distanceLabel} fetchSeries={fetchYearlyHeatmap} seriesToShow={2} height={400} />

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex="0 0 30%"><BarChart title="Year To Date Distance" unit={getProfileUnits().distanceLabel} fetchSeries={fetchYtdDistance} height={350} /></Box>
                <Box flex={1}><LineSeriesChart title="Yearly Distance" unit={getProfileUnits().distanceLabel} fetchSeries={fetchYearlyDistance} seriesToShow={4} height={350} /></Box>
              </HStack>

              <HStack align="stretch" gap={4} flexWrap="wrap">
                <Box flex="0 0 30%"><BarChart title="Year To Date Elevation" unit={getProfileUnits().elevationLabel} fetchSeries={fetchYtdElevation} height={350} /></Box>
                <Box flex={1}><LineSeriesChart title="Yearly Elevation" unit={getProfileUnits().elevationLabel} fetchSeries={fetchYearlyElevation} seriesToShow={4} height={350} /></Box>
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
                  <CalendarHeatmap title="All Activities" fetchDaily={fetchDailyDistance} unitName={getProfileUnits().distanceLabel} maxMonths={8} />
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
