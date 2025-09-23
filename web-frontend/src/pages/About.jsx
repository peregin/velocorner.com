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
} from "@chakra-ui/react";

const About = () => {
  const [status, setStatus] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        setLoading(true);
        const res = await ApiClient.status();
        setStatus(res);
      } catch (e) {
        setError(e);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  return (
    <Box maxW="1200px" mx="auto" p={6}>
      <VStack align="stretch" spacing={8}>
        <Box>
          <Heading size="lg" mb={2}>Welcome to the cycling site!</Heading>
          <Text color="gray.700">
            The site provides metrics for cycling and running activities. Compare yearly aggregated data like distance, hours, elevation,
            and view year-to-date series. It integrates with Strava and Garmin Connect to track activities and helps you find the best
            component prices via our product aggregator.
          </Text>
        </Box>

        {error && (
          <Alert.Root status="error">
            <Alert.Indicator />
            <Alert.Title>Failed to load status information.</Alert.Title>
          </Alert.Root>
        )}

        <Card.Root>
          <Card.Body>
            <VStack align="stretch" spacing={4}>
              <Heading size="md">Build & Runtime</Heading>
              {loading ? (
                <HStack>
                  <Text>Loading status…</Text>
                  <Progress.Root size="lg" value={null}>
                    <Progress.Track>
                      <Progress.Range />
                    </Progress.Track>
                  </Progress.Root>
                  {/* <Progress isIndeterminate w="200px" /> */}
                </HStack>
              ) : (
                status && (
                  <VStack align="stretch" spacing={4}>
                    <Grid templateColumns="repeat(auto-fit, minmax(220px, 1fr))" gap={4}>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Version</Text>
                        <Badge colorPalette="blue">{status.appVersion}</Badge>
                      </GridItem>
                      <GridItem>
                        <Text fontSize="sm" color="gray.600">Build Time</Text>
                        <Badge colorPalette="purple">{status.buildTime}</Badge>
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
                      <Text color="gray.600">Total: {formatBytes(status.memoryTotal)}</Text>
                    </HStack>
                    <Progress.Root value={status.memoryUsedPercentile} defaultValue={90}>
                      <Progress.Label>Memory usage</Progress.Label>
                      <Progress.Track flex='1'>
                        <Progress.Range />
                      </Progress.Track>
                      <Progress.ValueText>{status.memoryUsedPercentile}%</Progress.ValueText>
                    </Progress.Root>
                    {/* <Progress value={status.memoryUsedPercentile} size="sm" /> */}
                  </VStack>
                )
              )}
            </VStack>
          </Card.Body>
        </Card.Root>

        <Card.Root>
          <Card.Body>
            <Heading size="md" mb={2}>API</Heading>
            <Text>
              Use the <Link href="/docs" color="blue.500">Velocorner API</Link> to access your activity statistics. The API follows OpenAPI specifications and
              provides Swagger compatibility.
            </Text>
          </Card.Body>
        </Card.Root>
      </VStack>
    </Box>
  );
};

function formatBytes(bytes) {
  if (!bytes && bytes !== 0) return "-";
  const sizes = ["Bytes", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${(bytes / Math.pow(1024, i)).toFixed(2)} ${sizes[i]}`;
}

export default About;
