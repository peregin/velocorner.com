import { Box, Card, Grid } from "@chakra-ui/react";
import type { AthleteUnits } from "@/types/athlete";
import WordCloud from "@/components/charts/WordCloud";
import BarChart from "@/components/charts/BarChart";
import LineSeriesChart from "@/components/charts/LineSeriesChart";
import CalendarHeatmap from "@/components/charts/CalendarHeatmap";
import HeatmapChart from "@/components/charts/HeatmapChart";
import { dashboardCardProps } from "./shared";
import { useCallback } from "react";
import ApiClient from "@/service/ApiClient";

interface HomeDemoDashboardProps {
  wordCloud: any[];
  units: AthleteUnits;
}

const HomeDemoDashboard = ({
  wordCloud,
  units,
}: HomeDemoDashboardProps) => {
  const fetchDemoYtdDistance = useCallback(() => ApiClient.demoYearlyStatistics("distance", "Ride"), []);
  const fetchDemoYearlyElevation = useCallback(() => ApiClient.demoYearlyStatistics("elevation", "Ride"), []);
  const fetchDemoDailyDistance = useCallback(() => ApiClient.demoDailyStatistics("distance"), []);
  const fetchDemoHistogramDistance = useCallback(() => ApiClient.demoYearlyHistogram("distance", "Ride"), []);
  const fetchDemoHistogramElevation = useCallback(() => ApiClient.demoYearlyHistogram("elevation", "Ride"), []);
  return (
    <>
      <Grid templateColumns={{ base: "1fr", lg: "1fr 1fr 1.2fr" }} alignItems="stretch" gap={4} id="stats">
        <Box>
          <WordCloud words={wordCloud} height={350} />
        </Box>
        <Box>
          <BarChart title="Year To Date Distance (Sample)" unit={units.distanceLabel} fetchSeries={fetchDemoYtdDistance} height={350} />
        </Box>
        <Box>
          <LineSeriesChart title="Yearly Elevation (Sample)" unit={units.elevationLabel} fetchSeries={fetchDemoYearlyElevation} seriesToShow={5} height={350} />
        </Box>
      </Grid>

      <Card.Root {...dashboardCardProps}>
        <Card.Body p={{ base: 4, md: 6 }}>
          <CalendarHeatmap title="Latest Activities (Sample)" fetchDaily={fetchDemoDailyDistance} unitName={units.distanceLabel} maxMonths={8} />
        </Card.Body>
      </Card.Root>

      <Grid templateColumns={{ base: "1fr", lg: "1fr 1fr" }} gap={4}>
        <Box {...dashboardCardProps}>
          <HeatmapChart title="Distance Distribution (Sample)" fetchHeatmap={fetchDemoHistogramDistance} height={250} />
        </Box>
        <Box {...dashboardCardProps}>
          <HeatmapChart title="Elevation Distribution (Sample)" fetchHeatmap={fetchDemoHistogramElevation} height={250} />
        </Box>
      </Grid>
    </>
  );
};

export default HomeDemoDashboard;
