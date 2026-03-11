import { useEffect, useState } from "react";
import { Badge, Box, Card, Grid, Heading, HStack, Spinner, Text, VStack } from "@chakra-ui/react";
import { LuSparkles } from "react-icons/lu";
import ApiClient from "@/service/ApiClient";
import type { AthletePerformanceSummary } from "@/types/athlete";

interface PerformanceSummaryWidgetProps {
  isAuthenticated: boolean;
}

type ParsedPerformanceSummary = {
  trend?: {
    label?: string;
    evidence?: string;
  };
  strengths: string[];
  recommendations: string[];
  message?: string;
  fallbackText?: string;
};

const normalizeSummaryText = (summary?: string): string | undefined => {
  const trimmed = summary?.trim();
  if (!trimmed) return undefined;

  const lines = trimmed.split("\n");
  const firstLine = lines[0]?.trim().toLowerCase();
  const lastLine = lines[lines.length - 1]?.trim();

  if (firstLine === "```json" && lastLine === "```") {
    return lines.slice(1, -1).join("\n").trim();
  }

  return trimmed;
};

const parsePerformanceSummary = (summary?: string): ParsedPerformanceSummary | null => {
  const trimmed = normalizeSummaryText(summary);
  if (!trimmed) return null;

  try {
    const parsed = JSON.parse(trimmed) as {
      trend?: { label?: unknown; evidence?: unknown };
      strengths?: unknown;
      recommendations?: unknown;
      message?: unknown;
    };

    const asStringList = (value: unknown): string[] =>
      Array.isArray(value)
        ? value.filter((entry): entry is string => typeof entry === "string" && entry.trim().length > 0).map(entry => entry.trim())
        : [];

    return {
      trend: parsed.trend && (typeof parsed.trend.label === "string" || typeof parsed.trend.evidence === "string")
        ? {
            label: typeof parsed.trend.label === "string" ? parsed.trend.label.trim() : undefined,
            evidence: typeof parsed.trend.evidence === "string" ? parsed.trend.evidence.trim() : undefined
          }
        : undefined,
      strengths: asStringList(parsed.strengths),
      recommendations: asStringList(parsed.recommendations),
      message: typeof parsed.message === "string" && parsed.message.trim() ? parsed.message.trim() : undefined,
      fallbackText: trimmed
    };
  } catch {
    return {
      strengths: [],
      recommendations: [],
      message: trimmed,
      fallbackText: trimmed
    };
  }
};

const trendColorPalette = (label?: string) => {
  switch ((label || "").toLowerCase()) {
    case "improving":
      return "green";
    case "stable":
      return "blue";
    case "declining":
      return "red";
    case "inconclusive":
      return "orange";
    default:
      return "gray";
  }
};

const trendAccent = (label?: string) => {
  switch ((label || "").toLowerCase()) {
    case "improving":
      return {
        bg: "linear-gradient(135deg, rgba(34,197,94,0.16), rgba(16,185,129,0.08))",
        borderColor: "rgba(34,197,94,0.18)",
        textColor: "green.700"
      };
    case "stable":
      return {
        bg: "linear-gradient(135deg, rgba(59,130,246,0.14), rgba(14,165,233,0.06))",
        borderColor: "rgba(59,130,246,0.18)",
        textColor: "blue.700"
      };
    case "declining":
      return {
        bg: "linear-gradient(135deg, rgba(239,68,68,0.14), rgba(249,115,22,0.06))",
        borderColor: "rgba(239,68,68,0.18)",
        textColor: "red.700"
      };
    case "inconclusive":
      return {
        bg: "linear-gradient(135deg, rgba(245,158,11,0.14), rgba(251,191,36,0.06))",
        borderColor: "rgba(245,158,11,0.18)",
        textColor: "orange.700"
      };
    default:
      return {
        bg: "linear-gradient(135deg, rgba(148,163,184,0.12), rgba(226,232,240,0.08))",
        borderColor: "rgba(148,163,184,0.18)",
        textColor: "gray.700"
      };
  }
};

const conciseItems = (items: string[], limit = 3) => items.slice(0, limit);

const PerformanceSummaryWidget = ({ isAuthenticated }: PerformanceSummaryWidgetProps) => {
  const [data, setData] = useState<AthletePerformanceSummary | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!isAuthenticated) {
      setData(null);
      setLoading(false);
      return;
    }

    let isActive = true;
    setLoading(true);
    ApiClient.performanceSummary()
      .then((resp) => {
        if (isActive) {
          setData(resp || null);
        }
      })
      .catch((error) => {
        console.error("Error fetching performance summary:", error);
        if (isActive) setData(null);
      })
      .finally(() => {
        if (isActive) setLoading(false);
      });

    return () => {
      isActive = false;
    };
  }, [isAuthenticated]);

  const parsedSummary = parsePerformanceSummary(data?.summary);
  const summaryText = parsedSummary?.message || parsedSummary?.fallbackText;
  const updatedLabel = data?.createdAt ? new Date(data.createdAt).toLocaleString() : null;
  const trendStyle = trendAccent(parsedSummary?.trend?.label);
  const strengths = conciseItems(parsedSummary?.strengths || []);
  const recommendations = conciseItems(parsedSummary?.recommendations || []);

  return (
    <Card.Root
      borderRadius="28px"
      border="1px solid"
      borderColor="rgba(15, 23, 42, 0.08)"
      bg="linear-gradient(180deg, rgba(255,255,255,0.94), rgba(248,250,252,0.88))"
      boxShadow="0 24px 60px rgba(15, 23, 42, 0.08)"
      overflow="hidden"
    >
      <Card.Body p={{ base: 5, md: 6 }}>
        <VStack align="stretch" gap={4}>
          <HStack justify="space-between" align="start" gap={4}>
            <VStack align="stretch" gap={1}>
              <HStack gap={2} color="slate.800">
                <Box
                  p={2}
                  borderRadius="xl"
                  bg="rgba(59, 130, 246, 0.08)"
                  color="blue.600"
                >
                  <LuSparkles />
                </Box>
                <Heading size="sm">Performance Pulse</Heading>
              </HStack>
              <Text fontSize="sm" color="slate.500">
                Quick read on momentum, strengths and next gains.
              </Text>
            </VStack>
            {data?.evaluating ? (
              <Badge colorPalette="orange" variant="subtle" borderRadius="full" px={3} py={1}>
                Refreshing
              </Badge>
            ) : null}
          </HStack>

          {loading ? (
            <HStack gap={2}>
              <Spinner size="sm" />
              <Text fontSize="sm" color="slate.600">Loading pulse...</Text>
            </HStack>
          ) : (
            <VStack align="stretch" gap={4} fontSize="sm">
              {parsedSummary?.trend?.label && (
                <Box
                  p={4}
                  borderRadius="2xl"
                  bg={trendStyle.bg}
                  border="1px solid"
                  borderColor={trendStyle.borderColor}
                >
                  <VStack align="stretch" gap={2}>
                    <HStack gap={3} flexWrap="wrap">
                      <Badge
                        colorPalette={trendColorPalette(parsedSummary.trend.label)}
                        variant="solid"
                        borderRadius="full"
                        px={3}
                        py={1}
                      >
                        {parsedSummary.trend.label}
                      </Badge>
                      <Text fontSize="xs" fontWeight="semibold" color={trendStyle.textColor} textTransform="uppercase" letterSpacing="0.08em">
                        Current signal
                      </Text>
                    </HStack>
                    {parsedSummary.trend.evidence ? (
                      <Text color="slate.700" lineHeight="1.6">
                        {parsedSummary.trend.evidence}
                      </Text>
                    ) : null}
                  </VStack>
                </Box>
              )}

              {(strengths.length > 0 || recommendations.length > 0) && (
                <Grid templateColumns={{ base: "1fr", md: "1fr 1fr" }} gap={3}>
                  {strengths.length > 0 ? (
                    <Box p={4} borderRadius="2xl" bg="white" border="1px solid" borderColor="rgba(15, 23, 42, 0.07)">
                      <VStack align="stretch" gap={3}>
                        <Text fontSize="xs" fontWeight="bold" color="slate.500" textTransform="uppercase" letterSpacing="0.08em">
                          Working well
                        </Text>
                        <VStack align="stretch" gap={2}>
                          {strengths.map((strength) => (
                            <Box key={strength} px={3} py={2.5} borderRadius="xl" bg="rgba(15, 23, 42, 0.03)">
                              <Text color="slate.700">{strength}</Text>
                            </Box>
                          ))}
                        </VStack>
                      </VStack>
                    </Box>
                  ) : null}
                  {recommendations.length > 0 ? (
                    <Box
                      p={4}
                      borderRadius="2xl"
                      bg="linear-gradient(180deg, rgba(236, 253, 245, 0.9), rgba(240, 253, 250, 0.72))"
                      border="1px solid"
                      borderColor="rgba(16, 185, 129, 0.14)"
                    >
                      <VStack align="stretch" gap={3}>
                        <Text fontSize="xs" fontWeight="bold" color="green.700" textTransform="uppercase" letterSpacing="0.08em">
                          Next edge
                        </Text>
                        <VStack align="stretch" gap={2}>
                          {recommendations.map((recommendation) => (
                            <Box key={recommendation} px={3} py={2.5} borderRadius="xl" bg="rgba(255,255,255,0.7)">
                              <Text color="slate.700">{recommendation}</Text>
                            </Box>
                          ))}
                        </VStack>
                      </VStack>
                    </Box>
                  ) : null}
                </Grid>
              )}

              <Box px={4} py={3.5} borderRadius="2xl" bg="rgba(15, 23, 42, 0.03)">
                <Text color={summaryText ? "slate.700" : "slate.500"} lineHeight="1.7">
                  {summaryText || "Your latest activities are still being analyzed."}
                </Text>
              </Box>

              {data?.basedOn || updatedLabel ? (
                <Grid templateColumns={{ base: "1fr", md: "1fr auto" }} gap={2}>
                  <Text fontSize="xs" color="slate.500">
                    {data?.basedOn ? `Based on ${data.basedOn}` : ""}
                  </Text>
                  <Text fontSize="xs" color="slate.500" textAlign={{ base: "left", md: "right" }}>
                    {updatedLabel ? `Updated ${updatedLabel}` : ""}
                  </Text>
                </Grid>
              ) : null}

              {data?.evaluating && (
                <Text fontSize="xs" color="orange.700">
                  A fresh summary is running. Latest available snapshot shown for now.
                </Text>
              )}
            </VStack>
          )}
        </VStack>
      </Card.Body>
    </Card.Root>
  );
};

export default PerformanceSummaryWidget;
