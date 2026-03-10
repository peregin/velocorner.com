import { useEffect, useState } from "react";
import { Badge, Box, Card, Heading, HStack, List, Spinner, Text, VStack } from "@chakra-ui/react";
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

  return (
    <Card.Root borderRadius="28px" border="1px solid" borderColor="rgba(20, 32, 51, 0.08)" bg="rgba(255,255,255,0.76)" boxShadow="0 24px 60px rgba(18, 38, 63, 0.09)">
      <Card.Body p={{ base: 5, md: 6 }}>
        <Heading size="sm" mb={4}>
          <HStack gap={2}>
            <LuSparkles /> AI Performance Evaluation
          </HStack>
        </Heading>

        {loading ? (
          <HStack gap={2}>
            <Spinner size="sm" />
            <Text>Loading performance summary...</Text>
          </HStack>
        ) : (
          <VStack align="stretch" gap={4} fontSize='sm'>
            {parsedSummary?.trend?.label && (
              <HStack gap={3} align="start">
                <Badge colorPalette={trendColorPalette(parsedSummary.trend.label)} variant="solid" borderRadius="full" px={3} py={1}>
                  {parsedSummary.trend.label}
                </Badge>
                <Text color="slate.600">{parsedSummary.trend.evidence || ""}</Text>
              </HStack>
            )}
            {parsedSummary?.strengths.length ? (
              <Box p={4} borderRadius="2xl" bg="white" border="1px solid" borderColor="rgba(20, 32, 51, 0.06)">
                <VStack align="stretch" gap={1}>
                  <Text fontWeight="semibold" color="slate.700">
                  Strengths
                  </Text>
                  <List.Root ps={4}>
                    {parsedSummary.strengths.map((strength) => (
                      <List.Item key={strength} color="slate.600">
                        {strength}
                      </List.Item>
                    ))}
                  </List.Root>
                </VStack>
              </Box>
            ) : null}
            {parsedSummary?.recommendations.length ? (
              <Box p={4} borderRadius="2xl" bg="rgba(23, 166, 133, 0.06)" border="1px solid" borderColor="rgba(23, 166, 133, 0.12)">
                <VStack align="stretch" gap={1}>
                  <Text fontWeight="semibold" color="slate.700">
                  Recommendations
                  </Text>
                  <List.Root ps={4}>
                    {parsedSummary.recommendations.map((recommendation) => (
                      <List.Item key={recommendation} color="slate.600">
                        {recommendation}
                      </List.Item>
                    ))}
                  </List.Root>
                </VStack>
              </Box>
            ) : null}
            <Text color={summaryText ? "slate.700" : "slate.500"} lineHeight="1.8">
              {summaryText || "Your performance is being evaluated. Please check back in a moment."}
            </Text>
            {data?.basedOn && (
              <Text fontSize="xs" color="slate.500">
                Based on {data.basedOn}
              </Text>
            )}
            {updatedLabel && (
              <Text fontSize="xs" color="slate.500">
                Last generated: {updatedLabel}
              </Text>
            )}
            {data?.evaluating && (
              <Text fontSize="xs" color="orange.600">
                Evaluation is running. Showing the latest available summary.
              </Text>
            )}
          </VStack>
        )}
      </Card.Body>
    </Card.Root>
  );
};

export default PerformanceSummaryWidget;
