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
  useToast,
  Spinner,
  Badge,
  Divider,
  Image,
  Link
} from "@chakra-ui/react";
import { SearchIcon, ExternalLinkIcon } from "@chakra-ui/icons";

const Brands = () => {
  const [query, setQuery] = useState("");
  const [searchResults, setSearchResults] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
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
      
      const results = await ApiClient.searchBrands(query);
      setSearchResults(results);
      
      if (results.length === 0) {
        toast({
          title: "No Results",
          description: "No brands found matching your search",
          status: "info",
          duration: 3000,
          isClosable: true,
        });
      }
    } catch (error) {
      console.error('Search error:', error);
      toast({
        title: "Search Error",
        description: "Failed to search brands. Please try again.",
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleSuggestion = async (searchTerm) => {
    if (!searchTerm.trim()) return;

    try {
      const results = await ApiClient.suggestBrands(searchTerm);
      setSuggestions(results);
    } catch (error) {
      console.error('Suggestion error:', error);
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const handleInputChange = (e) => {
    const value = e.target.value;
    setQuery(value);
    
    if (value.length >= 2) {
      handleSuggestion(value);
    } else {
      setSuggestions([]);
    }
  };

  const formatPrice = (price, currency = "EUR") => {
    return new Intl.NumberFormat('de-DE', {
      style: 'currency',
      currency: currency
    }).format(price);
  };

  return (
    <Box maxW="1200px" mx="auto" p={6}>
      <VStack spacing={8} align="stretch">
        {/* Header */}
        <Box>
          <Heading size="lg" mb={2}>Brand Search</Heading>
          <Text color="gray.600">
            Search for cycling brands and products across multiple marketplaces
          </Text>
        </Box>

        {/* Search Form */}
        <Card>
          <CardBody>
            <VStack spacing={4}>
              <Box position="relative" width="100%" maxW="600px">
                <HStack>
                  <Input
                    placeholder="Search brands (e.g., 'Shimano', 'SRAM', 'Specialized')"
                    value={query}
                    onChange={handleInputChange}
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
                
                {/* Suggestions Dropdown */}
                {suggestions.length > 0 && (
                  <Box
                    position="absolute"
                    top="100%"
                    left={0}
                    right={0}
                    bg="white"
                    border="1px solid"
                    borderColor="gray.200"
                    borderRadius="md"
                    boxShadow="lg"
                    zIndex={10}
                    maxH="200px"
                    overflowY="auto"
                  >
                    {suggestions.map((brand, index) => (
                      <Box
                        key={index}
                        p={3}
                        cursor="pointer"
                        _hover={{ bg: "gray.50" }}
                        onClick={() => {
                          setQuery(brand.name);
                          setSuggestions([]);
                        }}
                      >
                        <Text fontWeight="medium">{brand.name}</Text>
                        {brand.category && (
                          <Text fontSize="sm" color="gray.600">
                            {brand.category}
                          </Text>
                        )}
                      </Box>
                    ))}
                  </Box>
                )}
              </Box>
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
                    No brands found matching "{query}"
                  </Text>
                )}

                {searchResults.length > 0 && (
                  <List spacing={3}>
                    {searchResults.map((brand, index) => (
                      <ListItem key={brand.id || index}>
                        <Card variant="outline">
                          <CardBody>
                            <VStack align="stretch" spacing={3}>
                              <HStack justify="space-between" align="start">
                                <VStack align="start" spacing={1}>
                                  <Text fontWeight="bold" fontSize="lg">
                                    {brand.name}
                                  </Text>
                                  {brand.category && (
                                    <Badge colorScheme="green" size="sm">
                                      {brand.category}
                                    </Badge>
                                  )}
                                </VStack>
                                {brand.logo && (
                                  <Image
                                    src={brand.logo}
                                    alt={brand.name}
                                    boxSize="50px"
                                    objectFit="contain"
                                    fallbackSrc="https://via.placeholder.com/50x50?text=Logo"
                                  />
                                )}
                              </HStack>
                              
                              {brand.description && (
                                <Text color="gray.600" fontSize="sm">
                                  {brand.description}
                                </Text>
                              )}
                              
                              <Divider />
                              
                              <VStack align="stretch" spacing={2}>
                                <Text fontWeight="medium" fontSize="sm">
                                  Available on:
                                </Text>
                                <HStack spacing={4} wrap="wrap">
                                  {brand.marketplaces?.map((marketplace, mIndex) => (
                                    <Link
                                      key={mIndex}
                                      href={marketplace.url}
                                      isExternal
                                      color="blue.500"
                                      fontSize="sm"
                                    >
                                      <HStack>
                                        <Text>{marketplace.name}</Text>
                                        <ExternalLinkIcon />
                                      </HStack>
                                    </Link>
                                  ))}
                                </HStack>
                              </VStack>
                              
                              {brand.products && brand.products.length > 0 && (
                                <>
                                  <Divider />
                                  <VStack align="stretch" spacing={2}>
                                    <Text fontWeight="medium" fontSize="sm">
                                      Sample Products:
                                    </Text>
                                    {brand.products.slice(0, 3).map((product, pIndex) => (
                                      <Box key={pIndex} p={2} bg="gray.50" borderRadius="md">
                                        <HStack justify="space-between">
                                          <Text fontSize="sm">{product.name}</Text>
                                          {product.price && (
                                            <Text fontSize="sm" fontWeight="bold">
                                              {formatPrice(product.price, product.currency)}
                                            </Text>
                                          )}
                                        </HStack>
                                      </Box>
                                    ))}
                                  </VStack>
                                </>
                              )}
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

export default Brands; 