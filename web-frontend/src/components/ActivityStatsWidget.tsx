import { useState, useEffect, useRef } from "react";
import { Heading, Text, Grid, GridItem, Card } from "@chakra-ui/react";
import ApiClient from "../service/ApiClient";
import type { UserStats, AthleteProfile } from "../types/athlete";

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
  const statsFetchKeyRef = useRef<string | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      statsFetchKeyRef.current = null;
      setUserStats(null);
      return;
    }

    const currentYear = new Date().getFullYear().toString();
    const currentKey = `${selectedActivityType}-${currentYear}`;
    if (statsFetchKeyRef.current === currentKey) return;
    statsFetchKeyRef.current = currentKey;

    const fetchUserData = async () => {
      try {
        const stats = await ApiClient.profileStatistics(selectedActivityType, currentYear);
        setUserStats(stats);
      } catch (error) {
        console.error('Error fetching user stats:', error);
      }
    };
    fetchUserData();
  }, [selectedActivityType, isAuthenticated]);

  if (!userStats) {
    return null;
  }

  return (
    <Card.Root>
      <Card.Body>
        <Heading size="md" mb={4}>Your Statistics</Heading>
        <Grid templateColumns="repeat(auto-fit, minmax(150px, 1fr))" gap={6}>
          <GridItem>
            <Text fontWeight="bold" mb={2}>Activities</Text>
            <Text fontSize="2xl" color="orange.500">
              {userStats.progress?.rides ?? 0}
            </Text>
          </GridItem>
          <GridItem>
            <Text fontWeight="bold" mb={2}>Total Distance</Text>
            <Text fontSize="2xl" color="blue.500">
              {formatStatNumber(userStats.progress?.distance, 1)} {athleteProfile?.unit?.distanceLabel || 'km'}
            </Text>
          </GridItem>
          <GridItem>
            <Text fontWeight="bold" mb={2}>Total Elevation</Text>
            <Text fontSize="2xl" color="green.500">
              {formatStatNumber(userStats.progress?.elevation, 0)} {athleteProfile?.unit?.elevationLabel || 'm'}
            </Text>
          </GridItem>
          <GridItem>
            <Text fontWeight="bold" mb={2}>Total Time</Text>
            <Text fontSize="2xl" color="purple.500">
              {formatStatHours(userStats.progress?.movingTime)} h
            </Text>
          </GridItem>
          <GridItem>
            <Text fontWeight="bold" mb={2}>Active Days</Text>
            <Text fontSize="2xl" color="orange.500">
              {userStats.progress?.days ?? 0} d
            </Text>
          </GridItem>
        </Grid>
      </Card.Body>
    </Card.Root>
  );
};

export default ActivityStatsWidget;

