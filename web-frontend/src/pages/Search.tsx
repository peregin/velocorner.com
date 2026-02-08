import React, { useState, useEffect, useRef, useCallback } from "react";
import { useSearchParams } from "react-router-dom";
import ApiClient from "../service/ApiClient";
import {
  Box,
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
import { LuSearch, LuClock, LuInfo } from "react-icons/lu";
import { toaster } from "@/components/ui/toaster";
import AutocompleteCombobox from "../components/ui/AutocompleteCombobox";

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
      const results = parseSuggestionsResponse(response);
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
        setSearchResults([activity]);
        setQuery(activity.name || '');
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
      handleSearch();
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
    const hours = Math.floor(duration / 3600);
    const minutes = Math.floor((duration % 3600) / 60);
    return `${hours}h ${minutes}m`;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Box maxW="1200px" mx="auto" p={6}>
      <VStack gap={8} align="stretch">
        {/* Header */}
        <Box>
          <Heading size="lg" mb={2}>Activity Search</Heading>
          <Text color="gray.600">
            Search through your activities by title, description, or location
          </Text>
        </Box>

        {/* Search Form */}
        <Card.Root>
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
          <Card.Root>
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
                              
                              <Text color="gray.600">
                                {activity.description || "No description available"}
                              </Text>
                              
                              <HStack gap={6} color="gray.500" fontSize="sm">
                                <HStack>
                                  <LuClock />
                                  <Text>{formatDate(activity.startDate)}</Text>
                                </HStack>
                                <HStack>
                                  <LuInfo />
                                  <Text>{activity.location || "Unknown location"}</Text>
                                </HStack>
                              </HStack>
                              
                              <Separator />
                              
                              <HStack justify="space-between" fontSize="sm">
                                <Text>
                                  <strong>Distance:</strong> {formatDistance(activity.distance)}
                                </Text>
                                <Text>
                                  <strong>Duration:</strong> {formatDuration(activity.movingTime)}
                                </Text>
                                <Text>
                                  <strong>Elevation:</strong> {activity.totalElevationGain?.toFixed(0) || 0}m
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
