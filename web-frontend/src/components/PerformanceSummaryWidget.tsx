import { useEffect, useState } from "react";
import { Badge, Card, Heading, HStack, List, Spinner, Text, VStack } from "@chakra-ui/react";
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
    <Card.Root>
      <Card.Body p={{ base: 3, md: 4 }}>
        <Heading size="sm" mb={3}>
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
          <VStack align="stretch" gap={2} fontSize='sm'>
            {parsedSummary?.trend?.label && (
              <HStack gap={2} align="start">
                <Badge colorPalette={trendColorPalette(parsedSummary.trend.label)} variant="subtle">
                  {parsedSummary.trend.label}
                </Badge>
                <Text color="gray.700">{parsedSummary.trend.evidence || ""}</Text>
              </HStack>
            )}
            {parsedSummary?.strengths.length ? (
              <VStack align="stretch" gap={1}>
                <Text fontWeight="semibold" color="gray.700">
                  Strengths
                </Text>
                <List.Root ps={4}>
                  {parsedSummary.strengths.map((strength) => (
                    <List.Item key={strength} color="gray.700">
                      {strength}
                    </List.Item>
                  ))}
                </List.Root>
              </VStack>
            ) : null}
            {parsedSummary?.recommendations.length ? (
              <VStack align="stretch" gap={1}>
                <Text fontWeight="semibold" color="gray.700">
                  Recommendations
                </Text>
                <List.Root ps={4}>
                  {parsedSummary.recommendations.map((recommendation) => (
                    <List.Item key={recommendation} color="gray.700">
                      {recommendation}
                    </List.Item>
                  ))}
                </List.Root>
              </VStack>
            ) : null}
            <Text color={summaryText ? "gray.700" : "gray.500"}>
              {summaryText || "Your performance is being evaluated. Please check back in a moment."}
            </Text>
            {data?.basedOn && (
              <Text fontSize="xs" color="gray.500">
                Based on {data.basedOn}
              </Text>
            )}
            {updatedLabel && (
              <Text fontSize="xs" color="gray.500">
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
