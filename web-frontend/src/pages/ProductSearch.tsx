import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import {
  Badge,
  Box,
  Button,
  Card,
  Grid,
  Heading,
  HStack,
  Image,
  Input,
  Link,
  Portal,
  Select,
  SimpleGrid,
  Spinner,
  Text,
  VStack,
  createListCollection,
} from "@chakra-ui/react";
import { LuExternalLink, LuPackage, LuSearch, LuStar, LuStore } from "react-icons/lu";
import ApiClient from "@/service/ApiClient";
import { toaster } from "@/components/ui/toaster";
import PageMeta from "@/components/PageMeta";

interface ProductSearchResult {
  market?: {
    name?: string;
    url?: string;
    logoUrl?: string;
  };
  brand?: {
    name?: string;
    logoUrl?: string;
  };
  name?: string;
  description?: string;
  price?: {
    value?: number | string;
    currency?: string;
  };
  imageUrl?: string;
  productUrl?: string;
  reviewStars?: number;
  isNew?: boolean;
  onSales?: boolean;
  onStock?: boolean;
}

const sortOptions = createListCollection({
  items: [
    { value: "price", label: "Price" },
    { value: "market", label: "Market" },
    { value: "name", label: "Name" },
    { value: "brand", label: "Brand" },
  ],
});

const sortDirectionOptions = createListCollection({
  items: [
    { value: "asc", label: "Ascending" },
    { value: "desc", label: "Descending" },
  ],
});

const ProductSearch = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialQuery = searchParams.get("query") || "";
  const [query, setQuery] = useState(initialQuery);
  const [results, setResults] = useState<ProductSearchResult[]>([]);
  const [sortBy, setSortBy] = useState("price");
  const [sortDirection, setSortDirection] = useState("asc");
  const [showOnSaleOnly, setShowOnSaleOnly] = useState(false);
  const [showInStockOnly, setShowInStockOnly] = useState(false);
  const [loading, setLoading] = useState(false);
  const [hasSearched, setHasSearched] = useState(Boolean(initialQuery));

  const formatPrice = (price?: ProductSearchResult["price"]) => {
    if (!price?.value) return "Price unavailable";
    const numericValue = typeof price.value === "number"
      ? price.value
      : Number(String(price.value).replace(/[^0-9.-]+/g, ""));
    const formattedValue = Number.isFinite(numericValue) ? numericValue.toFixed(2) : `${price.value}`;
    if (!price.currency) return formattedValue;
    return `${formattedValue} ${price.currency}`;
  };

  const getPriceValue = (product: ProductSearchResult) => {
    const rawValue = product.price?.value;
    if (typeof rawValue === "number") return rawValue;
    if (typeof rawValue === "string") {
      const parsed = Number(rawValue.replace(/[^0-9.-]+/g, ""));
      return Number.isFinite(parsed) ? parsed : Number.POSITIVE_INFINITY;
    }
    return Number.POSITIVE_INFINITY;
  };

  const getTextValue = (value?: string) => value?.trim().toLocaleLowerCase() || "";

  const filteredResults = results.filter((product) => {
    if (showOnSaleOnly && !product.onSales) return false;
    if (showInStockOnly && !product.onStock) return false;
    return true;
  });

  const sortedResults = [...filteredResults].sort((left, right) => {
    let comparison = 0;

    if (sortBy === "price") {
      comparison = getPriceValue(left) - getPriceValue(right);
    } else if (sortBy === "market") {
      comparison = getTextValue(left.market?.name).localeCompare(getTextValue(right.market?.name));
    } else if (sortBy === "brand") {
      comparison = getTextValue(left.brand?.name).localeCompare(getTextValue(right.brand?.name));
    } else {
      comparison = getTextValue(left.name).localeCompare(getTextValue(right.name));
    }

    return sortDirection === "desc" ? comparison * -1 : comparison;
  });

  const performSearch = async (searchTerm: string) => {
    const trimmedQuery = searchTerm.trim();
    if (!trimmedQuery) {
      toaster.create({
        title: "Search Error",
        description: "Please enter a product to search for.",
        type: "warning",
        duration: 3000,
      });
      return;
    }

    try {
      setLoading(true);
      setHasSearched(true);
      setSearchParams({ query: trimmedQuery });

      const response = await ApiClient.productSearch(trimmedQuery);
      const products = Array.isArray(response) ? response : [];
      setResults(products);

      if (products.length === 0) {
        toaster.create({
          title: "No Results",
          description: `No products found for "${trimmedQuery}".`,
          type: "info",
          duration: 3000,
        });
      }
    } catch (error) {
      console.error("Product search error:", error);
      toaster.create({
        title: "Search Error",
        description: "Failed to search products. Please try again.",
        type: "error",
        duration: 5000,
      });
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!initialQuery) return;
    setQuery(initialQuery);
    void performSearch(initialQuery);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialQuery]);

  return (
    <Box maxW="1200px" mx="auto" px={{ base: 4, md: 6 }} py={6}>
      <PageMeta
        title="Product Search & Price Comparison | Velocorner"
        description="Search and compare cycling components, apparel, and accessories across major bike marketplaces."
        canonical="https://velocorner.com/product-search"
      />
      <VStack gap={8} align="stretch">
        <Box>
          <Heading as="h1" size="lg" mb={2}>Product Search</Heading>
          <Text color="gray.600">
            Search cycling products across markets and compare pricing, availability, and offers.
          </Text>
        </Box>

        <Card.Root borderRadius="20px">
          <Card.Body>
            <VStack gap={4}>
              <HStack width="100%" align="stretch">
                <Input
                  size="lg"
                  value={query}
                  placeholder="Search products (e.g. SRAM XX1 cassette, Garmin Edge 840)"
                  onChange={(event) => setQuery(event.target.value)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter") {
                      void performSearch(query);
                    }
                  }}
                />
                <Button colorPalette="blue" size="lg" onClick={() => void performSearch(query)} loading={loading}>
                  <LuSearch />
                  Search
                </Button>
              </HStack>
            </VStack>
          </Card.Body>
        </Card.Root>

        {hasSearched && (
          <Card.Root borderRadius="20px">
            <Card.Body>
              <VStack gap={5} align="stretch">
                <HStack justify="space-between">
                  <Heading size="md">
                    Products
                    {sortedResults.length > 0 && (
                      <Badge ml={2} colorPalette="blue">
                        {sortedResults.length}
                      </Badge>
                    )}
                  </Heading>
                  {loading && <Spinner size="sm" />}
                </HStack>

                {results.length > 0 && (
                  <Grid
                    templateColumns={{ base: "1fr", md: "auto auto minmax(0, 1fr) minmax(0, 1fr)" }}
                    gap={3}
                    alignItems="center"
                  >
                    <Box
                      as="label"
                      display="inline-flex"
                      alignItems="center"
                      gap={2}
                      px={4}
                      py={2}
                      borderRadius="full"
                      border="1px solid"
                      borderColor="rgba(20, 32, 51, 0.08)"
                      bg="white"
                      cursor="pointer"
                      minH="40px"
                      width="fit-content"
                      whiteSpace="nowrap"
                    >
                      <input
                        type="checkbox"
                        checked={showOnSaleOnly}
                        onChange={(event) => setShowOnSaleOnly(event.target.checked)}
                      />
                      <Text color="slate.700" fontWeight="600">On Sale</Text>
                    </Box>
                    <Box
                      as="label"
                      display="inline-flex"
                      alignItems="center"
                      gap={2}
                      px={4}
                      py={2}
                      borderRadius="full"
                      border="1px solid"
                      borderColor="rgba(20, 32, 51, 0.08)"
                      bg="white"
                      cursor="pointer"
                      minH="40px"
                      width="fit-content"
                      whiteSpace="nowrap"
                    >
                      <input
                        type="checkbox"
                        checked={showInStockOnly}
                        onChange={(event) => setShowInStockOnly(event.target.checked)}
                      />
                      <Text color="slate.700" fontWeight="600">In Stock</Text>
                    </Box>

                    <Select.Root
                      collection={sortOptions}
                      value={[sortBy]}
                      onValueChange={(event) => {
                        if (event.value?.[0]) {
                          setSortBy(event.value[0]);
                        }
                      }}
                      size="sm"
                      width="100%"
                    >
                      <Select.HiddenSelect />
                      <Select.Control>
                        <Select.Trigger borderRadius="full" bg="white" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)">
                          <Select.ValueText placeholder="Sort by" ml="1rem" color="black" />
                          <Select.IndicatorGroup>
                            <Select.Indicator />
                          </Select.IndicatorGroup>
                        </Select.Trigger>
                      </Select.Control>
                      <Portal>
                        <Select.Positioner>
                          <Select.Content bg="white" color="black">
                            <Select.Item item={{ value: "price", label: "Price" }}>
                              Price
                              <Select.ItemIndicator color="blue" />
                            </Select.Item>
                            <Select.Item item={{ value: "market", label: "Market" }}>
                              Market
                              <Select.ItemIndicator color="blue" />
                            </Select.Item>
                            <Select.Item item={{ value: "name", label: "Name" }}>
                              Name
                              <Select.ItemIndicator color="blue" />
                            </Select.Item>
                            <Select.Item item={{ value: "brand", label: "Brand" }}>
                              Brand
                              <Select.ItemIndicator color="blue" />
                            </Select.Item>
                          </Select.Content>
                        </Select.Positioner>
                      </Portal>
                    </Select.Root>
                    <Select.Root
                      collection={sortDirectionOptions}
                      value={[sortDirection]}
                      onValueChange={(event) => {
                        if (event.value?.[0]) {
                          setSortDirection(event.value[0]);
                        }
                      }}
                      size="sm"
                      width="100%"
                    >
                      <Select.HiddenSelect />
                      <Select.Control>
                        <Select.Trigger borderRadius="full" bg="white" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)">
                          <Select.ValueText placeholder="Direction" ml="1rem" color="black" />
                          <Select.IndicatorGroup>
                            <Select.Indicator />
                          </Select.IndicatorGroup>
                        </Select.Trigger>
                      </Select.Control>
                      <Portal>
                        <Select.Positioner>
                          <Select.Content bg="white" color="black">
                            <Select.Item item={{ value: "asc", label: "Ascending" }}>
                              Ascending
                              <Select.ItemIndicator color="blue" />
                            </Select.Item>
                            <Select.Item item={{ value: "desc", label: "Descending" }}>
                              Descending
                              <Select.ItemIndicator color="blue" />
                            </Select.Item>
                          </Select.Content>
                        </Select.Positioner>
                      </Portal>
                    </Select.Root>
                  </Grid>
                )}

                {results.length === 0 && !loading && (
                  <Text color="gray.500" textAlign="center" py={8}>
                    No products found matching "{query}".
                  </Text>
                )}

                {results.length > 0 && sortedResults.length === 0 && !loading && (
                  <Text color="gray.500" textAlign="center" py={8}>
                    No products match the selected filters.
                  </Text>
                )}

                {sortedResults.length > 0 && (
                  <VStack gap={4} align="stretch">
                    {sortedResults.map((product, index) => (
                      <Card.Root key={`${product.productUrl || product.name || "product"}-${index}`} variant="outline">
                        <Card.Body>
                          <SimpleGrid columns={{ base: 1, md: 4 }} gap={5}>
                            <Box>
                              <Image
                                src={product.imageUrl}
                                alt={product.name || "Product image"}
                                w="100%"
                                h={{ base: "220px", md: "180px" }}
                                objectFit="contain"
                                borderRadius="lg"
                                bg="gray.50"
                                fallback={<Box h={{ base: "220px", md: "180px" }} borderRadius="lg" bg="gray.50" />}
                              />
                            </Box>

                            <VStack align="stretch" gap={3} gridColumn={{ md: "span 2" }}>
                              <Box>
                                <Heading size="md" mb={2}>{product.name || "Unnamed product"}</Heading>
                                <Text
                                  color="gray.600"
                                  lineHeight="1.6"
                                  overflow="hidden"
                                  display="-webkit-box"
                                  css={{
                                    WebkitBoxOrient: "vertical",
                                    WebkitLineClamp: 6,
                                  }}
                                >
                                  {product.description || "No description available."}
                                </Text>
                              </Box>

                              <HStack wrap="wrap" gap={2}>
                                {product.isNew && <Badge colorPalette="blue">New</Badge>}
                                {product.onSales && <Badge colorPalette="red">On Sale</Badge>}
                                <Badge colorPalette={product.onStock ? "green" : "gray"}>
                                  {product.onStock ? "In Stock" : "Out of Stock"}
                                </Badge>
                                {typeof product.reviewStars === "number" && product.reviewStars > 0 && (
                                  <Badge colorPalette="yellow">
                                    <HStack gap={1}>
                                      <LuStar />
                                      <Text>{product.reviewStars}</Text>
                                    </HStack>
                                  </Badge>
                                )}
                              </HStack>

                              <SimpleGrid columns={{ base: 1, sm: 2 }} gap={3}>
                                <Box>
                                  <Text fontSize="xs" color="gray.500" textTransform="uppercase">Brand</Text>
                                  <HStack mt={1}>
                                    {product.brand?.logoUrl && (
                                      <Image src={product.brand.logoUrl} alt={product.brand.name || "Brand logo"} boxSize="24px" objectFit="contain" />
                                    )}
                                    <Text fontWeight="semibold">{product.brand?.name || "Unknown brand"}</Text>
                                  </HStack>
                                </Box>
                                <Box>
                                  <Text fontSize="xs" color="gray.500" textTransform="uppercase">Market</Text>
                                  <HStack mt={1}>
                                    {product.market?.logoUrl && (
                                      <Image src={product.market.logoUrl} alt={product.market.name || "Market logo"} boxSize="24px" objectFit="contain" />
                                    )}
                                    <Text fontWeight="semibold">{product.market?.name || "Unknown market"}</Text>
                                  </HStack>
                                </Box>
                              </SimpleGrid>
                            </VStack>

                            <VStack align="stretch" gap={4} justify="space-between">
                              <Box>
                                <Text fontSize="xs" color="gray.500" textTransform="uppercase">Price</Text>
                                <Text fontSize="2xl" fontWeight="700" color="blue.600">
                                  {formatPrice(product.price)}
                                </Text>
                              </Box>

                              <VStack align="stretch" gap={2}>
                                {product.market?.url && (
                                  <Link href={product.market.url} target="_blank" rel="noopener noreferrer">
                                    <Button variant="outline" width="100%">
                                      <LuStore />
                                      Visit Market
                                    </Button>
                                  </Link>
                                )}
                                {product.productUrl && (
                                  <Link href={product.productUrl} target="_blank" rel="noopener noreferrer">
                                    <Button colorPalette="blue" width="100%">
                                      <LuPackage />
                                      View Product
                                      <LuExternalLink />
                                    </Button>
                                  </Link>
                                )}
                              </VStack>
                            </VStack>
                          </SimpleGrid>
                        </Card.Body>
                      </Card.Root>
                    ))}
                  </VStack>
                )}
              </VStack>
            </Card.Body>
          </Card.Root>
        )}
      </VStack>
    </Box>
  );
};

export default ProductSearch;
