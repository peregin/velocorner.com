import React, { useState, useEffect } from "react";
import ApiClient from "../service/ApiClient";
import {
  Box,
  VStack,
  HStack,
  Input,
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

const Search = () => {
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  const handleSearch = async () => {
    if (!query.trim()) {
      toaster.create({
        title: "Search Error",
        description: "Please enter a search term",
        status: "warning",
        duration: 3000,
      });
      return;
    }

    try {
      setLoading(true);
      setHasSearched(true);
      
      const results = await ApiClient.suggestActivities(query);
      setSearchResults(results);
      
      if (results.length === 0) {
        toaster.create({
          title: "No Results",
          description: "No activities found matching your search",
          status: "info",
          duration: 3000,
        });
      }
    } catch (error) {
      console.error('Search error:', error);
      toaster.create({
        title: "Search Error",
        description: "Failed to search activities. Please try again.",
        status: "error",
        duration: 5000,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const formatDistance = (distance) => {
    return `${(distance / 1000).toFixed(1)} km`;
  };

  const formatDuration = (duration) => {
    const hours = Math.floor(duration / 3600);
    const minutes = Math.floor((duration % 3600) / 60);
    return `${hours}h ${minutes}m`;
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString();
  };

  return (
    <Box maxW="1200px" mx="auto" p={6}>
      <VStack spacing={8} align="stretch">
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
            <VStack spacing={4}>
              <HStack width="100%" maxW="600px">
                <Input
                  placeholder="Search activities (e.g., 'morning ride', 'mountain trail')"
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  onKeyPress={handleKeyPress}
                  size="lg"
                />
                <Button
                  colorPalette="blue"
                  size="lg"
                  onClick={handleSearch}
                  isLoading={loading}
                  leftIcon={<LuSearch />}
                >
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
              <VStack spacing={4} align="stretch">
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
                  <List spacing={3}>
                    {searchResults.map((activity, index) => (
                      <ListItem key={activity.id || index}>
                        <Card variant="outline">
                          <CardBody>
                            <VStack align="stretch" spacing={2}>
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
                              
                              <HStack spacing={6} color="gray.500" fontSize="sm">
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
                          </CardBody>
                        </Card>
                      </ListItem>
                    ))}
                  </List>
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
