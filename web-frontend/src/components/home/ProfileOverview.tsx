import { Avatar, Badge, Box, Button, Card, Grid, Heading, HStack, Portal, Select, Spinner, Stack, Text, VStack, createListCollection } from "@chakra-ui/react";
import { type KeyboardEvent, useMemo } from "react";
import { FaSearch, FaSignOutAlt } from "react-icons/fa";
import { HiRefresh } from "react-icons/hi";
import AutocompleteCombobox from "@/components/AutocompleteCombobox";
import QuickStats from "@/components/QuickStats";
import WordCloud from "@/components/charts/WordCloud";
import { heroCardProps } from "./shared";
import { getInitials, formatTimestamp } from "@/service/formatters";
import type { AthleteProfile, AthleteUnits } from "@/types/athlete";

export interface SearchSuggestion {
  value: string;
  label: string;
  activity: any;
}

interface ProfileOverviewProps {
  athleteProfile: AthleteProfile | null;
  profileLoading: boolean;
  selectedActivityType: string;
  units: AthleteUnits;
  wordCloud: any[];
  refreshLoading: boolean;
  logoutLoading: boolean;
  searchQuery: string;
  searchSuggestions: SearchSuggestion[];
  suggestionsLoading: boolean;
  onRefresh: () => void;
  onUnitsChange: (unit: "metric" | "imperial") => void;
  onLogout: () => void;
  onSearchInputChange: (value: string) => void;
  onSuggestionSelect: (selectedValue: string) => void;
  onSearch: () => void;
  onSearchKeyPress: (event: KeyboardEvent) => void;
}

const ProfileOverview = ({
  athleteProfile,
  profileLoading,
  selectedActivityType,
  units,
  wordCloud,
  refreshLoading,
  logoutLoading,
  searchQuery,
  searchSuggestions,
  suggestionsLoading,
  onRefresh,
  onUnitsChange,
  onLogout,
  onSearchInputChange,
  onSuggestionSelect,
  onSearch,
  onSearchKeyPress,
}: ProfileOverviewProps) => {
  const unitsCollection = useMemo(
    () =>
      createListCollection({
        items: [
          { value: "metric", label: "Metric" },
          { value: "imperial", label: "Imperial" },
        ],
      }),
    []
  );

  return (
    <Card.Root {...heroCardProps}>
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
                <VStack gap={4} align="stretch" width="100%">
                  <HStack gap={4} align="stretch" width="100%" flexDirection={{ base: "column", xl: "row" }}>
                    <HStack gap={4} align="top" flex="1">
                      <Avatar.Root size="2xl">
                        {athleteProfile.avatarUrl && (
                          <Avatar.Image src={athleteProfile.avatarUrl} alt={athleteProfile.displayName} />
                        )}
                        <Avatar.Fallback>
                          {getInitials(athleteProfile.displayName) || athleteProfile.displayName.charAt(0).toUpperCase()}
                        </Avatar.Fallback>
                      </Avatar.Root>
                      <Box ml="1rem">
                        <Badge colorPalette="green" borderRadius="full" px={3} py={1} mb={3}>
                          Dashboard ready
                        </Badge>
                        <Heading size="2xl" color='black'>Hello, {athleteProfile.displayName}</Heading>
                        {athleteProfile.displayLocation && (
                          <Text color="slate.500">{athleteProfile.displayLocation}</Text>
                        )}
                        {athleteProfile.lastUpdate && (
                          <Text fontSize="xs" color="slate.500">
                            Last updated: {formatTimestamp(athleteProfile.lastUpdate)}
                          </Text>
                        )}
                        <Text mt={3} color="slate.600" maxW="2xl" hideBelow="md">
                          Spot trends, top rides, and personal bests in one place.
                        </Text>
                      </Box>
                    </HStack>

                    <VStack gap={2} align="stretch" flexShrink={0} ml="auto" minW={{ base: "100%", xl: "220px" }}>
                      <Button colorPalette="orange" onClick={onRefresh} loading={refreshLoading} width="100%" borderRadius="full">
                        <HiRefresh />
                        Refresh
                      </Button>
                      <Select.Root
                        collection={unitsCollection}
                        value={[athleteProfile.unit ?? "metric"]}
                        onValueChange={(event) => {
                          if (event.value?.[0]) {
                            onUnitsChange(event.value[0] as "metric" | "imperial");
                          }
                        }}
                        size="sm"
                        width="100%"
                      >
                        <Select.HiddenSelect />
                        <Select.Control>
                          <Select.Trigger
                            borderRadius="full"
                            bg="rgba(255,255,255,0.72)"
                            border="1px solid"
                            borderColor="rgba(20, 32, 51, 0.08)">
                            <Select.ValueText ml='1rem'/>
                            <Select.IndicatorGroup>
                              <Select.Indicator />
                            </Select.IndicatorGroup>
                          </Select.Trigger>
                        </Select.Control>
                        <Portal>
                          <Select.Positioner>
                            <Select.Content bg='white' color='black'>
                              <Select.Item item={{ value: "metric", label: "Metric" }}>
                                Metric
                                <Select.ItemIndicator color='blue'/>
                              </Select.Item>
                              <Select.Item item={{ value: "imperial", label: "Imperial" }}>
                                Imperial
                                <Select.ItemIndicator />
                              </Select.Item>
                            </Select.Content>
                          </Select.Positioner>
                        </Portal>
                      </Select.Root>
                      <Button color='black' variant="outline" borderRadius="full" onClick={onLogout} loading={logoutLoading} width="100%">
                        <FaSignOutAlt style={{ marginRight: "8px" }} />
                        Logout
                      </Button>
                    </VStack>
                  </HStack>

                  <VStack width="100%">
                    <QuickStats selectedActivityType={selectedActivityType} units={units} />
                  </VStack>

                  <Stack direction={{ base: "column", md: "row" }} gap={2} width="100%" maxW="100%">
                    <Box width="100%">
                      <AutocompleteCombobox
                        value={searchQuery}
                        items={searchSuggestions as any}
                        placeholder="Search for activities ..."
                        emptyMessage={suggestionsLoading ? "Loading..." : "No activities found"}
                        onInputValueChange={onSearchInputChange}
                        onSelect={onSuggestionSelect}
                        onKeyPress={onSearchKeyPress}
                        itemToString={(item: any) => item?.label || item?.value || item || ""}
                        itemToValue={(item: any) => item?.value || item || ""}
                      />
                    </Box>
                    <Button colorPalette="blue" onClick={onSearch} borderRadius="full" width={{ base: "100%", md: "auto" }}>
                      <FaSearch style={{ marginRight: "8px" }} />
                      Search
                    </Button>
                  </Stack>
                </VStack>
              ) : (
                <Text>No profile information available.</Text>
              )}
            </VStack>
          </Box>

          <Box hideBelow="md">
            <WordCloud words={wordCloud} />
          </Box>
        </Grid>
      </Card.Body>
    </Card.Root>
  );
};

export default ProfileOverview;
