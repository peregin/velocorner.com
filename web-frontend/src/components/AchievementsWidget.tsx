import { useState, useEffect } from "react";
import ApiClient from "../service/ApiClient";
import {
  Heading,
  Text,
  Card,
  Spinner,
  HStack,
  Flex,
  List,
} from "@chakra-ui/react";
import type {
  AchievementEntry,
  AchievementMetric,
  AthleteAchievements,
  AthleteProfile,
} from "../types/athlete";
import { LuTrophy } from "react-icons/lu";

interface AchievementsWidgetProps {
  selectedActivityType: string;
  athleteProfile: AthleteProfile | null;
  isAuthenticated: boolean;
}

const AchievementsWidget = ({
  selectedActivityType,
  athleteProfile,
  isAuthenticated,
}: AchievementsWidgetProps) => {
  const [achievements, setAchievements] = useState<AthleteAchievements | null>(
    null
  );
  const [achievementsLoading, setAchievementsLoading] = useState(false);
  const [achievementsError, setAchievementsError] = useState<string | null>(
    null
  );

  useEffect(() => {
    if (!isAuthenticated) {
      setAchievements(null);
      setAchievementsLoading(false);
      setAchievementsError(null);
      return;
    }

    let isActive = true;
    setAchievementsLoading(true);
    setAchievementsError(null);

    ApiClient.achievements(selectedActivityType)
      .then((data) => {
        if (!isActive) return;
        setAchievements(data);
      })
      .catch((error) => {
        console.error("Error fetching achievements:", error);
        if (!isActive) return;
        setAchievements(null);
        setAchievementsError("Unable to load achievements.");
      })
      .finally(() => {
        if (isActive) {
          setAchievementsLoading(false);
        }
      });

    return () => {
      isActive = false;
    };
  }, [selectedActivityType, isAuthenticated]);

  const formatAchievementDate = (dateString?: string) => {
    if (!dateString) return null;
    const date = new Date(dateString);
    if (Number.isNaN(date.getTime())) return null;
    return date.toLocaleDateString();
  };

  const formatDistanceValue = (meters?: number) => {
    if (typeof meters !== "number" || Number.isNaN(meters)) {
      return { value: "—", unit: athleteProfile?.unit?.distanceLabel || "km" };
    }
    const unit = athleteProfile?.unit?.distanceLabel || "km";
    const isMiles = unit.toLowerCase().includes("mi");
    const formatted = isMiles ? meters / 1609.344 : meters / 1000;
    const decimals = formatted >= 100 ? 0 : 1;
    return { value: formatted.toFixed(decimals), unit };
  };

  const formatElevationValue = (meters?: number) => {
    if (typeof meters !== "number" || Number.isNaN(meters)) {
      return { value: "—", unit: athleteProfile?.unit?.elevationLabel || "m" };
    }
    const unit = athleteProfile?.unit?.elevationLabel || "m";
    const isFeet = unit.toLowerCase().includes("ft");
    const formatted = isFeet ? meters * 3.28084 : meters;
    return { value: formatted.toFixed(formatted >= 1000 ? 0 : 1), unit };
  };

  const formatSpeedValue = (metersPerSecond?: number) => {
    if (
      typeof metersPerSecond !== "number" ||
      Number.isNaN(metersPerSecond)
    ) {
      return { value: "—", unit: athleteProfile?.unit?.speedLabel || "km/h" };
    }
    const unit = athleteProfile?.unit?.speedLabel || "km/h";
    const isMph = unit.toLowerCase().includes("mph");
    const formatted = isMph
      ? metersPerSecond * 2.23693629
      : metersPerSecond * 3.6;
    return { value: formatted.toFixed(1), unit };
  };

  const formatDurationValue = (seconds?: number) => {
    if (typeof seconds !== "number" || Number.isNaN(seconds)) {
      return "—";
    }
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const remainingSeconds = seconds % 60;
    if (hours > 0) {
      return `${hours}h ${minutes.toString().padStart(2, "0")}m`;
    }
    if (minutes > 0) {
      return `${minutes}m ${remainingSeconds.toString().padStart(2, "0")}s`;
    }
    return `${remainingSeconds}s`;
  };

  const formatTemperatureValue = (celsius?: number) => {
    if (typeof celsius !== "number" || Number.isNaN(celsius)) {
      return {
        value: "—",
        unit: athleteProfile?.unit?.temperatureLabel || "°C",
      };
    }
    const unit = athleteProfile?.unit?.temperatureLabel || "°C";
    const isFahrenheit = unit.toLowerCase().includes("f");
    const formatted = isFahrenheit ? (celsius * 9) / 5 + 32 : celsius;
    return { value: formatted.toFixed(0), unit };
  };

  const formatPowerValue = (watts?: number) => {
    if (typeof watts !== "number" || Number.isNaN(watts)) {
      return { value: "—", unit: "W" };
    }
    return { value: watts.toFixed(0), unit: "W" };
  };

  const formatHeartRateValue = (bpm?: number) => {
    if (typeof bpm !== "number" || Number.isNaN(bpm)) {
      return { value: "—", unit: "bpm" };
    }
    return { value: bpm.toFixed(0), unit: "bpm" };
  };

  const achievementDefinitions: {
    key: AchievementMetric;
    label: string;
    formatter: (entry?: AchievementEntry) => { value: string; unit?: string };
  }[] = [
    {
      key: "maxDistance",
      label: "Longest Distance",
      formatter: (entry) => formatDistanceValue(entry?.value),
    },
    {
      key: "maxElevation",
      label: "Maximum Elevation",
      formatter: (entry) => formatElevationValue(entry?.value),
    },
    {
      key: "maxTimeInSec",
      label: "Longest Moving Time",
      formatter: (entry) => ({ value: formatDurationValue(entry?.value) }),
    },
    {
      key: "maxAverageSpeed",
      label: "Fastest Average Speed",
      formatter: (entry) => formatSpeedValue(entry?.value),
    },
    {
      key: "maxAveragePower",
      label: "Highest Average Power",
      formatter: (entry) => formatPowerValue(entry?.value),
    },
    {
      key: "maxHeartRate",
      label: "Maximum Heart Rate",
      formatter: (entry) => formatHeartRateValue(entry?.value),
    },
    {
      key: "maxAverageHeartRate",
      label: "Highest Average Heart Rate",
      formatter: (entry) => formatHeartRateValue(entry?.value),
    },
    {
      key: "maxAverageTemperature",
      label: "Warmest Average Temperature",
      formatter: (entry) => formatTemperatureValue(entry?.value),
    },
    {
      key: "minAverageTemperature",
      label: "Coldest Average Temperature",
      formatter: (entry) => formatTemperatureValue(entry?.value),
    },
  ];

  const visibleAchievements = achievements
    ? achievementDefinitions.filter((definition) => achievements?.[definition.key])
    : [];

  return (
    <Card.Root>
      <Card.Body>
        <Heading size="md" mb={2}>
          <Flex align="center" gap={2}>
            <LuTrophy /> Best Achievements
          </Flex>
        </Heading>
        {achievementsLoading ? (
          <HStack gap={2}>
            <Spinner />
            <Text>Loading achievements...</Text>
          </HStack>
        ) : achievements ? (
          visibleAchievements.length ? (
            <>
              <List.Root gap="0" variant="plain" align="center">
                {visibleAchievements.map((definition) => {
                  const achievement = achievements?.[definition.key];
                  const formatted = definition.formatter(achievement);
                  const activityDate = formatAchievementDate(
                    achievement?.activityTime
                  );
                  const valueWithUnit = formatted.unit
                    ? `${formatted.value} ${formatted.unit}`
                    : formatted.value;
                  return (
                    <List.Item key={definition.key}>
                      {definition.label}: {valueWithUnit} {achievement?.activityName}{" "}
                      {activityDate}
                    </List.Item>
                  );
                })}
              </List.Root>
            </>
          ) : (
            <Text color="gray.500">
              No achievements available yet for {selectedActivityType}.
            </Text>
          )
        ) : (
          <Text color="gray.500">
            {achievementsError ||
              `No achievements available yet for ${selectedActivityType}.`}
          </Text>
        )}
      </Card.Body>
    </Card.Root>
  );
};

export default AchievementsWidget;

