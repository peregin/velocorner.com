import { Box, Card, Grid, Image, VStack } from "@chakra-ui/react";
import ActivityStatsWidget from "@/components/ActivityStatsWidget";
import AchievementsWidget from "@/components/AchievementsWidget";
import PerformanceSummaryWidget from "@/components/PerformanceSummaryWidget";
import LineSeriesChart from "@/components/charts/LineSeriesChart";
import BarChart from "@/components/charts/BarChart";
import HeatmapChart from "@/components/charts/HeatmapChart";
import CalendarHeatmap from "@/components/charts/CalendarHeatmap";
import TopActivitiesWidget from "@/components/TopActivitiesWidget";
import type { AthleteProfile, AthleteUnits } from "@/types/athlete";
import { dashboardCardProps } from "./shared";
import { useCallback } from "react";
import ApiClient from "@/service/ApiClient";

interface HomeAnalyticsSectionProps {
  selectedActivityType: string;
  athleteProfile: AthleteProfile | null;
  units: AthleteUnits;
}

const AnalyticsSection = ({
  selectedActivityType,
  athleteProfile,
  units,
}: HomeAnalyticsSectionProps) => {
  const fetchYearlyHeatmap = useCallback(() => ApiClient.yearlyHeatmap(selectedActivityType), [selectedActivityType]);
  const fetchYearlyDistance = useCallback(() => ApiClient.yearlyStatistics("distance", selectedActivityType), [selectedActivityType]);
  const fetchYearlyElevation = useCallback(() => ApiClient.yearlyStatistics("elevation", selectedActivityType), [selectedActivityType]);
  const fetchYearlyTime = useCallback(() => ApiClient.yearlyStatistics("time", selectedActivityType), [selectedActivityType]);
  const fetchYtdDistance = useCallback(() => ApiClient.ytdStatistics("distance", selectedActivityType), [selectedActivityType]);
  const fetchYtdElevation = useCallback(() => ApiClient.ytdStatistics("elevation", selectedActivityType), [selectedActivityType]);
  const fetchYtdTime = useCallback(() => ApiClient.ytdStatistics("time", selectedActivityType), [selectedActivityType]);
  const fetchHistogramDistance = useCallback(() => ApiClient.yearlyHistogram("distance", selectedActivityType), [selectedActivityType]);
  const fetchHistogramElevation = useCallback(() => ApiClient.yearlyHistogram("elevation", selectedActivityType), [selectedActivityType]);
  const fetchDailyDistance = useCallback(() => ApiClient.dailyStatistics("distance"), []);
  return (
    <>
      <Image alignSelf="flex-end" width="169px" height="31px" src="/images/powered-by-strava1.png" alt="Powered by Strava" />

      <VStack gap={6} align="stretch" id="stats">
        <Grid templateColumns={{ base: "1fr", xl: "1.5fr 1fr" }} gap={4} alignItems="stretch" hideBelow="md">
          <Box h="100%">
            <ActivityStatsWidget
              selectedActivityType={selectedActivityType}
              isAuthenticated
              athleteProfile={athleteProfile}
            />
          </Box>

          <Box h="100%">
            <AchievementsWidget
              selectedActivityType={selectedActivityType}
              athleteProfile={athleteProfile}
              isAuthenticated
            />
          </Box>
        </Grid>

        <PerformanceSummaryWidget isAuthenticated />

        <LineSeriesChart title="Yearly Heatmap (Distance)" unit={units.distanceLabel} fetchSeries={fetchYearlyHeatmap} seriesToShow={2} height={400} />

        <Grid templateColumns={{ base: "1fr", xl: "0.8fr 1.4fr" }} gap={4}>
          <Box>
            <BarChart title="Year To Date Distance" unit={units.distanceLabel} fetchSeries={fetchYtdDistance} height={350} />
          </Box>
          <Box>
            <LineSeriesChart title="Yearly Distance" unit={units.distanceLabel} fetchSeries={fetchYearlyDistance} seriesToShow={4} height={350} />
          </Box>
        </Grid>

        <Grid templateColumns={{ base: "1fr", xl: "0.8fr 1.4fr" }} gap={4}>
          <Box>
            <BarChart title="Year To Date Elevation" unit={units.elevationLabel} fetchSeries={fetchYtdElevation} height={350} />
          </Box>
          <Box>
            <LineSeriesChart title="Yearly Elevation" unit={units.elevationLabel} fetchSeries={fetchYearlyElevation} seriesToShow={4} height={350} />
          </Box>
        </Grid>

        <Grid templateColumns={{ base: "1fr", xl: "0.8fr 1.4fr" }} gap={4}>
          <Box>
            <BarChart title="Year To Date Time" unit="h" fetchSeries={fetchYtdTime} height={350} />
          </Box>
          <Box>
            <LineSeriesChart title="Yearly Time" unit="h" fetchSeries={fetchYearlyTime} seriesToShow={4} height={350} />
          </Box>
        </Grid>

        <Grid templateColumns={{ base: "1fr", xl: "1fr 1fr" }} gap={4}>
          <Box {...dashboardCardProps}>
            <HeatmapChart title="Activity Distribution for Distance" fetchHeatmap={fetchHistogramDistance} height={250} />
          </Box>
          <Box {...dashboardCardProps}>
            <HeatmapChart title="Activity Distribution for Elevation" fetchHeatmap={fetchHistogramElevation} height={250} />
          </Box>
        </Grid>

        <Grid templateColumns={{ base: "1fr", xl: "1fr 1fr" }} gap={4} hideBelow="md">
          <Box>
            <TopActivitiesWidget
              title="Longest Activities"
              action="distance"
              selectedActivityType={selectedActivityType}
              athleteProfile={athleteProfile}
            />
          </Box>
          <Box>
            <TopActivitiesWidget
              title="Most Elevation"
              action="elevation"
              selectedActivityType={selectedActivityType}
              athleteProfile={athleteProfile}
            />
          </Box>
        </Grid>

        <Card.Root {...dashboardCardProps}>
          <Card.Body p={{ base: 4, md: 6 }}>
            <CalendarHeatmap title="All Activities" fetchDaily={fetchDailyDistance} unitName={units.distanceLabel} maxMonths={8} />
          </Card.Body>
        </Card.Root>
      </VStack>
    </>
  );
};

export default AnalyticsSection;
