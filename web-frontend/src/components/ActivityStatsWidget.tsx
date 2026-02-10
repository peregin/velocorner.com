import { useState, useEffect, useRef, useMemo } from "react";
import { Heading, Text, Grid, GridItem, Card, Box, HStack, VStack, Progress, Select, createListCollection, Portal } from "@chakra-ui/react";
import ApiClient from "../service/ApiClient";
import { type UserStats, type AthleteProfile, getAthleteUnits } from "../types/athlete";

interface ActivityStatsWidgetProps {
  selectedActivityType: string;
  isAuthenticated: boolean;
  athleteProfile: AthleteProfile | null;
}

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

const ActivityStatsWidget = ({ selectedActivityType, isAuthenticated, athleteProfile }: ActivityStatsWidgetProps) => {
  const [userStats, setUserStats] = useState<UserStats | null>(null);
  const [selectedYear, setSelectedYear] = useState<string>(new Date().getFullYear().toString());
  const [availableYears, setAvailableYears] = useState<number[]>([]);
  const statsFetchKeyRef = useRef<string | null>(null);

  // Fetch available years when activity type changes
  useEffect(() => {
    if (!isAuthenticated) {
      setAvailableYears([]);
      return;
    }

    const fetchYears = async () => {
      try {
        const years = await ApiClient.activityYears(selectedActivityType);
        setAvailableYears(years);
        // Set current year as default if available, otherwise use the first available year
        const currentYear = new Date().getFullYear();
        if (years.includes(currentYear)) {
          setSelectedYear(currentYear.toString());
        } else if (years.length > 0) {
          setSelectedYear(years[0].toString());
        }
      } catch (error) {
        console.error('Error fetching years:', error);
      }
    };
    fetchYears();
  }, [selectedActivityType, isAuthenticated]);

  // Fetch stats when year or activity type changes
  useEffect(() => {
    if (!isAuthenticated) {
      statsFetchKeyRef.current = null;
      setUserStats(null);
      return;
    }

    const currentKey = `${selectedActivityType}-${selectedYear}`;
    if (statsFetchKeyRef.current === currentKey) return;
    statsFetchKeyRef.current = currentKey;

    const fetchUserData = async () => {
      try {
        const stats = await ApiClient.profileStatistics(selectedActivityType, selectedYear);
        setUserStats(stats);
      } catch (error) {
        console.error('Error fetching user stats:', error);
      }
    };
    fetchUserData();
  }, [selectedActivityType, selectedYear, isAuthenticated]);

  // Create collection for Select component - must be before conditional return
  const yearCollection = useMemo(() => {
    return createListCollection({
      items: availableYears.map(year => ({ value: year.toString(), label: year.toString() })),
    });
  }, [availableYears]);

  if (!userStats) {
    return null;
  }

  const distanceUnit = getAthleteUnits(athleteProfile?.unit).distanceLabel;
  const elevationUnit = getAthleteUnits(athleteProfile?.unit).elevationLabel;

  return (
    <Card.Root>
      <Card.Body>
        <VStack align="stretch" gap={4}>
          <HStack justify="space-between" align="center">
            <Heading size="md">Your Statistics</Heading>
            <HStack gap={2}>
              <Select.Root
                collection={yearCollection}
                value={[selectedYear]}
                onValueChange={(e) => {
                  if (e.value && e.value[0]) {
                    setSelectedYear(e.value[0]);
                  }
                }}
                size="sm"
                width="120px"
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
                      {availableYears.map((year) => (
                        <Select.Item key={year} item={{ value: year.toString(), label: year.toString() }}>
                          {year}
                          <Select.ItemIndicator />
                        </Select.Item>
                      ))}
                    </Select.Content>
                  </Select.Positioner>
                </Portal>
              </Select.Root>
              <Text fontSize="sm" color="gray.500">Year To Date</Text>
            </HStack>
          </HStack>

          {/* Main Statistics */}
          <Grid templateColumns="repeat(auto-fit, minmax(120px, 1fr))" gap={4}>
            <GridItem>
              <Text fontWeight="bold" mb={2} fontSize="sm">Activities</Text>
              <Text fontSize="2xl" color="orange.500">
                {userStats.progress?.rides ?? 0}
              </Text>
            </GridItem>
            <GridItem>
              <Text fontWeight="bold" mb={2} fontSize="sm">Distance</Text>
              <Text fontSize="2xl" color="blue.500">
                {formatStatNumber(userStats.progress?.distance, 1)} <Text as="span" fontSize="sm">{distanceUnit}</Text>
              </Text>
            </GridItem>
            <GridItem>
              <Text fontWeight="bold" mb={2} fontSize="sm">Elevation</Text>
              <Text fontSize="2xl" color="green.500">
                {formatStatNumber(userStats.progress?.elevation, 0)} <Text as="span" fontSize="sm">{elevationUnit}</Text>
              </Text>
            </GridItem>
            <GridItem>
              <Text fontWeight="bold" mb={2} fontSize="sm">Hours</Text>
              <Text fontSize="2xl" color="purple.500">
                {formatStatHours(userStats.progress?.movingTime)} <Text as="span" fontSize="sm">h</Text>
              </Text>
            </GridItem>
            <GridItem>
              <Text fontWeight="bold" mb={2} fontSize="sm">Days</Text>
              <Text fontSize="2xl" color="orange.500">
                {userStats.progress?.days ?? 0} <Text as="span" fontSize="sm">d</Text>
              </Text>
            </GridItem>
          </Grid>

          {/* Yearly Percentile Progress Bar */}
          {userStats.yearlyPercentile !== undefined && (
            <Box>
              <Progress.Root value={userStats.yearlyPercentile} size="sm" colorPalette="blue">
                <Progress.Track>
                  <Progress.Range />
                </Progress.Track>
              </Progress.Root>
            </Box>
          )}

          {/* Commutes Section */}
          <Box bg="gray.50" p={3} borderRadius="md">
            <HStack mb={2}>
              <Text fontWeight="bold" fontSize="sm">Commutes</Text>
            </HStack>
            <Grid templateColumns="repeat(auto-fit, minmax(100px, 1fr))" gap={3}>
              <GridItem>
                <Text fontSize="lg" fontWeight="bold">
                  {userStats.commute?.rides ?? 0}
                </Text>
              </GridItem>
              <GridItem>
                <Text fontSize="lg" fontWeight="bold">
                  {formatStatNumber(userStats.commute?.distance, 1)} <Text as="span" fontSize="xs">{distanceUnit}</Text>
                </Text>
              </GridItem>
              <GridItem>
                <Text fontSize="lg" fontWeight="bold">
                  {formatStatNumber(userStats.commute?.elevation, 0)} <Text as="span" fontSize="xs">{elevationUnit}</Text>
                </Text>
              </GridItem>
              <GridItem>
                <Text fontSize="lg" fontWeight="bold">
                  {formatStatHours(userStats.commute?.movingTime)} <Text as="span" fontSize="xs">h</Text>
                </Text>
              </GridItem>
              <GridItem>
                <Text fontSize="lg" fontWeight="bold">
                  {userStats.commute?.days ?? 0} <Text as="span" fontSize="xs">d</Text>
                </Text>
              </GridItem>
            </Grid>
          </Box>

          {/* Predicted Activities Section */}
          <Box bg="gray.50" p={3} borderRadius="md">
            <HStack mb={2}>
              <Text fontWeight="bold" fontSize="sm">Predicted activities</Text>
            </HStack>
            <Grid templateColumns="repeat(auto-fit, minmax(100px, 1fr))" gap={3}>
              <GridItem>
                <Text fontSize="lg" fontWeight="bold">
                  {userStats.estimate?.rides ?? 0}
                </Text>
              </GridItem>
              <GridItem>
                <Text fontSize="lg" fontWeight="bold">
                  {formatStatNumber(userStats.estimate?.distance, 1)} <Text as="span" fontSize="xs">{distanceUnit}</Text>
                </Text>
              </GridItem>
              <GridItem>
                <Text fontSize="lg" fontWeight="bold">
                  {formatStatNumber(userStats.estimate?.elevation, 0)} <Text as="span" fontSize="xs">{elevationUnit}</Text>
                </Text>
              </GridItem>
              <GridItem>
                <Text fontSize="lg" fontWeight="bold">
                  {formatStatHours(userStats.estimate?.movingTime)} <Text as="span" fontSize="xs">h</Text>
                </Text>
              </GridItem>
              <GridItem>
                <Text fontSize="lg" fontWeight="bold">
                  {userStats.estimate?.days ?? 0} <Text as="span" fontSize="xs">d</Text>
                </Text>
              </GridItem>
            </Grid>
          </Box>
        </VStack>
      </Card.Body>
    </Card.Root>
  );
};

export default ActivityStatsWidget;

