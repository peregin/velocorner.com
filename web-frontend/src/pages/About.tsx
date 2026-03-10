import { useEffect, useState } from "react";
import ApiClient from "../service/ApiClient";
import {
  Alert,
  Badge,
  Box,
  Card,
  Grid,
  Heading,
  HStack,
  Link,
  List,
  Progress,
  Separator,
  Text,
  VStack,
} from "@chakra-ui/react";

interface StatusResponse {
  appVersion?: string;
  buildTime?: string;
  upTime?: string;
  gitHash?: string;
  hostOsVersion?: string;
  osVersion?: string;
  dockerBaseImage?: string;
  javaVersion?: string;
  scalaVersion?: string;
  sbtVersion?: string;
  catsVersion?: string;
  playVersion?: string;
  applicationMode?: string;
  pings?: number;
  memoryUsedPercentile?: number;
  memoryTotal?: number;
}

const formatBytes = (bytes: number) => {
  if (!bytes) return "0 B";
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), sizes.length - 1);
  const value = bytes / 1024 ** i;
  return `${value.toFixed(value >= 10 ? 0 : 1)} ${sizes[i]}`;
};

const statusItems = (status: StatusResponse) => [
  { label: "Version", value: status.appVersion, palette: "green" },
  { label: "Build", value: status.buildTime, palette: "cyan" },
  { label: "Uptime", value: status.upTime, palette: "blue" },
  { label: "Git", value: status.gitHash?.slice(0, 10), palette: "gray" },
  { label: "Runtime", value: status.javaVersion, palette: "orange" },
  { label: "Scala", value: status.scalaVersion, palette: "purple" },
  { label: "SBT", value: status.sbtVersion, palette: "teal" },
  { label: "Play", value: status.playVersion, palette: "pink" },
  { label: "Mode", value: status.applicationMode, palette: "yellow" },
  { label: "Pings", value: status.pings?.toString(), palette: "red" },
];

const About = () => {
  const [status, setStatus] = useState<StatusResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const res = await ApiClient.status();
        setStatus(res);
      } catch (e) {
        setError(e as Error);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <Box maxW="1280px" mx="auto" px={{ base: 4, md: 6 }} pb={{ base: 14, md: 20 }}>
      <VStack align="stretch" gap={{ base: 6, md: 8 }}>
        <Card.Root
          borderRadius="32px"
          border="1px solid"
          borderColor="rgba(20, 32, 51, 0.08)"
          bg="rgba(255,255,255,0.76)"
          boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)"
        >
          <Card.Body p={{ base: 6, md: 8 }}>
            <Grid templateColumns={{ base: "1fr", lg: "1.4fr 0.9fr" }} gap={8}>
              <VStack align="start" gap={4}>
                <Badge colorPalette="green" borderRadius="full" px={3} py={1}>
                  About Velocorner
                </Badge>
                <Heading size="2xl" lineHeight="1.05" maxW="14ch">
                  Clearer training insight, less dashboard noise.
                </Heading>
                <Text color="slate.600" fontSize="lg" lineHeight="1.8" maxW="3xl">
                  Velocorner helps endurance athletes compare seasons, review standout rides and runs, and understand progress quickly.
                  The goal is simple: reduce friction between raw activity history and useful decisions.
                </Text>
                <Text color="slate.600" lineHeight="1.8" maxW="3xl">
                  Activity data is synchronized from{" "}
                  <Link href="https://www.strava.com" color="brand.700" target="_blank" rel="noopener noreferrer">
                    Strava
                  </Link>
                  . The platform also exposes an API and spans a deliberately mixed stack across Scala, React, Rust, Kotlin, Go, and search infrastructure.
                </Text>
              </VStack>

              <VStack
                align="stretch"
                gap={4}
                p={{ base: 5, md: 6 }}
                borderRadius="28px"
                bg="linear-gradient(180deg, #0f172a 0%, #16263f 100%)"
                color="white"
              >
                <Text textTransform="uppercase" letterSpacing="0.14em" fontSize="xs" color="whiteAlpha.700">
                  Principles
                </Text>
                {[
                  "Useful by default: lead with the metrics that change decisions.",
                  "Compact but legible: reduce clutter without hiding important context.",
                  "Built in public: practical engineering across multiple services and languages.",
                ].map((item) => (
                  <Box key={item} p={4} borderRadius="2xl" bg="rgba(255,255,255,0.06)" border="1px solid" borderColor="whiteAlpha.100">
                    <Text color="whiteAlpha.900" lineHeight="1.7">
                      {item}
                    </Text>
                  </Box>
                ))}
              </VStack>
            </Grid>
          </Card.Body>
        </Card.Root>

        <Grid templateColumns={{ base: "1fr", lg: "1.15fr 0.85fr" }} gap={6}>
          <Card.Root borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
            <Card.Body p={{ base: 6, md: 7 }}>
              <VStack align="start" gap={4}>
                <Heading size="lg">Platform snapshot</Heading>
                <Text color="slate.600" lineHeight="1.8">
                  Velocorner is both a product and an engineering playground for production-style services, analytics pipelines, and frontend iteration.
                </Text>
                <List.Root gap={2} ps={4}>
                  <List.Item><Text as="span" fontWeight="700">web-app:</Text> Scala, Play Framework, ZIO</List.Item>
                  <List.Item><Text as="span" fontWeight="700">web-frontend:</Text> React, Chakra UI</List.Item>
                  <List.Item><Text as="span" fontWeight="700">crawler-service:</Text> Scala, http4s, cats-effect</List.Item>
                  <List.Item><Text as="span" fontWeight="700">exchange-rate-service:</Text> Rust</List.Item>
                  <List.Item><Text as="span" fontWeight="700">data-provider:</Text> PostgreSQL and storage backends</List.Item>
                  <List.Item><Text as="span" fontWeight="700">data-search:</Text> ZincSearch</List.Item>
                  <List.Item><Text as="span" fontWeight="700">user-service:</Text> Java, Spring Boot</List.Item>
                  <List.Item><Text as="span" fontWeight="700">weather-service:</Text> Kotlin</List.Item>
                  <List.Item><Text as="span" fontWeight="700">health-check-service:</Text> Go</List.Item>
                </List.Root>
              </VStack>
            </Card.Body>
          </Card.Root>

          <Card.Root borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
            <Card.Body p={{ base: 6, md: 7 }}>
              <VStack align="start" gap={4}>
                <Heading size="lg">Developer access</Heading>
                <Text color="slate.600" lineHeight="1.8">
                  The{" "}
                  <Link href="/docs" color="brand.700">
                    Velocorner API
                  </Link>{" "}
                  follows the OpenAPI standard and is Swagger compatible, making it straightforward to inspect and integrate.
                </Text>
                <Text color="slate.600" lineHeight="1.8">
                  Feedback on metrics, integrations, and visualizations is always useful. The product is intentionally evolving in public.
                </Text>
              </VStack>
            </Card.Body>
          </Card.Root>
        </Grid>

        {error && (
          <Alert.Root status="error" borderRadius="2xl">
            <Alert.Indicator />
            <Alert.Title>Failed to load status information.</Alert.Title>
          </Alert.Root>
        )}

        <Card.Root borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
          <Card.Body p={{ base: 6, md: 7 }}>
            <VStack align="stretch" gap={5}>
              <HStack justify="space-between" align="start" flexWrap="wrap">
                <VStack align="start" gap={1}>
                  <Heading size="lg">Build and runtime status</Heading>
                  <Text color="slate.600">
                    Current deployment, platform versions, and memory usage.
                  </Text>
                </VStack>
                {status?.hostOsVersion && (
                  <Badge borderRadius="full" px={3} py={1}>
                    {status.hostOsVersion}
                  </Badge>
                )}
              </HStack>

              {loading ? (
                <VStack align="stretch" gap={3}>
                  <Text color="slate.600">Loading status…</Text>
                  <Progress.Root size="lg" value={null}>
                    <Progress.Track>
                      <Progress.Range />
                    </Progress.Track>
                  </Progress.Root>
                </VStack>
              ) : status ? (
                <VStack align="stretch" gap={5}>
                  <Grid templateColumns={{ base: "repeat(2, 1fr)", md: "repeat(5, 1fr)" }} gap={3}>
                    {statusItems(status).map((item) => (
                      <Box key={item.label} p={4} borderRadius="2xl" bg="white" border="1px solid" borderColor="rgba(20, 32, 51, 0.06)">
                        <Text fontSize="xs" textTransform="uppercase" letterSpacing="0.1em" color="slate.500" mb={2}>
                          {item.label}
                        </Text>
                        <Badge colorPalette={item.palette} borderRadius="full">
                          {item.value || "n/a"}
                        </Badge>
                      </Box>
                    ))}
                  </Grid>

                  <Separator />

                  <VStack align="stretch" gap={3}>
                    <HStack justify="space-between" flexWrap="wrap">
                      <Text fontWeight="600">Memory usage</Text>
                      <Text color="slate.600">
                        {status.memoryUsedPercentile ?? 0}% of {formatBytes(status.memoryTotal || 0)}
                      </Text>
                    </HStack>
                    <Progress.Root value={status.memoryUsedPercentile ?? 0} colorPalette="green">
                      <Progress.Track>
                        <Progress.Range />
                      </Progress.Track>
                    </Progress.Root>
                  </VStack>
                </VStack>
              ) : null}
            </VStack>
          </Card.Body>
        </Card.Root>
      </VStack>
    </Box>
  );
};

export default About;
