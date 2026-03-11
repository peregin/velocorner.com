import { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import ApiClient from "../service/ApiClient";
import { useAuth } from "../service/auth";
import { getInitials, formatTimestamp } from "../service/formatters";
import {
  Avatar,
  Badge,
  Button,
  Heading,
  Text,
  Image,
  Box,
  Grid,
  Stack,
  VStack,
  HStack,
  Card,
  Progress,
  Spinner,
  Tabs,
  Select,
  createListCollection,
  Portal
} from "@chakra-ui/react";
import AutocompleteCombobox from "../components/AutocompleteCombobox";
import { toaster } from "@/components/ui/toaster";
import strava from 'super-tiny-icons/images/svg/strava.svg';
import { FaSignOutAlt, FaSearch } from 'react-icons/fa';
import WordCloud from "../components/charts/WordCloud";
import Weather from "@/components/charts/Weather";
import LineSeriesChart from "@/components/charts/LineSeriesChart";
import BarChart from "@/components/charts/BarChart";
import HeatmapChart from "@/components/charts/HeatmapChart";
import CalendarHeatmap from "@/components/charts/CalendarHeatmap";
import AchievementsWidget from "@/components/AchievementsWidget";
import ActivityStatsWidget from "@/components/ActivityStatsWidget";
import TopActivitiesWidget from "@/components/TopActivitiesWidget";
import QuickStats from "@/components/QuickStats";
import PerformanceSummaryWidget from "@/components/PerformanceSummaryWidget";
import MarketingWidget from "@/components/MarketingWidget";
import { getAthleteUnits } from "../types/athlete";
import { HiRefresh } from "react-icons/hi";
import { useAthleteProfile } from "@/service/useAthleteProfile";

const showError = (title: string, description: string) => {
  toaster.create({ title, description, type: "error", duration: 5000 });
};

const Home = () => {
  const [wordCloud, setWordCloud] = useState<any[]>([]);
  const [activityTypes, setActivityTypes] = useState<string[]>(['Ride']);
  const [selectedActivityType, setSelectedActivityType] = useState('Ride');
  const [loading, setLoading] = useState(true);
  const [logoutLoading, setLogoutLoading] = useState(false);
  const [refreshLoading, setRefreshLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchSuggestions, setSearchSuggestions] = useState<Array<{ value: string; label: string; activity: any }>>([]);
  const [suggestionsLoading, setSuggestionsLoading] = useState(false);
  const searchTimeoutRef = useRef<number | null>(null);
  const initialLoadRef = useRef(false);

  const { isAuthenticated, authLoading, connect, logout } = useAuth();
  const { athleteProfile, profileLoading } = useAthleteProfile(isAuthenticated, true);
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

  const handleLogout = async () => {
    setLogoutLoading(true);
    await logout();
    setLogoutLoading(false);
  };

  const handleRefresh = async () => {
    try {
      setRefreshLoading(true);
      await ApiClient.refreshActivities();
      window.location.reload();
    } catch (error) {
      console.error("Error refreshing activities:", error);
      showError("Refresh Failed", "Unable to refresh activities. Please try again.");
      setRefreshLoading(false);
    }
  };

  const handleUnitsChange = async (selectedUnit: "metric" | "imperial") => {
    try {
      await ApiClient.updateUnits(selectedUnit);
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
      navigate(`/search?q=${encodeURIComponent(selectedValue.trim())}`);
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
    <Box maxW="1280px" mx="auto" px={{ base: 4, md: 6 }} pb={{ base: 2, md: 4 }}>
      <VStack gap={{ base: 6, md: 8 }} align="stretch">
        {!isAuthenticated && (
          <Card.Root
            borderRadius="32px"
            border="1px solid"
            borderColor="rgba(20, 32, 51, 0.08)"
            bg="rgba(255,255,255,0.76)"
            boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)"
            overflow="hidden"
          >
            <Card.Body p={{ base: 6, md: 8 }}>
              <Grid templateColumns={{ base: "1fr", lg: "1.35fr 0.9fr" }} gap={{ base: 8, lg: 10 }} alignItems="center">
                <VStack align="start" gap={5}>
                  <Badge colorPalette="green" borderRadius="full" px={3} py={1}>
                    Modern endurance analytics
                  </Badge>
                  <VStack align="start" gap={3}>
                    <Heading size={{ base: "2xl", md: "3xl" }} lineHeight="1.05" letterSpacing="-0.03em" maxW="15ch" color='black'>
                      Train with a sharper view of your season.
                    </Heading>
                    <Text color="slate.600" fontSize={{ base: "md", md: "lg" }} maxW="2xl" lineHeight="1.8">
                      Velocorner turns Strava activity history into a concise performance cockpit with year-over-year comparisons,
                      standout efforts, and search that gets you back to the ride you want in seconds.
                    </Text>
                  </VStack>
                  <HStack
                    gap={3}
                    flexWrap="wrap"
                    width="100%"
                    align="stretch"
                    flexDirection={{ base: "column", md: "row" }}
                  >
                    {!isAuthenticated ? (
                      <Button
                        colorPalette="orange"
                        size="lg"
                        borderRadius="full"
                        fontWeight="700"
                        px={6}
                        width={{ base: "100%", md: "auto" }}
                        boxShadow="0 14px 28px rgba(252, 121, 52, 0.24)"
                        onClick={connect}
                        loading={authLoading}
                      >
                        <Image src={strava} boxSize="20px" mr={2} />
                        Connect with Strava
                      </Button>
                    ) : (
                      <Button
                        colorPalette="green"
                        size="lg"
                        borderRadius="full"
                        fontWeight="700"
                        px={6}
                        width={{ base: "100%", md: "auto" }}
                        onClick={handleRefresh}
                        loading={refreshLoading}
                      >
                        <HiRefresh />
                        Refresh activities
                      </Button>
                    )}
                    <Button
                      asChild
                      variant="outline"
                      size="lg"
                      borderRadius="full"
                      px={6}
                      width={{ base: "100%", md: "auto" }}
                      color="slate.900"
                      borderColor="blackAlpha.300"
                      _hover={{ bg: "whiteAlpha.900", color: "slate.900" }}
                    >
                      <a href="#stats">Explore dashboard</a>
                    </Button>
                  </HStack>

                  <QuickStats selectedActivityType='Ride' units={getAthleteUnits('metric')} demo={true} />
                </VStack>

                <MarketingWidget />
              </Grid>
            </Card.Body>
          </Card.Root>
        )}

        <Card.Root
          borderRadius="28px"
          border="1px solid"
          borderColor="rgba(20, 32, 51, 0.08)"
          bg="rgba(255,255,255,0.76)"
          boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)"
        >
          <Card.Body p={{ base: 4, md: 6 }}>
            <Weather />
          </Card.Body>
        </Card.Root>

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
            <Grid templateColumns={{ base: "1fr", lg: "1fr 1fr 1.2fr" }} alignItems="stretch" gap={4} id="stats">
              <Box>
                <WordCloud words={wordCloud} height={350} />
              </Box>
              <Box>
                <BarChart title="Year To Date Distance (Sample)" unit={getProfileUnits().distanceLabel}
                  fetchSeries={fetchDemoYtdDistance} height={350} />
              </Box>
              <Box>
                <LineSeriesChart title="Yearly Elevation (Sample)" unit={getProfileUnits().elevationLabel}
                  fetchSeries={fetchDemoYearlyElevation} seriesToShow={5} height={350} />
              </Box>
            </Grid>

            <Card.Root borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
              <Card.Body p={{ base: 4, md: 6 }}>
                <CalendarHeatmap title="Latest Activities (Sample)"
                  fetchDaily={fetchDemoDailyDistance} unitName={getProfileUnits().distanceLabel} maxMonths={8} />
              </Card.Body>
            </Card.Root>

            <Grid templateColumns={{ base: "1fr", lg: "1fr 1fr" }} gap={4}>
              <Box borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
                <HeatmapChart title="Distance Distribution (Sample)" fetchHeatmap={fetchDemoHistogramDistance} height={250} /></Box>
              <Box borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
                <HeatmapChart title="Elevation Distribution (Sample)" fetchHeatmap={fetchDemoHistogramElevation} height={250} />
              </Box>
            </Grid>
          </>
        )}

        {/* Profile Section for Authenticated Users */}
        {isAuthenticated && (
          <>
            <Image alignSelf="flex-end" width='169px' height='31px' src='/images/powered-by-strava1.png' alt="Powered by Strava" />
            {/* Activity Type Tabs */}
            {/* <Text fontWeight="bold">Activity Types</Text> */}
            <Tabs.Root
              variant="outline"
              value={selectedActivityType}
              onValueChange={(e) => e.value && handleActivityTypeChange(e.value)}
            >
              <Tabs.List bg="rgba(255,255,255,0.72)" rounded="full" p={1} border="1px solid" borderColor="rgba(20, 32, 51, 0.08)">
                {activityTypes.map((activityType) => (
                  <Tabs.Trigger
                    key={activityType}
                    value={activityType}
                    color="slate.700"
                    px={5}
                    py={2}
                    borderRadius="full"
                    _selected={{ bg: "slate.900", color: "white", boxShadow: "sm" }}
                  >
                    {activityType}
                  </Tabs.Trigger>
                ))}
              </Tabs.List>
            </Tabs.Root>

            <Card.Root borderRadius="32px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
              <Card.Body p={{ base: 6, md: 8 }}>
                <Grid templateColumns={{ base: "1fr", lg: "1.6fr 0.9fr" }} gap={6}>
                  <Box>
                    <VStack gap={4} align="stretch">
                      {profileLoading ? (
                        <HStack gap={3}>
                          <Spinner />
                          <Text>Loading your profile...</Text>
                        </HStack>
                      ) : athleteProfile ? (
                        // multiple profile entries, avatar, stats, search
                        <VStack gap={4} align="stretch" width="100%">
                          <HStack gap={4} align="stretch" width="100%" flexDirection={{ base: "column", xl: "row" }}>
                            <HStack gap={4} align="top" flex="1">
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
                              <Box ml='1rem'>
                                <Badge colorPalette="green" borderRadius="full" px={3} py={1} mb={3}>
                                  Dashboard ready
                                </Badge>
                                <Heading size="2xl">Hello, {athleteProfile.displayName}</Heading>
                                {athleteProfile.displayLocation && (
                                  <Text color="slate.500">{athleteProfile.displayLocation}</Text>
                                )}
                                {athleteProfile?.lastUpdate && (
                                  <Text fontSize="xs" color="slate.500">
                                    Last updated: {formatTimestamp(athleteProfile?.lastUpdate)}
                                  </Text>
                                )}
                                <Text mt={3} color="slate.600" maxW="2xl" hideBelow='md'>
                                  Spot trends, top rides, and personal bests in one place.
                                </Text>
                              </Box>
                            </HStack>

                            <VStack gap={2} align="stretch" flexShrink={0} ml="auto" minW={{ base: "100%", xl: "220px" }}>
                              <Button
                                colorPalette="orange"
                                onClick={handleRefresh}
                                loading={refreshLoading}
                                width="100%"
                                borderRadius="full"
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
                                width="100%"
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
                              <Button variant="outline" borderRadius="full" onClick={handleLogout} loading={logoutLoading} width="100%">
                                <FaSignOutAlt style={{ marginRight: '8px' }} />
                                Logout
                              </Button>
                            </VStack>
                          </HStack>
                          <VStack width="100%">
                            <QuickStats selectedActivityType={selectedActivityType} units={getProfileUnits()} />
                          </VStack>
                          <Stack
                            direction={{ base: "column", md: "row" }}
                            gap={2}
                            width="100%"
                            maxW="100%"
                          >
                            <Box width="100%">
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
                              borderRadius="full"
                              width={{ base: "100%", md: "auto" }}
                            >
                              <FaSearch style={{ marginRight: '8px' }} />
                              Search
                            </Button>
                          </Stack>
                        </VStack>
                      ) : (
                        <Text>No profile information available.</Text>
                      )}
                    </VStack>
                  </Box>

                  <Box hideBelow='md'>
                    <WordCloud words={wordCloud} />
                  </Box>
                </Grid>
              </Card.Body>
            </Card.Root>

            {/* User Statistics for Authenticated Users */}
            <VStack gap={6} align="stretch" id="stats">
              <Grid templateColumns={{ base: "1fr", xl: "1.5fr 1fr" }} gap={4} alignItems="stretch" hideBelow='md'>
                <Box h="100%">
                  <ActivityStatsWidget
                    selectedActivityType={selectedActivityType}
                    isAuthenticated={isAuthenticated}
                    athleteProfile={athleteProfile}
                  />
                </Box>

                <Box h="100%">
                  <AchievementsWidget
                    selectedActivityType={selectedActivityType}
                    athleteProfile={athleteProfile}
                    isAuthenticated={isAuthenticated}
                  />
                </Box>
              </Grid>

              <PerformanceSummaryWidget isAuthenticated={isAuthenticated} />

              {/* Charts parity to Play widgets */}
              <LineSeriesChart title="Yearly Heatmap (Distance)" unit={getProfileUnits().distanceLabel} fetchSeries={fetchYearlyHeatmap} seriesToShow={2} height={400} />

              <Grid templateColumns={{ base: "1fr", xl: "0.8fr 1.4fr" }} gap={4}>
                <Box><BarChart title="Year To Date Distance" unit={getProfileUnits().distanceLabel} fetchSeries={fetchYtdDistance} height={350} /></Box>
                <Box><LineSeriesChart title="Yearly Distance" unit={getProfileUnits().distanceLabel} fetchSeries={fetchYearlyDistance} seriesToShow={4} height={350} /></Box>
              </Grid>

              <Grid templateColumns={{ base: "1fr", xl: "0.8fr 1.4fr" }} gap={4}>
                <Box><BarChart title="Year To Date Elevation" unit={getProfileUnits().elevationLabel} fetchSeries={fetchYtdElevation} height={350} /></Box>
                <Box><LineSeriesChart title="Yearly Elevation" unit={getProfileUnits().elevationLabel} fetchSeries={fetchYearlyElevation} seriesToShow={4} height={350} /></Box>
              </Grid>

              <Grid templateColumns={{ base: "1fr", xl: "0.8fr 1.4fr" }} gap={4}>
                <Box><BarChart title="Year To Date Time" unit={'h'} fetchSeries={fetchYtdTime} height={350} /></Box>
                <Box><LineSeriesChart title="Yearly Time" unit={'h'} fetchSeries={fetchYearlyTime} seriesToShow={4} height={350} /></Box>
              </Grid>

              <Grid templateColumns={{ base: "1fr", xl: "1fr 1fr" }} gap={4}>
                <Box borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
                  <HeatmapChart title="Activity Distribution for Distance" fetchHeatmap={fetchHistogramDistance} height={250} />
                </Box>
                <Box borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
                  <HeatmapChart title="Activity Distribution for Elevation" fetchHeatmap={fetchHistogramElevation} height={250} />
                </Box>
              </Grid>

              <Grid templateColumns={{ base: "1fr", xl: "1fr 1fr" }} gap={4}>
                <Box>
                  <TopActivitiesWidget
                    title="Longest Activities"
                    action="distance"
                    selectedActivityType={selectedActivityType}
                    athleteProfile={athleteProfile}
                  />
                </Box>
                <Box>
                  <TopActivitiesWidget
                    title="Most Elevation"
                    action="elevation"
                    selectedActivityType={selectedActivityType}
                    athleteProfile={athleteProfile}
                  />
                </Box>
              </Grid>

              <Card.Root borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
                <Card.Body p={{ base: 4, md: 6 }}>
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
          title="Windy Map" style={{ border: 'none', borderRadius: '28px', boxShadow: '0 24px 60px rgba(18, 38, 63, 0.09)' }}>
        </iframe>
      </Box>
    </Box>
  );
};

export default Home;
