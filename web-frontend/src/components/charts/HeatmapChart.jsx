import { useEffect, useMemo, useState } from 'react';
import Chart from 'react-apexcharts';
import { Box, Heading, Text } from '@chakra-ui/react';

const normalizeHeatmapSeries = (payload) => {
  const rawSeries = Array.isArray(payload?.series)
    ? payload.series
    : Array.isArray(payload)
      ? payload
      : [];

  return rawSeries
    .map((entry, index) => {
      const data = Array.isArray(entry?.data)
        ? entry.data
            .map((point) => ({
              x: typeof point?.x === 'string' ? point.x : String(point?.x ?? ''),
              y: Number(point?.y),
            }))
            .filter((point) => point.x && Number.isFinite(point.y))
        : [];

      return {
        name: typeof entry?.name === 'string' && entry.name.trim() ? entry.name : `Series ${index + 1}`,
        data,
      };
    })
    .filter((entry) => entry.data.length > 0);
};

const HeatmapChart = ({ title, fetchHeatmap, height = 250 }) => {
  const [series, setSeries] = useState([]);
  const [loading, setLoading] = useState(true);

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
      setLoading(true);
      try {
        const data = await fetchHeatmap();
        if (mounted) {
          setSeries(normalizeHeatmapSeries(data));
        }
      } catch (error) {
        console.error(`Failed to load heatmap data for ${title}:`, error);
        if (mounted) {
          setSeries([]);
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };
    load();
    return () => { mounted = false };
  }, [fetchHeatmap, title]);

  const hasData = series.length > 0;

  return (
    <Box>
      <Heading size="md" mt='1rem' ml='1rem'>{title}</Heading>
      {loading ? (
        <Text px='1rem' py='1.5rem'>Loading {title} Data...</Text>
      ) : hasData ? (
        <Chart options={options} series={series} type="heatmap" height={height} />
      ) : (
        <Text px='1rem' py='1.5rem'>No data available for {title}.</Text>
      )}
    </Box>
  );
};

export default HeatmapChart;

