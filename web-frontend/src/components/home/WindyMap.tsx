import { Box } from "@chakra-ui/react";

const WindyMap = () => {
  return (
    <Box py="1rem">
      <iframe
        width="100%"
        height="400"
        src="https://embed.windy.com/embed.html?type=map&location=coordinates&metricRain=mm&metricTemp=%C2%B0C&metricWind=km/h&zoom=5&overlay=wind&product=ecmwf&level=surface&lat=47.31&lon=8.527&detailLat=47.31&detailLon=8.527000000000044&marker=true"
        title="Windy Map"
        style={{ border: "none", borderRadius: "28px", boxShadow: "0 24px 60px rgba(18, 38, 63, 0.09)" }}
      />
    </Box>
  );
};

export default WindyMap;
