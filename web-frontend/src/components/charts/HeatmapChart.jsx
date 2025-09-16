import { useEffect, useMemo, useState } from 'react';
import Chart from 'react-apexcharts';
import { Card, Heading } from '@chakra-ui/react';

const HeatmapChart = ({ title, fetchHeatmap, height = 250 }) => {
  const [series, setSeries] = useState([]);

  const options = useMemo(() => ({
    chart: { type: 'heatmap', toolbar: { show: false } },
    dataLabels: { enabled: true, style: { colors: ['#888888'] } },
    plotOptions: { heatmap: { radius: 3, enableShades: true, distributed: true, shadeIntensity: 0.9 } },
    grid: { padding: { right: 20 } },
    noData: { text: `Loading ${title} Data...` },
    xaxis: { type: 'category' },
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
    <Card.Root>
      <Card.Body>
        <Heading size="md" mb={4}>{title}</Heading>
        <Chart options={options} series={series} type="heatmap" height={height} />
      </Card.Body>
    </Card.Root>
  );
};

export default HeatmapChart;


