import React, { useState, useEffect, useRef, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import ApiClient from "../service/ApiClient";
import {
  Box,
  SimpleGrid,
  VStack,
  HStack,
  Button,
  Text,
  Card,
  Heading,
  List,
  ListItem,
  Spinner,
  Badge,
  Separator
} from "@chakra-ui/react";
import { LuSearch, LuClock } from "react-icons/lu";
import { toaster } from "@/components/ui/toaster";
import AutocompleteCombobox from "../components/AutocompleteCombobox";
import ImageCarousel from "@/components/ImageCarousel";

const getNumber = (...values: any[]) => {
  const value = values.find((entry) => typeof entry === "number" && !Number.isNaN(entry));
  return typeof value === "number" ? value : 0;
};

const getString = (...values: any[]) => {
  const value = values.find((entry) => typeof entry === "string" && entry.trim().length > 0);
  return typeof value === "string" ? value.trim() : "";
};

const normalizeActivity = (activity: any) => ({
  ...activity,
  startDate: getString(activity.startDate, activity.start_date, activity.start_date_local),
  distanceValue: getNumber(activity.distance),
  movingTimeValue: getNumber(activity.movingTime, activity.moving_time),
  elapsedTimeValue: getNumber(activity.elapsedTime, activity.elapsed_time),
  elevationValue: getNumber(activity.totalElevationGain, activity.total_elevation_gain),
  averageSpeedValue: getNumber(activity.averageSpeed, activity.average_speed),
  averagePowerValue: getNumber(activity.averagePower, activity.average_power, activity.averageWatts, activity.average_watts),
  averageHeartRateValue: getNumber(activity.averageHeartRate, activity.average_heart_rate, activity.averageHeartrate, activity.average_heartrate),
  averageTemperatureValue: getNumber(activity.averageTemperature, activity.average_temperature, activity.averageTemp, activity.average_temp),
});

const Search = () => {
  const [searchParams] = useSearchParams();
  const urlQuery = searchParams.get('q') || '';
  const urlActivityId = searchParams.get('aid') || '';
  const [query, setQuery] = useState(urlQuery);
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [searchSuggestions, setSearchSuggestions] = useState<Array<{ value: string; label: string; activity: any }>>([]);
  const [loading, setLoading] = useState(false);
  const [suggestionsLoading, setSuggestionsLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const searchTimeoutRef = useRef<number | null>(null);

  // Helper to parse API response - handles both formats
  const parseSuggestionsResponse = (response: any): any[] => {
    if (!response) return [];
    
    // Handle format: { suggestions: [{ value: string, data: string (JSON) }] }
    if (response.suggestions && Array.isArray(response.suggestions)) {
      return response.suggestions.map((s: any) => {
        try {
          const activity = typeof s.data === 'string' ? JSON.parse(s.data) : s.data;
          //console.log("Activity from API:", JSON.stringify(activity))
          return activity;
        } catch (e) {
          return null;
        }
      }).filter((a: any) => a !== null);
    }
    
    // Handle format: array of activities directly
    if (Array.isArray(response)) {
      return response;
    }
    
    return [];
  };

  const fetchSuggestions = useCallback(async (query: string) => {
    if (!query.trim()) {
      setSearchSuggestions([]);
      return;
    }

    try {
      setSuggestionsLoading(true);
      const response = await ApiClient.suggestActivities(query);
      const suggestions = parseSuggestionsResponse(response);
      
      // Format for AutocompleteCombobox
      const formattedSuggestions = suggestions.map((activity: any) => ({
        value: activity.name || '',
        label: activity.name || '',
        activity: activity
      }));
      
      setSearchSuggestions(formattedSuggestions);
    } catch (error) {
      console.error('Error fetching suggestions:', error);
      setSearchSuggestions([]);
    } finally {
      setSuggestionsLoading(false);
    }
  }, []);

  const performSearch = async (searchTerm: string) => {
    if (!searchTerm.trim()) {
      toaster.create({
        title: "Search Error",
        description: "Please enter a search term",
        type: "warning",
        duration: 3000,
      });
      return;
    }

    try {
      setLoading(true);
      setHasSearched(true);
      
      const response = await ApiClient.suggestActivities(searchTerm);
      const results = parseSuggestionsResponse(response).map(normalizeActivity);
      setSearchResults(results);
      
      if (results.length === 0) {
        toaster.create({
          title: "No Results",
          description: "No activities found matching your search",
          type: "info",
          duration: 3000,
        });
      }
    } catch (error) {
      console.error('Search error:', error);
      toaster.create({
        title: "Search Error",
        description: "Failed to search activities. Please try again.",
        type: "error",
        duration: 5000,
      });
    } finally {
      setLoading(false);
    }
  };

  const performActivitySearch = async (activityId: string) => {
    try {
      setLoading(true);
      setHasSearched(true);
      
      const activity = await ApiClient.getActivity(activityId);
      if (activity) {
        const normalizedActivity = normalizeActivity(activity);
        setSearchResults([normalizedActivity]);
        setQuery(normalizedActivity.name || '');
      } else {
        setSearchResults([]);
        toaster.create({
          title: "Not Found",
          description: "Activity not found",
          type: "warning",
          duration: 3000,
        });
      }
    } catch (error) {
      console.error('Error fetching activity:', error);
      toaster.create({
        title: "Search Error",
        description: "Failed to load activity. Please try again.",
        type: "error",
        duration: 5000,
      });
      setSearchResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchInputChange = (value: string) => {
    setQuery(value);
    
    // Clear existing timeout
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }
    
    // Debounce API calls for autocomplete
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
      // Navigate to search page with activity ID
      window.history.replaceState({}, '', `/search?aid=${suggestion.activity.id}`);
      performActivitySearch(suggestion.activity.id.toString());
    } else {
      // Fallback to query search
      setQuery(selectedValue);
      void performSearch(selectedValue);
    }
  };

  const handleSearch = async () => {
    await performSearch(query);
  };

  // Auto-search when query or activity ID parameter is present in URL
  useEffect(() => {
    if (urlActivityId) {
      performActivitySearch(urlActivityId);
    } else if (urlQuery) {
      setQuery(urlQuery);
      performSearch(urlQuery);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [urlQuery, urlActivityId]);

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const formatDistance = (distance: number) => {
    return `${(distance / 1000).toFixed(1)} km`;
  };

  const formatDuration = (duration: number) => {
    if (!duration) return "0m";
    const hours = Math.floor(duration / 3600);
    const minutes = Math.floor((duration % 3600) / 60);
    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }
    return `${minutes}m`;
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return "Unknown date";
    const date = new Date(dateString);
    if (Number.isNaN(date.getTime())) return "Unknown date";
    return date.toLocaleString();
  };

  const formatMetric = (value: number, unit: string, digits = 0) => {
    if (!value) return "n/a";
    return `${value.toFixed(digits)} ${unit}`;
  };

  const formatSpeed = (metersPerSecond: number) => {
    if (!metersPerSecond) return "n/a";
    return `${(metersPerSecond * 3.6).toFixed(1)} km/h`;
  };

  return (
    <Box maxW="1200px" mx="auto" p={6}>
      <ImageCarousel />
      <VStack gap={8} align="stretch" mt='2rem'>
        {/* Header */}
        <Box>
          <Heading size="lg" mb={2}>Activity Search</Heading>
          <Text color="gray.600">
            Search through your activities by title, description, or location
          </Text>
        </Box>

        {/* Search Form */}
        <Card.Root borderRadius='20px'>
          <Card.Body>
            <VStack gap={4}>
              <HStack width="100%" maxW="600px">
                <Box flex={1}>
                  <AutocompleteCombobox
                    value={query}
                    items={searchSuggestions as any}
                    placeholder="Search activities (e.g., 'morning ride', 'mountain trail')"
                    emptyMessage={suggestionsLoading ? "Loading..." : "No activities found"}
                    onInputValueChange={handleSearchInputChange}
                    onSelect={handleSuggestionSelect}
                    onKeyPress={handleKeyPress}
                    itemToString={(item: any) => item?.label || item?.value || item || ''}
                    itemToValue={(item: any) => item?.value || item || ''}
                  />
                </Box>
                <Button
                  colorPalette="blue"
                  size="lg"
                  onClick={handleSearch}
                  loading={loading}
                >
                  <LuSearch style={{ marginRight: '8px' }} />
                  Search
                </Button>
              </HStack>
            </VStack>
          </Card.Body>
        </Card.Root>

        {/* Search Results */}
        {hasSearched && (
          <Card.Root borderRadius='20px'>
            <Card.Body>
              <VStack gap={4} align="stretch">
                <HStack justify="space-between">
                  <Heading size="md">
                    Search Results
                    {searchResults.length > 0 && (
                      <Badge ml={2} colorPalette="blue">
                        {searchResults.length}
                      </Badge>
                    )}
                  </Heading>
                  {loading && <Spinner size="sm" />}
                </HStack>

                {searchResults.length === 0 && !loading && (
                  <Text color="gray.500" textAlign="center" py={8}>
                    No activities found matching "{query}"
                  </Text>
                )}

                {searchResults.length > 0 && (
                  <List.Root gap={3}>
                    {searchResults.map((activity, index) => (
                      <List.Item key={activity.id || index}>
                        <Card.Root variant="outline">
                          <Card.Body>
                            <VStack align="stretch" gap={2}>
                              <HStack justify="space-between">
                                <Text fontWeight="bold" fontSize="lg">
                                  {activity.name}
                                </Text>
                                <Badge colorPalette="green">
                                  {activity.type}
                                </Badge>
                              </HStack>

                              <SimpleGrid columns={{ base: 2, md: 5 }} gap={3}>
                                <Box>
                                  <Text fontSize="xs" color="gray.500" textTransform="uppercase">Elevation</Text>
                                  <Text fontSize="sm" fontWeight="semibold">{formatMetric(activity.elevationValue, "m")}</Text>
                                </Box>
                                <Box>
                                  <Text fontSize="xs" color="gray.500" textTransform="uppercase">Avg speed</Text>
                                  <Text fontSize="sm" fontWeight="semibold">{formatSpeed(activity.averageSpeedValue)}</Text>
                                </Box>
                                <Box>
                                  <Text fontSize="xs" color="gray.500" textTransform="uppercase">Avg power</Text>
                                  <Text fontSize="sm" fontWeight="semibold">{formatMetric(activity.averagePowerValue, "W")}</Text>
                                </Box>
                                <Box>
                                  <Text fontSize="xs" color="gray.500" textTransform="uppercase">Avg heart rate</Text>
                                  <Text fontSize="sm" fontWeight="semibold">{formatMetric(activity.averageHeartRateValue, "bpm")}</Text>
                                </Box>
                                <Box>
                                  <Text fontSize="xs" color="gray.500" textTransform="uppercase">Temperature</Text>
                                  <Text fontSize="sm" fontWeight="semibold">{formatMetric(activity.averageTemperatureValue, "°C")}</Text>
                                </Box>
                              </SimpleGrid>
                              
                              <Separator />
                              
                              <HStack justify="space-between" fontSize="sm">
                                 <HStack>
                                  <LuClock />
                                  <Text>{formatDate(activity.startDate)}</Text>
                                </HStack>
                                <Text>
                                  <strong>Distance:</strong> {formatDistance(activity.distanceValue)}
                                </Text>
                                <Text>
                                  <strong>Moving Time:</strong> {formatDuration(activity.movingTimeValue)}
                                </Text>
                                <Text>
                                  <strong>Duration:</strong> {activity.elapsedTimeValue > 0 ? formatDuration(activity.elapsedTimeValue) : ""}
                                </Text>
                              </HStack>
                            </VStack>
                          </Card.Body>
                        </Card.Root>
                      </List.Item>
                    ))}
                  </List.Root>
                )}
              </VStack>
            </Card.Body>
          </Card.Root>
        )}
      </VStack>
    </Box>
  );
};

export default Search;
