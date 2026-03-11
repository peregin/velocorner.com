import { Badge, Box, HStack, Image, Separator, Text, VStack } from "@chakra-ui/react";

const marketingLines = [
  "See yearly distance, elevation, achievements, year to date progress, heatmaps and much more!",
  "Spot trends, top rides, and personal bests in one place.",
  "Keep the interface focused on decisions, not chart clutter.",
];

export default function MarketingWidget() {
  return (
    <Box
      display={{ base: "none", lg: "block" }}
      p={{ base: 5, md: 6 }}
      borderRadius="28px"
      bg="linear-gradient(180deg, #0f172a 0%, #16263f 100%)"
      color="white"
      boxShadow="inset 0 1px 0 rgba(255,255,255,0.08)"
    >
      <VStack align="stretch" gap={4}>
        <HStack justify="space-between">
          <Text textTransform="uppercase" letterSpacing="0.16em" fontSize="xs" color="whiteAlpha.700">
            What you get
          </Text>
          <Badge colorPalette="green" borderRadius="full">Live data</Badge>
        </HStack>
        <VStack align="stretch" gap={3}>
          {marketingLines.map((line) => (
            <Box key={line} p={4} borderRadius="2xl" bg="rgba(255,255,255,0.06)" border="1px solid" borderColor="whiteAlpha.100">
              <Text color="whiteAlpha.900" lineHeight="1.7">
                {line}
              </Text>
            </Box>
          ))}
        </VStack>
        <Separator borderColor="whiteAlpha.200" />
        <Image
          alignSelf="flex-end"
          width="169px"
          height="31px"
          src="/images/powered-by-strava1.png"
          alt="Powered by Strava"
        />
      </VStack>
    </Box>
  );
}
