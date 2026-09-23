import { lazy, Suspense, useEffect, useMemo, useState } from "react";
import {
  Badge,
  Box,
  Card,
  Grid,
  Heading,
  HStack,
  Link,
  Spinner,
  Text,
  VStack,
} from "@chakra-ui/react";
import { LuExternalLink, LuMountain, LuRoute } from "react-icons/lu";
import ApiClient from "@/service/ApiClient";
import type { AthleteClimbingInsights, AthleteUnits } from "@/types/athlete";
import { dashboardCardProps } from "./shared";

const ActivityTerrainMap = lazy(() => import("./ActivityTerrainMap"));

type LastActivity = {
  id: number;
  name: string;
  distance?: number;
  total_elevation_gain?: number;
  start_date_local?: string;
  elapsed_time?: number;
};

type ActivityRoutePoint = {
  lat: number;
  lon: number;
  ele?: number;
};

type ActivityRoute = {
  activityId: number;
  source: "gpx" | "polyline" | "streams";
  points: ActivityRoutePoint[];
};

type ElevationSample = {
  distance: number;
  elevation: number;
};

type ElevationBand = {
  fill: string;
  stroke: string;
};

const ELEVATION_PROFILE_LEGEND = [
  { label: "Descent", color: "rgba(8, 145, 178, 0.75)" },
  { label: "Flat", color: "rgba(5, 150, 105, 0.75)" },
  { label: "Climb", color: "rgba(217, 119, 6, 0.75)" },
  { label: "Steep", color: "rgba(220, 38, 38, 0.75)" },
] as const;

const getElevationBand = (grade: number): ElevationBand => {
  if (grade <= -3) {
    return {
      fill: "rgba(14, 116, 144, 0.24)",
      stroke: "rgba(8, 145, 178, 0.5)",
    };
  }

  if (grade < 2) {
    return {
      fill: "rgba(16, 185, 129, 0.22)",
      stroke: "rgba(5, 150, 105, 0.42)",
    };
  }

  if (grade < 6) {
    return {
      fill: "rgba(245, 158, 11, 0.22)",
      stroke: "rgba(217, 119, 6, 0.42)",
    };
  }

  return {
    fill: "rgba(239, 68, 68, 0.2)",
    stroke: "rgba(220, 38, 38, 0.42)",
  };
};

const formatDistance = (distanceMeters?: number, units?: AthleteUnits) => {
  const value = distanceMeters ?? 0;
  if (units?.distanceLabel === "mi") {
    return `${(value / 1609.344).toFixed(1)} mi`;
  }
  return `${(value / 1000).toFixed(1)} km`;
};

const formatElevation = (elevationMeters?: number, units?: AthleteUnits) => {
  const value = elevationMeters ?? 0;
  if (units?.elevationLabel === "ft") {
    return `${Math.round(value * 3.28084)} ft`;
  }
  return `${Math.round(value)} m`;
};

const formatHours = (seconds?: number) => {
  const value = seconds ?? 0;
  return `${(value / 3600).toFixed(value / 3600 >= 10 ? 0 : 1)} h`;
};

const formatElapsedTime = (seconds?: number) => {
  const totalSeconds = Math.max(0, Math.round(seconds ?? 0));
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);

  if (hours > 0) {
    return `${hours}h ${minutes}m`;
  }

  return `${minutes}m`;
};

const formatTrend = (value?: number) => {
  if (typeof value !== "number" || Number.isNaN(value) || value === 0) return "Flat vs baseline";
  return `${value > 0 ? "+" : ""}${value}% vs baseline`;
};

const formatDensity = (value?: number, units?: AthleteUnits) => {
  const amount = value ?? 0;
  if (units?.elevationLabel === "ft") {
    return `${Math.round(amount)} ft / 100 mi`;
  }
  return `${Math.round(amount)} m / 100 km`;
};

const statusCopy = (insights?: AthleteClimbingInsights) => {
  const climbDelta = insights?.recentClimbingDeltaPct ?? 0;
  const rateDelta = insights?.recentClimbingRateDeltaPct ?? 0;

  if (climbDelta >= 20 || rateDelta >= 15) {
    return {
      tone: "green" as const,
      label: "Climbing up",
      text: `Your last 4 weeks are ${Math.max(climbDelta, rateDelta)}% stronger than baseline on climbing load.`
    };
  }

  if (climbDelta <= -15 || rateDelta <= -12) {
    return {
      tone: "orange" as const,
      label: "Backed off",
      text: `Recent climbing volume sits below your usual level, which can be a good recovery window.`
    };
  }

  return {
    tone: "blue" as const,
    label: "Steady hills",
    text: "Your climbing mix is tracking close to baseline with no major terrain shift."
  };
};

const formatDate = (dateValue?: string) => {
  if (!dateValue) return "Latest activity";

  const date = new Date(dateValue);
  if (Number.isNaN(date.getTime())) return "Latest activity";

  return new Intl.DateTimeFormat("en-CH", {
    weekday: "short",
    day: "2-digit",
    month: "short",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
};

const toRadians = (value: number) => (value * Math.PI) / 180;

const getDistanceBetweenPoints = (start: ActivityRoutePoint, end: ActivityRoutePoint) => {
  const earthRadius = 6371000;
  const lat1 = toRadians(start.lat);
  const lat2 = toRadians(end.lat);
  const deltaLat = lat2 - lat1;
  const deltaLon = toRadians(end.lon - start.lon);

  const a = Math.sin(deltaLat / 2) ** 2
    + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLon / 2) ** 2;

  return 2 * earthRadius * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
};

const buildElevationSamples = (points: ActivityRoutePoint[]) => {
  const elevatedPoints = points.filter((point): point is ActivityRoutePoint & { ele: number } => typeof point.ele === "number");
  if (elevatedPoints.length < 2) return [] as ElevationSample[];

  let totalDistance = 0;
  const samples: ElevationSample[] = [{ distance: 0, elevation: elevatedPoints[0].ele }];

  for (let index = 1; index < elevatedPoints.length; index += 1) {
    totalDistance += getDistanceBetweenPoints(elevatedPoints[index - 1], elevatedPoints[index]);
    samples.push({ distance: totalDistance, elevation: elevatedPoints[index].ele });
  }

  return samples;
};

const ElevationProfile = ({ samples, units }: { samples: ElevationSample[]; units: AthleteUnits }) => {
  const width = 860;
  const height = 110;
  const paddingX = 16;
  const paddingTop = 12;
  const paddingBottom = 20;
  const elevations = samples.map((sample) => sample.elevation);
  const minElevation = Math.min(...elevations);
  const maxElevation = Math.max(...elevations);
  const elevationSpan = Math.max(maxElevation - minElevation, 1);
  const maxDistance = samples[samples.length - 1]?.distance ?? 1;
  const baselineY = height - paddingBottom;
  const toX = (distance: number) => paddingX + (distance / maxDistance) * (width - paddingX * 2);
  const toY = (elevation: number) => paddingTop + ((maxElevation - elevation) / elevationSpan) * (height - paddingTop - paddingBottom);
  const areaSegments = samples.slice(0, -1).map((sample, index) => {
    const nextSample = samples[index + 1];
    const x1 = toX(sample.distance);
    const y1 = toY(sample.elevation);
    const x2 = toX(nextSample.distance);
    const y2 = toY(nextSample.elevation);
    const grade = ((nextSample.elevation - sample.elevation) / Math.max(nextSample.distance - sample.distance, 1)) * 100;

    return {
      d: `M ${x1} ${baselineY} L ${x1} ${y1} L ${x2} ${y2} L ${x2} ${baselineY} Z`,
      ...getElevationBand(grade),
    };
  });
  const line = samples.map((sample, index) => `${index === 0 ? "M" : "L"} ${toX(sample.distance)} ${toY(sample.elevation)}`).join(" ");

  return (
    <Box borderRadius="18px" p={{ base: 2.5, md: 2.25 }} bg="linear-gradient(180deg, rgba(12, 31, 46, 0.06), rgba(12, 31, 46, 0.02))" border="1px solid rgba(18, 38, 63, 0.06)">
      <HStack justify="space-between" mb={1.25} mx = '0.5rem'>
        <Text textTransform="uppercase" letterSpacing="0.16em" fontSize="xs" color="slate.500" fontWeight="semibold">
          Elevation profile
        </Text>
        <Text fontSize="xs" color="slate.500">
          {formatElevation(maxElevation - minElevation, units)} relief
        </Text>
      </HStack>
      <Box borderRadius="14px" overflow="hidden" bg="linear-gradient(180deg, rgba(186, 230, 253, 0.28), rgba(255,255,255,0.76))">
        <svg viewBox={`0 0 ${width} ${height}`} width="100%" height="110" role="img" aria-label="Elevation profile of the latest activity">
          {[0.25, 0.5, 0.75].map((ratio) => (
            <line
              key={ratio}
              x1={paddingX}
              x2={width - paddingX}
              y1={paddingTop + ratio * (height - paddingTop - paddingBottom)}
              y2={paddingTop + ratio * (height - paddingTop - paddingBottom)}
              stroke="rgba(71,85,105,0.12)"
              strokeDasharray="5 7"
            />
          ))}
          {areaSegments.map((segment, index) => (
            <path key={`${samples[index].distance}-${samples[index + 1].distance}`} d={segment.d} fill={segment.fill} stroke={segment.stroke} strokeWidth="1" />
          ))}
          <path d={line} fill="none" stroke="rgba(255,255,255,0.88)" strokeWidth="7" strokeLinecap="round" strokeLinejoin="round" />
          <path d={line} fill="none" stroke="#0f766e" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
          <text x={paddingX} y={height - 6} fill="rgba(51,65,85,0.72)" fontSize="14">0</text>
          <text x={width - paddingX} y={height - 6} textAnchor="end" fill="rgba(51,65,85,0.72)" fontSize="14">
            {formatDistance(maxDistance, units)}
          </text>
        </svg>
      </Box>
      <HStack gap={2.5} mt={1.25} mx='0.5rem' flexWrap="wrap">
        {ELEVATION_PROFILE_LEGEND.map((item) => (
          <HStack key={item.label} gap={1.5} color="slate.500">
            <Box boxSize="8px" borderRadius="full" bg={item.color} boxShadow={`0 0 0 1px ${item.color}`} />
            <Text fontSize="10px" textTransform="uppercase" letterSpacing="0.12em">
              {item.label}
            </Text>
          </HStack>
        ))}
      </HStack>
    </Box>
  );
};

const TerrainScene = ({
  activity,
  route,
  units,
  elevationSummary,
}: {
  activity: LastActivity | null;
  route: ActivityRoute;
  units: AthleteUnits;
  elevationSummary: { high: number; low: number } | null;
}) => {
  const elevationSamples = buildElevationSamples(route.points);

  return (
    <Grid templateColumns={{ base: "1fr", md: "minmax(0, 0.9fr) minmax(260px, 0.74fr)" }} gap={2.5} alignItems="stretch">
      <Suspense fallback={<Box minH="320px" display="grid" placeItems="center"><Spinner size="sm" /></Box>}>
        <ActivityTerrainMap points={route.points} />
      </Suspense>

      <VStack align="stretch" gap={{ base: 2.5, md: 2 }} h="100%">
        {activity ? (
          <Box borderRadius="18px" p={{ base: 3, md: 2.5 }} bg="rgba(255,255,255,0.82)" border="1px solid rgba(18, 38, 63, 0.06)">
            <VStack align="stretch" gap={{ base: 2.5, md: 2 }}>
              <VStack align="stretch" gap={1}>
                <HStack justify="space-between" align="start" gap={3}>
                  {activity?.id ? (
                    <Link
                      href={`https://www.strava.com/activities/${activity.id}`}
                      target="_blank"
                      rel="noopener noreferrer"
                      color="blue.500"
                      width="fit-content"
                      _hover={{ color: "blue.600", textDecoration: "none" }}
                    >
                      <HStack gap={1.5} align="center">
                        <Text textTransform="uppercase" letterSpacing="0.16em" fontSize="xs" fontWeight="semibold">
                          Last Activity
                        </Text>
                        <LuExternalLink size={12} />
                      </HStack>
                    </Link>
                  ) : (
                    <Text textTransform="uppercase" letterSpacing="0.16em" fontSize="xs" color="slate.500" fontWeight="semibold">
                      Last Activity
                    </Text>
                  )}
                  <Text color="slate.600" fontSize="sm" textAlign="right" flexShrink={0}>
                    {formatDate(activity.start_date_local)}
                  </Text>
                </HStack>
                <Heading size="md" color="gray.900" lineHeight="1.25">
                  {activity.name}
                </Heading>
              </VStack>

              <Grid templateColumns="repeat(3, minmax(0, 1fr))" gap={{ base: 2.5, md: 2 }} hideBelow='md'>
                <Box borderRadius="16px" p={{ base: 2.5, md: 2.25 }} bg="rgba(18, 38, 63, 0.04)">
                  <HStack mx='0.5rem' mb={1} color="slate.500">
                    <LuRoute />
                    <Text fontSize="sm">Distance</Text>
                  </HStack>
                  <Text mx='0.5rem' fontSize="md" fontWeight="bold" color="gray.900">
                    {formatDistance(activity.distance, units)}
                  </Text>
                </Box>

                <Box borderRadius="16px" p={{ base: 2.5, md: 2.25 }} bg="rgba(18, 38, 63, 0.04)">
                  <HStack mx='0.5rem' mb={1} color="slate.500">
                    <LuMountain />
                    <Text fontSize="sm">Elevation</Text>
                  </HStack>
                  <Text mx='0.5rem' fontSize="md" fontWeight="bold" color="gray.900">
                    {formatElevation(activity.total_elevation_gain, units)}
                  </Text>
                </Box>

                <Box borderRadius="16px" p={{ base: 2.5, md: 2.25 }} bg="rgba(18, 38, 63, 0.04)">
                  <HStack mx='0.5rem' mb={1} color="slate.500">
                    <Text fontSize="sm">Time</Text>
                  </HStack>
                  <Text mx='0.5rem' fontSize="md" fontWeight="bold" color="gray.900">
                    {formatElapsedTime(activity.elapsed_time)}
                  </Text>
                </Box>
              </Grid>
            </VStack>
          </Box>
        ) : null}

        {elevationSamples.length > 1 && <ElevationProfile samples={elevationSamples} units={units} />}

        <Box mt="auto" hideBelow='md'>
          {elevationSummary && (
            <Box borderRadius="18px" p={{ base: 3, md: 2.5 }} bg="rgba(18, 38, 63, 0.04)" border="1px solid rgba(18, 38, 63, 0.06)">
              <Text fontSize="xs" textAlign='center' color="slate.500" mb={1.25}>Route elevation range</Text>
              <HStack justify="space-between" gap={4} flexWrap="wrap">
                <Box>
                  <Text fontSize="xs" color="slate.500">High point</Text>
                  <Text fontSize="md" fontWeight="bold" color="gray.900">
                    {formatElevation(elevationSummary.high, units)}
                  </Text>
                </Box>
                <Box>
                  <Text fontSize="xs" color="slate.500">Low point</Text>
                  <Text fontSize="md" fontWeight="bold" color="gray.900">
                    {formatElevation(elevationSummary.low, units)}
                  </Text>
                </Box>
              </HStack>
            </Box>
          )}
        </Box>
      </VStack>
    </Grid>
  );
};

interface LastActivityRoute3DProps {
  units: AthleteUnits;
  selectedActivityType: string;
}

const LastActivityRoute3D = ({ units, selectedActivityType }: LastActivityRoute3DProps) => {
  const [activity, setActivity] = useState<LastActivity | null>(null);
  const [route, setRoute] = useState<ActivityRoute | null>(null);
  const [climbingInsights, setClimbingInsights] = useState<AthleteClimbingInsights | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    const fetchLastActivity = async () => {
      try {
        setLoading(true);
        const [data, climbingData] = await Promise.all([
          ApiClient.lastActivity(),
          ApiClient.climbingInsights(selectedActivityType).catch((insightsError) => {
            console.error("Error fetching climbing insights:", insightsError);
            return null;
          }),
        ]);
        if (!active) return;
        setActivity(data ?? null);
        setClimbingInsights(climbingData ?? null);

        if (data?.id) {
          try {
            const routeData = await ApiClient.activityRoute(data.id);
            if (!active) return;
            setRoute(routeData ?? null);
            setError(routeData?.points?.length ? null : "Route unavailable for this activity.");
          } catch (routeError) {
            console.error("Error fetching activity route:", routeError);
            if (!active) return;
            setRoute(null);
            setError("Route unavailable for this activity.");
          }
        } else {
          setRoute(null);
          setError("No latest activity available.");
        }
      } catch (fetchError) {
        console.error("Error fetching last activity:", fetchError);
        if (!active) return;
        setActivity(null);
        setRoute(null);
        setClimbingInsights(null);
        setError("Latest activity route is currently unavailable.");
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void fetchLastActivity();

    return () => {
      active = false;
    };
  }, [selectedActivityType]);

  const elevationSummary = useMemo(() => {
    const elevations = route?.points.map((point) => point.ele).filter((value): value is number => typeof value === "number" && Number.isFinite(value)) ?? [];

    if (elevations.length < 2) return null;

    return {
      high: Math.max(...elevations),
      low: Math.min(...elevations),
    };
  }, [route]);

  const hasRoute = Boolean(route?.points?.length && route.points.length > 1);
  const climbStatus = statusCopy(climbingInsights || undefined);
  const climbingMetrics = [
    {
      label: "4-week climbing",
      value: `${formatElevation(climbingInsights?.rolling4Weeks?.elevation, units)} • ${climbingInsights?.rolling4Weeks?.rides ?? 0} rides`,
      helper: `${formatTrend(climbingInsights?.recentClimbingDeltaPct)}`,
    },
    {
      label: "Elevation / hour",
      value: `${formatElevation(climbingInsights?.rolling4Weeks?.elevationPerHour, units)}/h`,
      helper: formatTrend(climbingInsights?.recentClimbingRateDeltaPct),
    },
    {
      label: "Elevation density",
      value: formatDensity(climbingInsights?.rolling4Weeks?.elevationPer100Km, units),
      helper: formatTrend(climbingInsights?.recentDensityDeltaPct),
    },
    {
      label: "Climbiness score",
      value: `${climbingInsights?.rolling4Weeks?.climbinessScore ?? 0}/100`,
      helper: `Baseline ${climbingInsights?.baseline4Weeks?.climbinessScore ?? 0}/100`,
    },
  ];

  return (
    <Card.Root {...dashboardCardProps} overflow="hidden">
      <Card.Body p={{ base: 4, md: 5 }}>
        <Grid templateColumns={{ base: "1fr", xl: "minmax(0, 1.44fr) minmax(240px, 0.56fr)" }} gap={{ base: 4, md: 5 }} alignItems="stretch">
          {loading ? (
            <HStack gap={3} minH="220px" justify="center" borderRadius="24px" bg="rgba(18, 38, 63, 0.04)">
              <Spinner size="sm" />
              <Text color="slate.600">Building the terrain view of your latest activity...</Text>
            </HStack>
          ) : hasRoute && route ? (
            <TerrainScene activity={activity} route={route} units={units} elevationSummary={elevationSummary} />
          ) : (
            <Box borderRadius="24px" p={{ base: 4, md: 5 }} bg="rgba(18, 38, 63, 0.04)" minH="220px">
              <Badge colorPalette="orange" borderRadius="full" px={3} py={1} mb={4}>
                Terrain unavailable
              </Badge>
              <Heading size="md" color="gray.900" mb={3}>
                We could not build a terrain scene for your latest activity.
              </Heading>
              <Text color="slate.600">
                {error || "No route geometry was returned for the latest activity."}
              </Text>
            </Box>
          )}

          <VStack align="stretch" gap={3} justify="space-between" h="100%">
            <VStack align="stretch" gap={2.5} flex="1" justify="flex-start">
              {loading ? (
                <HStack gap={3} minH="140px">
                  <Spinner size="sm" />
                  <Text color="slate.600">Loading latest activity...</Text>
                </HStack>
              ) : activity ? (
                <>
                  <Box
                    borderRadius="20px"
                    p={3.5}
                    bg="linear-gradient(135deg, rgba(14,116,144,0.08), rgba(15,118,110,0.08))"
                    border="1px solid rgba(14,116,144,0.12)"
                  >
                    <VStack align="stretch" gap={2}>
                      <HStack justify="space-between" align="start" gap={3}>
                        <Text textTransform="uppercase" letterSpacing="0.12em" fontSize="10px" color="teal.700" fontWeight="bold">
                          {selectedActivityType} climbing pulse
                        </Text>
                        <Badge colorPalette={climbStatus.tone} borderRadius="full" px={2.5} py={0.5} flexShrink={0}>
                          {climbStatus.label}
                        </Badge>
                      </HStack>
                      <Text fontSize="sm" color="slate.700" lineHeight="1.6">
                        {climbStatus.text}
                      </Text>
                    </VStack>
                  </Box>

                  <Grid templateColumns={{ base: "1fr", sm: "repeat(2, minmax(0, 1fr))" }} gap={2.5} hideBelow='md'>
                    {climbingMetrics.map((metric) => (
                      <Box key={metric.label} borderRadius="18px" p={3} bg="rgba(18, 38, 63, 0.04)">
                        <Text fontSize="xs" color="slate.500" mb={1}>{metric.label}</Text>
                        <Text fontSize="md" fontWeight="bold" color="gray.900">{metric.value}</Text>
                        <Text fontSize="xs" color="slate.500" mt={1.5} lineHeight="1.5">{metric.helper}</Text>
                      </Box>
                    ))}
                  </Grid>

                </>
              ) : (
                <Box borderRadius="20px" p={4} bg="rgba(18, 38, 63, 0.04)">
                  <Text color="slate.600">{error || "No latest activity available."}</Text>
                </Box>
              )}
            </VStack>

            {climbingInsights?.rolling4Weeks ? (
              <HStack hideBelow='md' justify="space-between" gap={4} flexWrap="wrap" borderRadius="18px" p={3} bg="rgba(255,255,255,0.7)" 
                border="1px solid rgba(18, 38, 63, 0.06)" mt="auto">
                <Box>
                  <Text fontSize="xs" color="slate.500">Rolling block</Text>
                  <Text fontSize="sm" fontWeight="semibold" color="slate.800">
                    {formatHours(climbingInsights.rolling4Weeks.movingTime)} • {formatDistance((climbingInsights.rolling4Weeks.distance ?? 0) * (units.distanceLabel === "mi" ? 1609.344 : 1000), units)}
                  </Text>
                </Box>
                <Box>
                  <Text fontSize="xs" color="slate.500">Baseline climbing</Text>
                  <Text fontSize="sm" fontWeight="semibold" color="slate.800">
                    {formatElevation(climbingInsights.baseline4Weeks?.elevation, units)}
                  </Text>
                </Box>
              </HStack>
            ) : null}
          </VStack>
        </Grid>
      </Card.Body>
    </Card.Root>
  );
};

export default LastActivityRoute3D;
