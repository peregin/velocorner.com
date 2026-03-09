import { useEffect, useState } from "react";
import { Card, Heading, HStack, Spinner, Text, VStack } from "@chakra-ui/react";
import { LuSparkles } from "react-icons/lu";
import ApiClient from "@/service/ApiClient";
import type { AthletePerformanceSummary } from "@/types/athlete";

interface PerformanceSummaryWidgetProps {
  isAuthenticated: boolean;
}

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

  const summaryText = data?.summary?.trim();
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
          <VStack align="stretch" gap={2}>
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
