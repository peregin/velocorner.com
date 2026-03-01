import { useEffect, useState } from "react";
import ApiClient from "../service/ApiClient";
import {
  Box,
  Heading,
  Text,
  VStack,
  HStack,
  Grid,
  GridItem,
  Badge,
  Card,
  Link,
  Separator,
  Progress,
  Alert,
  List,
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
    <Box maxW="1200px" mx="auto" p={6}>
      <VStack align="stretch" gap={8}>
        <Card.Root overflow="hidden">
          <Card.Body bgGradient="to-r" gradientFrom="blue.50" gradientTo="green.50">
            <VStack align="start" gap={3}>
              <Heading size="xl">Welcome to Velocorner</Heading>
              <Text color="gray.700">
                Velocorner helps you understand your training with clear, comparable metrics for cycling, running, and other sports.
                Track yearly progress for distance, time, and elevation, then compare your current season with past seasons through
                year-to-date and heatmap views.
              </Text>
              <Text color="gray.700">
                Activity data is synchronized from{" "}
                <Link href="https://www.strava.com" color="blue.600" target="_blank" rel="noopener noreferrer">
                  Strava
                </Link>{" "}
                and can be extended with services like{" "}
                <Link href="https://connect.garmin.com/" color="blue.600" target="_blank" rel="noopener noreferrer">
                  Garmin Connect
                </Link>
                . Velocorner also includes a product aggregator to help compare cycling component prices.
              </Text>
            </VStack>
          </Card.Body>
        </Card.Root>

        <Card.Root>
          <Card.Body>
            <VStack align="start" gap={2}>
              <Heading size="md">Stay Connected</Heading>
              <Text color="gray.700">
                Feedback shapes the roadmap. If you have ideas for new metrics, visualizations, or integrations, we would love to hear from you.
              </Text>
              <Text color="gray.700">
                Thanks for visiting and riding with Velocorner.
              </Text>
            </VStack>
          </Card.Body>
        </Card.Root>

        {error && (
          <Alert.Root status="error">
            <Alert.Indicator />
            <Alert.Title>Failed to load status information.</Alert.Title>
          </Alert.Root>
        )}

        <Card.Root>
          <Card.Body>
            <VStack align="stretch" gap={4}>
              <Heading size="md">Build and Runtime Status</Heading>
              {loading ? (
                <HStack>
                  <Text>Loading status…</Text>
                  <Progress.Root size="lg" value={null}>
                    <Progress.Track>
                      <Progress.Range />
                    </Progress.Track>
                  </Progress.Root>
                </HStack>
              ) : (
                status && (
                  <VStack align="stretch" gap={4}>
                    <Grid templateColumns="repeat(auto-fit, minmax(220px, 1fr))" gap={4}>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Version</Text>
                        <Badge colorPalette="blue">{status.appVersion}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Build Time</Text>
                        <Badge colorPalette="cyan">{status.buildTime}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Uptime</Text>
                        <Badge colorPalette="green">{status.upTime}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Git</Text>
                        <Badge colorPalette="gray">{status.gitHash?.slice(0, 10)}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Host OS</Text>
                        <Badge>{status.hostOsVersion}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">OS</Text>
                        <Badge>{status.osVersion}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Docker Image</Text>
                        <Badge>{status.dockerBaseImage}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Java</Text>
                        <Badge>{status.javaVersion}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Scala</Text>
                        <Badge>{status.scalaVersion}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">SBT</Text>
                        <Badge>{status.sbtVersion}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Cats</Text>
                        <Badge>{status.catsVersion}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Play</Text>
                        <HStack>
                          <Badge>{status.playVersion}</Badge>
                          <Badge colorPalette="teal">{status.applicationMode}</Badge>
                        </HStack>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Pings</Text>
                        <Badge colorPalette="red">{status.pings}</Badge>
                      </GridItem>
                    </Grid>

                    <Separator />

                    <HStack justify="space-between">
                      <HStack>
                        <Text>Memory usage</Text>
                        <Badge colorPalette="pink">{status.memoryUsedPercentile}%</Badge>
                      </HStack>
                      <Text color="gray.600">Total: {formatBytes(status.memoryTotal || 0)}</Text>
                    </HStack>
                    <Progress.Root value={status.memoryUsedPercentile} defaultValue={90}>
                      <Progress.Label>Memory usage</Progress.Label>
                      <Progress.Track flex='1'>
                        <Progress.Range />
                      </Progress.Track>
                      <Progress.ValueText>{status.memoryUsedPercentile}%</Progress.ValueText>
                    </Progress.Root>
                  </VStack>
                )
              )}
            </VStack>
          </Card.Body>
        </Card.Root>

        <Card.Root>
          <Card.Body>
            <VStack align="start" gap={3}>
              <Heading size="md">Developer API</Heading>
              <Text>
                Use the <Link href="/docs" color="blue.600">Velocorner API</Link> to retrieve activity statistics and integrate them into your own tools.
                The API follows the{" "}
                <Link href="https://www.openapis.org/" color="blue.600" target="_blank" rel="noopener noreferrer">
                  OpenAPI
                </Link>{" "}
                standard and is{" "}
                <Link href="https://swagger.io/" color="blue.600" target="_blank" rel="noopener noreferrer">
                  Swagger
                </Link>{" "}
                compatible.
              </Text>
            </VStack>
          </Card.Body>
        </Card.Root>

        <Card.Root>
          <Card.Body>
            <VStack align="start" gap={3}>
              <Heading size="md">Tech Stack</Heading>
              <Text color="gray.700">
                Velocorner is a practical playground for building and operating a production-grade platform across multiple technologies.
              </Text>
              <List.Root ps={4} gap={1}>
                <List.Item><Text as="span" fontWeight="medium">web-app:</Text> Scala, Play Framework, ZIO</List.Item>
                <List.Item><Text as="span" fontWeight="medium">web-frontend:</Text> React</List.Item>
                <List.Item><Text as="span" fontWeight="medium">crawler-service:</Text> Scala, http4s, cats-effect, circe</List.Item>
                <List.Item><Text as="span" fontWeight="medium">exchange-rate-service:</Text> Rust</List.Item>
                <List.Item><Text as="span" fontWeight="medium">data-provider:</Text> PostgreSQL and other storage backends</List.Item>
                <List.Item><Text as="span" fontWeight="medium">data-search:</Text> ZincSearch</List.Item>
                <List.Item><Text as="span" fontWeight="medium">user-service:</Text> Java, Spring Boot</List.Item>
                <List.Item><Text as="span" fontWeight="medium">weather-service:</Text> Kotlin</List.Item>
                <List.Item><Text as="span" fontWeight="medium">health-check-service:</Text> Go</List.Item>
              </List.Root>
            </VStack>
          </Card.Body>
        </Card.Root>
      </VStack>
    </Box>
  );
};

function formatBytes(bytes: number) {
  if (!bytes && bytes !== 0) return "-";
  const sizes = ["Bytes", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${(bytes / Math.pow(1024, i)).toFixed(2)} ${sizes[i]}`;
}

export default About;
