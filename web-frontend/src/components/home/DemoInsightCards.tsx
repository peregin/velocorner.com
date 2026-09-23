import { lazy, Suspense } from "react";
import { Box, Card, Grid, Heading, HStack, Spinner, Text, VStack } from "@chakra-ui/react";
import { LuGauge, LuMap, LuMountain, LuRoute, LuSparkles, LuTarget } from "react-icons/lu";
import { dashboardCardProps } from "./shared";

const ActivityTerrainMap = lazy(() => import("./ActivityTerrainMap"));

type DemoInsightCardsProps = {
  distanceLabel: string;
  elevationLabel: string;
};

type DemoPoint = {
  x: number;
  y: number;
};

type ProfileSegment = {
  d: string;
  fill: string;
  stroke: string;
};

const DEMO_PROFILE_LEGEND = [
  { label: "Descent", color: "rgba(8, 145, 178, 0.75)" },
  { label: "Flat", color: "rgba(5, 150, 105, 0.75)" },
  { label: "Climb", color: "rgba(217, 119, 6, 0.75)" },
  { label: "Steep", color: "rgba(220, 38, 38, 0.75)" },
] as const;

const getProfileGradientBand = (grade: number) => {
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

const DEMO_ROUTE_POINTS = [
  { lat: 46.6244, lon: 8.0414 },
  { lat: 46.6312, lon: 8.0272 },
  { lat: 46.6414, lon: 8.0174 },
  { lat: 46.6519, lon: 8.0207 },
  { lat: 46.6607, lon: 8.0349 },
  { lat: 46.6648, lon: 8.0527 },
  { lat: 46.6611, lon: 8.0726 },
  { lat: 46.6548, lon: 8.0958 },
  { lat: 46.6453, lon: 8.1071 },
  { lat: 46.6344, lon: 8.0993 },
  { lat: 46.6256, lon: 8.0842 },
  { lat: 46.6178, lon: 8.0671 },
  { lat: 46.6148, lon: 8.0502 },
  { lat: 46.6192, lon: 8.0419 },
  { lat: 46.6248, lon: 8.0432 },
] as const;

const DEMO_PROFILE_CONTROL_POINTS: DemoPoint[] = [
  { x: 16, y: 84 },
  { x: 74, y: 83 },
  { x: 128, y: 81 },
  { x: 182, y: 77 },
  { x: 236, y: 70 },
  { x: 290, y: 56 },
  { x: 344, y: 39 },
  { x: 398, y: 25 },
  { x: 456, y: 34 },
  { x: 516, y: 58 },
  { x: 574, y: 64 },
  { x: 632, y: 42 },
  { x: 690, y: 28 },
  { x: 746, y: 40 },
  { x: 800, y: 60 },
  { x: 844, y: 74 },
];

const interpolatePoint = (start: DemoPoint, end: DemoPoint, ratio: number): DemoPoint => ({
  x: start.x + (end.x - start.x) * ratio,
  y: start.y + (end.y - start.y) * ratio,
});

const densifyPolyline = (points: DemoPoint[], segmentsPerStep: number) => {
  if (points.length < 2) return points;

  const dense: DemoPoint[] = [];
  for (let index = 0; index < points.length - 1; index += 1) {
    const start = points[index];
    const end = points[index + 1];
    for (let segment = 0; segment < segmentsPerStep; segment += 1) {
      dense.push(interpolatePoint(start, end, segment / segmentsPerStep));
    }
  }
  dense.push(points[points.length - 1]);
  return dense;
};

const DEMO_PROFILE_POINTS = densifyPolyline(DEMO_PROFILE_CONTROL_POINTS, 3);
const demoProfileLinePath = DEMO_PROFILE_POINTS.map((point, index) => `${index === 0 ? "M" : "L"} ${point.x} ${point.y}`).join(" ");
const demoProfileBaselineY = 90;
const demoProfileSegments: ProfileSegment[] = DEMO_PROFILE_POINTS.slice(0, -1).map((point, index) => {
  const nextPoint = DEMO_PROFILE_POINTS[index + 1];
  const grade = ((point.y - nextPoint.y) / Math.max(nextPoint.x - point.x, 1)) * 10;

  return {
    d: `M ${point.x} ${demoProfileBaselineY} L ${point.x} ${point.y} L ${nextPoint.x} ${nextPoint.y} L ${nextPoint.x} ${demoProfileBaselineY} Z`,
    ...getProfileGradientBand(grade),
  };
});

const terrainStats = [
  { label: "Distance", valueMetric: "42.6 km", valueImperial: "26.5 mi", icon: LuRoute },
  { label: "Climbing", valueMetric: "1,180 m", valueImperial: "3,871 ft", icon: LuMountain },
  { label: "Relief", valueMetric: "684 m", valueImperial: "2,244 ft", icon: LuMap },
];

const DemoTerrainModelCard = ({ distanceLabel, elevationLabel }: DemoInsightCardsProps) => {
  const distanceValue = distanceLabel === "mi" ? terrainStats[0].valueImperial : terrainStats[0].valueMetric;
  const climbingValue = elevationLabel === "ft" ? terrainStats[1].valueImperial : terrainStats[1].valueMetric;
  const reliefValue = elevationLabel === "ft" ? terrainStats[2].valueImperial : terrainStats[2].valueMetric;
  const highPointValue = elevationLabel === "ft" ? "7,037 ft" : "2,145 m";
  const lowPointValue = elevationLabel === "ft" ? "4,793 ft" : "1,461 m";
  const stats = [
    { ...terrainStats[0], value: distanceValue },
    { ...terrainStats[1], value: climbingValue },
    { ...terrainStats[2], value: reliefValue },
  ];

  return (
    <Card.Root {...dashboardCardProps} overflow="hidden" h="100%">
      <Card.Body p={{ base: 4, md: 4.5 }}>
        <Grid templateColumns={{ base: "1fr", xl: "minmax(0, 1.28fr) minmax(250px, 0.72fr)" }} gap={{ base: 3, md: 4 }} alignItems="stretch">
          <VStack align="stretch" gap={2}>
            <Suspense fallback={<Box minH="320px" display="grid" placeItems="center"><Spinner size="sm" /></Box>}>
              <ActivityTerrainMap points={DEMO_ROUTE_POINTS} />
            </Suspense>

            <Box borderRadius="18px" p={2} bg="linear-gradient(180deg, rgba(12, 31, 46, 0.06), rgba(12, 31, 46, 0.02))" border="1px solid rgba(18, 38, 63, 0.06)">
              <HStack justify="space-between" mb={1.25}>
                <Text textTransform="uppercase" letterSpacing="0.16em" fontSize="xs" color="slate.500" fontWeight="semibold">
                  Elevation profile
                </Text>
                <Text fontSize="xs" color="slate.500">
                  {reliefValue} relief
                </Text>
              </HStack>
              <Box borderRadius="14px" overflow="hidden" bg="linear-gradient(180deg, rgba(186, 230, 253, 0.28), rgba(255,255,255,0.76))">
                <svg viewBox="0 0 860 110" width="100%" height="88" role="img" aria-label="Sample elevation profile">
                  {[31.5, 55, 78.5].map((y) => (
                    <line key={y} x1="16" x2="844" y1={y} y2={y} stroke="rgba(71,85,105,0.12)" strokeDasharray="5 7" />
                  ))}
                  {demoProfileSegments.map((segment, index) => (
                    <path key={`${DEMO_PROFILE_POINTS[index].x}-${DEMO_PROFILE_POINTS[index + 1].x}`} d={segment.d} fill={segment.fill} stroke={segment.stroke} strokeWidth="1" />
                  ))}
                  <path d={demoProfileLinePath} fill="none" stroke="rgba(255,255,255,0.88)" strokeWidth="7" strokeLinecap="round" strokeLinejoin="round" />
                  <path d={demoProfileLinePath} fill="none" stroke="#0f766e" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
                  <text x="16" y="104" fill="rgba(51,65,85,0.72)" fontSize="14">0</text>
                  <text x="844" y="104" textAnchor="end" fill="rgba(51,65,85,0.72)" fontSize="14">{distanceValue}</text>
                </svg>
              </Box>
              <HStack gap={3} mt={1.5} flexWrap="wrap">
                {DEMO_PROFILE_LEGEND.map((item) => (
                  <HStack key={item.label} gap={1.5} color="slate.500">
                    <Box boxSize="8px" borderRadius="full" bg={item.color} boxShadow={`0 0 0 1px ${item.color}`} />
                    <Text fontSize="10px" textTransform="uppercase" letterSpacing="0.12em">
                      {item.label}
                    </Text>
                  </HStack>
                ))}
              </HStack>
            </Box>
          </VStack>

          <VStack align="stretch" gap={2.5} justify="space-between">
            <VStack align="stretch" gap={2}>
              <Text textTransform="uppercase" letterSpacing="0.18em" fontSize="xs" color="slate.500" fontWeight="semibold">
                Last Activity
              </Text>
              <Heading size="md" color="gray.900" lineHeight="1.2">
                Alpine ridge loop
              </Heading>
              <Text color="slate.600" fontSize="sm">Sat, 21 Mar 2026, 08:12</Text>

              <Grid mt='2rem' templateColumns={{ base: "1fr", sm: "repeat(2, minmax(0, 1fr))" }} gap={2}>
                {stats.slice(0, 2).map((stat) => {
                  const Icon = stat.icon;
                  return (
                    <Box key={stat.label} borderRadius="18px" p={2.5} bg="rgba(18, 38, 63, 0.04)">
                      <HStack mb={1} color="slate.500">
                        <Icon />
                        <Text fontSize="sm">{stat.label}</Text>
                      </HStack>
                      <Text fontSize="md" fontWeight="bold" color="gray.900">
                        {stat.value}
                      </Text>
                    </Box>
                  );
                })}
              </Grid>

              <Box borderRadius="18px" p={2.5} bg="rgba(18, 38, 63, 0.04)" border="1px solid rgba(18, 38, 63, 0.06)">
                <Text fontSize="sm" color="slate.500" mb={1}>Terrain span</Text>
                <HStack justify="space-between" gap={4} flexWrap="wrap">
                  <Box>
                    <Text fontSize="xs" color="slate.500">High point</Text>
                    <Text fontSize="sm" fontWeight="bold" color="gray.900">{highPointValue}</Text>
                  </Box>
                  <Box>
                    <Text fontSize="xs" color="slate.500">Low point</Text>
                    <Text fontSize="sm" fontWeight="bold" color="gray.900">{lowPointValue}</Text>
                  </Box>
                </HStack>
              </Box>
            </VStack>

            {/* <VStack align="stretch" gap={3}>
              <Text fontSize="xs" color="slate.600">DEM resolution: 384 x 512</Text>
              <Text color="blue.600" fontWeight="semibold" fontSize="sm">
                Connect Strava to open sample routes like this one
              </Text>
            </VStack> */}
          </VStack>
        </Grid>
      </Card.Body>
    </Card.Root>
  );
};

const DemoPerformancePulseCard = () => {
  const facts = [
    { label: "Trend", value: "Improving", tone: "green.700" },
    { label: "Scope", value: "Last 6 weeks", tone: "slate.700" },
    { label: "Updated", value: "Sample snapshot", tone: "slate.700" },
  ];
  const strengths = ["Sustained climbing is getting steadier.", "Weekend volume is stacking without extra fatigue."];
  const nextMoves = ["Keep one mid-week threshold session.", "Protect recovery before the next long climb block."];

  return (
    <Card.Root
      borderRadius="28px"
      border="1px solid"
      borderColor="rgba(15, 23, 42, 0.07)"
      bg="linear-gradient(180deg, rgba(255,255,255,0.98), rgba(248,250,252,0.92))"
      boxShadow="0 20px 50px rgba(15, 23, 42, 0.08)"
      overflow="hidden"
      h="100%"
    >
      <Card.Body p={{ base: 3.5, md: 3.5 }}>
        <VStack align="stretch" gap={2} h="100%">
          <HStack justify="space-between" align="start" gap={2} flexWrap="wrap">
            <HStack gap={3} color="slate.800" align="center" flex="1 1 220px" minW={0}>
              <Box
                p={2}
                borderRadius="2xl"
                bg="linear-gradient(135deg, rgba(251,191,36,0.2), rgba(249,115,22,0.1))"
                color="orange.700"
                boxShadow="inset 0 0 0 1px rgba(249,115,22,0.12)"
                flexShrink={0}
              >
                <LuSparkles />
              </Box>
              <VStack align="stretch" gap={0.5} minW={0}>
                <Heading size="sm">Performance Pulse</Heading>
                <Text fontSize="xs" color="slate.500" lineClamp={2}>
                  Demo coaching insight with trend and next steps.
                </Text>
              </VStack>
            </HStack>
            <Grid templateColumns={{ base: "1fr", sm: "repeat(3, minmax(0, 1fr))" }} gap={2} flex="999 1 420px" minW={{ base: "100%", md: "380px" }}>
              {facts.map((fact) => (
                <Box key={fact.label} p={2} borderRadius="xl" bg="rgba(255,255,255,0.88)" border="1px solid" borderColor="rgba(15, 23, 42, 0.07)">
                  <VStack align="stretch" gap={1}>
                    <Text fontSize="10px" fontWeight="bold" letterSpacing="0.08em" textTransform="uppercase" color="slate.500">
                      {fact.label}
                    </Text>
                    <Text fontSize="xs" fontWeight="semibold" color={fact.tone} lineHeight="1.35">
                      {fact.value}
                    </Text>
                  </VStack>
                </Box>
              ))}
            </Grid>
          </HStack>

          <Grid templateColumns={{ base: "1fr", md: "repeat(2, minmax(0, 1fr))" }} gap={2}>
            <Box p={2.5} borderRadius="2xl" bg="linear-gradient(135deg, rgba(34,197,94,0.16), rgba(16,185,129,0.08))" border="1px solid" borderColor="rgba(34,197,94,0.18)">
              <VStack align="stretch" gap={1.5}>
                <HStack gap={2} color="green.700">
                  <LuGauge/>
                  <Text fontSize="xs" fontWeight="bold" textTransform="uppercase" letterSpacing="0.08em">
                    What changed
                  </Text>
                </HStack>
                <Text color="slate.700" lineHeight="1.4" fontSize="xs">
                  Climbing power is trending up while recovery stays stable.
                </Text>
              </VStack>
            </Box>

            <Box p={2.5} borderRadius="2xl" bg="rgba(255,255,255,0.9)" border="1px solid" borderColor="rgba(15, 23, 42, 0.07)">
              <VStack align="stretch" gap={1.5}>
                <HStack gap={2} color="slate.600">
                  <LuTarget />
                  <Text fontSize="xs" fontWeight="bold" textTransform="uppercase" letterSpacing="0.08em">
                    Keep leaning in
                  </Text>
                </HStack>
                <VStack align="stretch" gap={1.25}>
                  {strengths.map((strength) => (
                    <Box key={strength} px={2.25} py={1.5} borderRadius="xl" bg="rgba(15, 23, 42, 0.03)">
                      <Text color="slate.700" fontSize="xs" lineHeight="1.35">{strength}</Text>
                    </Box>
                  ))}
                </VStack>
              </VStack>
            </Box>
          </Grid>

          <Box p={2.5} borderRadius="2xl" bg="linear-gradient(180deg, rgba(236,253,245,0.9), rgba(240,253,250,0.72))" border="1px solid" borderColor="rgba(16, 185, 129, 0.14)">
            <VStack align="stretch" gap={1.5}>
              <HStack gap={2} color="green.700">
                <LuTarget />
                <Text fontSize="xs" fontWeight="bold" textTransform="uppercase" letterSpacing="0.08em">
                  Next best move
                </Text>
              </HStack>
              <VStack align="stretch" gap={1.25}>
                {nextMoves.map((move) => (
                  <Box key={move} px={2.25} py={1.5} borderRadius="xl" bg="rgba(255,255,255,0.72)">
                    <Text color="slate.700" fontSize="xs" lineHeight="1.35">{move}</Text>
                  </Box>
                ))}
              </VStack>
            </VStack>
          </Box>
        </VStack>
      </Card.Body>
    </Card.Root>
  );
};

const DemoInsightCards = ({ distanceLabel, elevationLabel }: DemoInsightCardsProps) => (
  <Grid templateColumns={{ base: "1fr", xl: "1.05fr 0.95fr" }} gap={4} alignItems="stretch">
    <DemoTerrainModelCard distanceLabel={distanceLabel} elevationLabel={elevationLabel} />
    <DemoPerformancePulseCard />
  </Grid>
);

export default DemoInsightCards;
