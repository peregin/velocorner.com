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
  CardBody,
  Heading,
  List,
  ListItem,
  ListIcon,
  useToast,
  Spinner,
  Badge,
  Divider
} from "@chakra-ui/react";
import { SearchIcon, TimeIcon, LocationIcon } from "@chakra-ui/icons";

const Search = () => {
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const toast = useToast();

  const handleSearch = async () => {
    if (!query.trim()) {
      toast({
        title: "Search Error",
        description: "Please enter a search term",
        status: "warning",
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    try {
      setLoading(true);
      setHasSearched(true);
      
      const results = await ApiClient.suggestActivities(query);
      setSearchResults(results);
      
      if (results.length === 0) {
        toast({
          title: "No Results",
          description: "No activities found matching your search",
          status: "info",
          duration: 3000,
          isClosable: true,
        });
      }
    } catch (error) {
      console.error('Search error:', error);
      toast({
        title: "Search Error",
        description: "Failed to search activities. Please try again.",
        status: "error",
        duration: 5000,
        isClosable: true,
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
        <Card>
          <CardBody>
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
                  colorScheme="blue"
                  size="lg"
                  onClick={handleSearch}
                  isLoading={loading}
                  leftIcon={<SearchIcon />}
                >
                  Search
                </Button>
              </HStack>
            </VStack>
          </CardBody>
        </Card>

        {/* Search Results */}
        {hasSearched && (
          <Card>
            <CardBody>
              <VStack spacing={4} align="stretch">
                <HStack justify="space-between">
                  <Heading size="md">
                    Search Results
                    {searchResults.length > 0 && (
                      <Badge ml={2} colorScheme="blue">
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
                                <Badge colorScheme="green">
                                  {activity.type}
                                </Badge>
                              </HStack>
                              
                              <Text color="gray.600">
                                {activity.description || "No description available"}
                              </Text>
                              
                              <HStack spacing={6} color="gray.500" fontSize="sm">
                                <HStack>
                                  <TimeIcon />
                                  <Text>{formatDate(activity.startDate)}</Text>
                                </HStack>
                                <HStack>
                                  <LocationIcon />
                                  <Text>{activity.location || "Unknown location"}</Text>
                                </HStack>
                              </HStack>
                              
                              <Divider />
                              
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
            </CardBody>
          </Card>
        )}
      </VStack>
    </Box>
  );
};

export default Search; 