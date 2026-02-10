import { useEffect, useMemo, useState } from 'react';
import Chart from 'react-apexcharts';
import { Box, Heading } from '@chakra-ui/react';

const HeatmapChart = ({ title, fetchHeatmap, height = 250 }) => {
  const [series, setSeries] = useState([]);

  const options = useMemo(() => ({
    chart: { type: 'heatmap', toolbar: { show: false } },
    dataLabels: { enabled: true, style: { colors: ['#888888'] } },
    plotOptions: { heatmap: { radius: 3, enableShades: true, distributed: true, shadeIntensity: 0.9 } },
    grid: { padding: { right: 20 } },
    noData: { text: `Loading ${title} Data...` },
    xaxis: {
      type: 'category',
      labels: {
        show: true,
        rotate: 0,
        rotateAlways: false,
        hideOverlappingLabels: true,
        showDuplicates: false,
        trim: false,
        maxHeight: 120,
        style: {
          fontSize: '8px',
          fontFamily: 'Arial, sans-serif',
        }
      }
    },
  }), [title]);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      const data = await fetchHeatmap();
      if (mounted) setSeries(data);
    };
    load();
    return () => { mounted = false };
  }, [fetchHeatmap]);

  return (
    <Box>
      <Heading size="md" mb={4}>{title}</Heading>
      <Chart options={options} series={series} type="heatmap" height={height} />
    </Box>
  );
};

export default HeatmapChart;


