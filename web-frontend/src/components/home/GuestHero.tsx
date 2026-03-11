import { Badge, Button, Card, Grid, Heading, HStack, Image, Text, VStack } from "@chakra-ui/react";
import type { AthleteUnits } from "@/types/athlete";
import QuickStats from "@/components/QuickStats";
import MarketingWidget from "@/components/MarketingWidget";
import { heroCardProps } from "./shared";
import strava from "super-tiny-icons/images/svg/strava.svg";

interface HomeGuestHeroProps {
  authLoading: boolean;
  onConnect: () => void;
  demoUnits: AthleteUnits;
}

const GuestHero = ({ authLoading, onConnect, demoUnits }: HomeGuestHeroProps) => {
  return (
    <Card.Root {...heroCardProps}>
      <Card.Body p={{ base: 6, md: 8 }}>
        <Grid templateColumns={{ base: "1fr", lg: "1.35fr 0.9fr" }} gap={{ base: 8, lg: 10 }} alignItems="center">
          <VStack align="start" gap={5}>
            <Badge colorPalette="green" borderRadius="full" px={3} py={1}>
              Modern endurance analytics
            </Badge>
            <VStack align="start" gap={3}>
              <Heading size={{ base: "2xl", md: "3xl" }} lineHeight="1.05" letterSpacing="-0.03em" maxW="15ch" color="black">
                Train with a sharper view of your season.
              </Heading>
              <Text color="slate.600" fontSize={{ base: "md", md: "lg" }} maxW="2xl" lineHeight="1.8">
                Velocorner turns Strava activity history into a concise performance cockpit with year-over-year comparisons,
                standout efforts, and search that gets you back to the ride you want in seconds.
              </Text>
            </VStack>
            <HStack gap={3} flexWrap="wrap" width="100%" align="stretch" flexDirection={{ base: "column", md: "row" }}>
              <Button
                colorPalette="orange"
                size="lg"
                borderRadius="full"
                fontWeight="700"
                px={6}
                width={{ base: "100%", md: "auto" }}
                boxShadow="0 14px 28px rgba(252, 121, 52, 0.24)"
                onClick={onConnect}
                loading={authLoading}
              >
                <Image src={strava} boxSize="20px" mr={2} />
                Connect with Strava
              </Button>
              <Button
                asChild
                variant="outline"
                size="lg"
                borderRadius="full"
                px={6}
                width={{ base: "100%", md: "auto" }}
                color="slate.900"
                borderColor="blackAlpha.300"
                _hover={{ bg: "whiteAlpha.900", color: "slate.900" }}
              >
                <a href="#stats">Explore dashboard</a>
              </Button>
            </HStack>

            <QuickStats selectedActivityType="Ride" units={demoUnits} demo />
          </VStack>

          <MarketingWidget />
        </Grid>
      </Card.Body>
    </Card.Root>
  );
};

export default GuestHero;
