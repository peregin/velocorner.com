import { useState, useEffect, useMemo } from "react";
import ApiClient from "../service/ApiClient";
import {
  Box,
  Button,
  Card,
  Heading,
  HStack,
  Link,
  Separator,
  Spinner,
  Table,
  Text,
  VStack,
} from "@chakra-ui/react";
import type { AthleteProfile } from "../types/athlete";
import { getAthleteUnits } from "../types/athlete";
import { LuArrowDown, LuArrowUp, LuArrowUpDown, LuExternalLink } from "react-icons/lu";

interface Activity {
  id: number;
  name: string;
  distance: number;
  total_elevation_gain: number;
  moving_time: number;
  elapsed_time: number;
  start_date_local: string;
}

interface TopActivitiesWidgetProps {
  title: string;
  action: "distance" | "elevation";
  selectedActivityType: string;
  athleteProfile: AthleteProfile | null;
}

const formatDate = (dateString: string) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  if (Number.isNaN(date.getTime())) return "";
  const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
  const months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

  const day = String(date.getDate()).padStart(2, '0');
  const month = months[date.getMonth()];
  const year = date.getFullYear();
  const dayName = days[date.getDay()];

  let hours = date.getHours();
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const ampm = hours >= 12 ? 'PM' : 'AM';
  hours = hours % 12;
  hours = hours ? hours : 12;
  const hoursStr = String(hours).padStart(2, '0');

  return `${day} ${month}, ${year}, ${dayName} ${hoursStr}:${minutes} ${ampm}`;
};

const formatElapsedTime = (seconds: number) => {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  if (hours > 0) {
    return `${hours}h ${minutes}m`;
  }
  return `${minutes}m`;
};

type SortKey = "start_date_local" | "name" | "distance" | "total_elevation_gain" | "moving_time";
type SortDirection = "asc" | "desc";

const TopActivitiesWidget = ({
  title,
  action,
  selectedActivityType,
  athleteProfile
}: TopActivitiesWidgetProps) => {
  const [activities, setActivities] = useState<Activity[]>([]);
  const [loading, setLoading] = useState(true);
  const [sortKey, setSortKey] = useState<SortKey>(
    action === "distance" ? "distance" : "total_elevation_gain"
  );
  const [sortDirection, setSortDirection] = useState<SortDirection>("desc");

  useEffect(() => {
    setSortKey(action === "distance" ? "distance" : "total_elevation_gain");
    setSortDirection("desc");
  }, [action]);

  useEffect(() => {
    const fetchActivities = async () => {
      try {
        setLoading(true);
        const data = await ApiClient.topActivities(action, selectedActivityType);
        setActivities(data || []);
      } catch (error) {
        console.error(`Error fetching top ${action} activities:`, error);
        setActivities([]);
      } finally {
        setLoading(false);
      }
    };

    fetchActivities();
  }, [action, selectedActivityType]);

  const units = getAthleteUnits(athleteProfile?.unit);
  const distanceUnit = units.distanceLabel || "km";
  const elevationUnit = units.elevationLabel || "m";

  const sortableColumns: { key: SortKey; label: string; numeric?: boolean }[] = [
    { key: "start_date_local", label: "Date" },
    { key: "name", label: "Activity" },
    { key: "distance", label: "Distance", numeric: true },
    { key: "total_elevation_gain", label: "Elevation", numeric: true },
    { key: "moving_time", label: "Moving / Elapsed", numeric: true },
  ];

  const handleSort = (key: SortKey) => {
    if (sortKey === key) {
      setSortDirection((prev) => (prev === "asc" ? "desc" : "asc"));
      return;
    }
    setSortKey(key);
    if (key === "name") {
      setSortDirection("asc");
      return;
    }
    setSortDirection("desc");
  };

  const sortedActivities = useMemo(() => {
    const copy = [...activities];
    const directionMultiplier = sortDirection === "asc" ? 1 : -1;

    copy.sort((a, b) => {
      let comparison = 0;
      if (sortKey === "name") {
        comparison = a.name.localeCompare(b.name);
      } else if (sortKey === "start_date_local") {
        const left = new Date(a.start_date_local).getTime();
        const right = new Date(b.start_date_local).getTime();
        comparison = left - right;
      } else {
        comparison = (a[sortKey] as number) - (b[sortKey] as number);
      }

      return comparison * directionMultiplier;
    });

    return copy;
  }, [activities, sortDirection, sortKey]);

  const renderSortIcon = (key: SortKey) => {
    if (sortKey !== key) {
      return <LuArrowUpDown size={13} />;
    }
    return sortDirection === "asc" ? <LuArrowUp size={13} /> : <LuArrowDown size={13} />;
  };

  return (
    <Card.Root 
      borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)"
      overflow="hidden"
    >
      <Card.Body p={0}>
        <VStack align="stretch" gap={0}>
          <Box
            px={{ base: 4, md: 5 }}
            py={4}
            bgGradient="linear(to-r, blue.600, cyan.500)"
            color="white"
          >
            <Heading letterSpacing="0.01em" color='black'>{title}</Heading>
          </Box>
          <Separator />
        {loading ? (
          <HStack justify="center" py={8}>
            <Spinner />
            <Text>Loading activities...</Text>
          </HStack>
        ) : activities.length === 0 ? (
          <Text textAlign="center" color="gray.500" py={8}>
            No activities found
          </Text>
        ) : (
          <Box overflowX="auto" px={{ base: 2, md: 3 }} py={3}>
            <Table.Root
              variant="line"
              size="sm"
              css={{
                "--table-border-color": "colors.gray.200",
              }}
            >
              <Table.Header>
                <Table.Row>
                  {sortableColumns.map((column) => (
                    <Table.ColumnHeader key={column.key} py={2.5}>
                      <Button
                        variant="ghost"
                        size="xs"
                        onClick={() => handleSort(column.key)}
                        fontSize="xs"
                        fontWeight="semibold"
                        color="gray.700"
                        px={1.5}
                        minH="24px"
                      >
                        <HStack gap={1.5}>
                          <Text>{column.label}</Text>
                          <Box color={sortKey === column.key ? "blue.600" : "gray.400"}>
                            {renderSortIcon(column.key)}
                          </Box>
                        </HStack>
                      </Button>
                    </Table.ColumnHeader>
                  ))}
                </Table.Row>
              </Table.Header>
              <Table.Body>
                {sortedActivities.map((activity) => (
                  <Table.Row
                    key={activity.id}
                    _hover={{ bg: "blue.50" }}
                    transition="background 0.15s ease"
                  >
                    <Table.Cell fontSize="xs" py={2}>
                      <Link
                        href={`https://www.strava.com/activities/${activity.id}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        color="blue.600"
                        textDecoration="underline"
                        _hover={{ color: "blue.700" }}
                        display="-webkit-box"
                        css={{
                          WebkitLineClamp: "2",
                          WebkitBoxOrient: "vertical",
                        }}
                        overflow="hidden"
                        textOverflow="ellipsis"
                        lineHeight="1.25rem"
                        maxHeight="2.5rem"
                      >
                        <HStack as="span" gap={1.5} align="baseline">
                          {formatDate(activity.start_date_local)}
                          <LuExternalLink size={11} />
                        </HStack>
                      </Link>
                    </Table.Cell>
                    <Table.Cell fontSize="xs" py={2}>
                      <Link
                        href={`https://www.strava.com/activities/${activity.id}`}
                        target="_blank"
                        rel="noopener noreferrer"
                        color="gray.700"
                        textDecoration="underline"
                        _hover={{ color: "blue.700", textDecoration: "underline" }}
                        display="-webkit-box"
                        css={{
                          WebkitLineClamp: "2",
                          WebkitBoxOrient: "vertical",
                        }}
                        overflow="hidden"
                        textOverflow="ellipsis"
                        lineHeight="1.25rem"
                        maxHeight="2.5rem"
                        fontWeight="medium"
                      >
                        {activity.name}
                      </Link>
                    </Table.Cell>
                    <Table.Cell fontSize="xs" py={2} fontWeight="semibold" color="gray.700">
                      {(activity.distance / 1000).toFixed(1)} {distanceUnit}
                    </Table.Cell>
                    <Table.Cell fontSize="xs" py={2} fontWeight="semibold" color="gray.700">
                      {Math.round(activity.total_elevation_gain)} {elevationUnit}
                    </Table.Cell>
                    <Table.Cell fontSize="xs" py={2} color="gray.600">
                      {formatElapsedTime(activity.moving_time)} / {formatElapsedTime(activity.elapsed_time)}
                    </Table.Cell>
                  </Table.Row>
                ))}
              </Table.Body>
            </Table.Root>
          </Box>
        )}
        </VStack>
      </Card.Body>
    </Card.Root>
  );
};

export default TopActivitiesWidget;
