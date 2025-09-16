import { useEffect, useMemo, useState } from 'react';
import Chart from 'react-apexcharts';
import { Box, Heading } from '@chakra-ui/react';

const LineSeriesChart = ({ title, unit, fetchSeries, seriesToShow = 2, height = 400 }) => {
  const [series, setSeries] = useState([]);
  const [loading, setLoading] = useState(true);

  const options = useMemo(() => ({
    chart: { type: 'line', toolbar: { show: false }, zoom: { enabled: true } },
    stroke: { curve: 'smooth' },
    dataLabels: { enabled: false },
    xaxis: { type: 'datetime' },
    yaxis: { labels: { formatter: (v) => `${Math.round(v)} ${unit}` } },
    tooltip: { x: { format: 'dd MMM' }, y: { formatter: (v) => `${Math.round(v)} ${unit}` } },
    legend: { position: 'top' }
  }), [unit]);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      setLoading(true);
      try {
        const resp = await fetchSeries();
        // yearly endpoints return { status, series }, demo or other may return same
        const seriesList = resp.series || resp;
        const mapped = seriesList.map(s => ({
          name: s.name,
          data: (s.series || []).map(p => {
            const d = p.day; // yyyy-MM-dd
            return [new Date(d).getTime(), p.value];
          })
        }));
        // show last N series and hide older by ordering
        const visible = mapped.slice(-seriesToShow);
        const hidden = mapped.slice(0, Math.max(0, mapped.length - seriesToShow)).map(s => ({ ...s, opacity: 0.2 }));
        const finalSeries = [...hidden, ...visible];
        if (mounted) setSeries(finalSeries);
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => { mounted = false };
  }, [fetchSeries, seriesToShow]);

  return (
    <Box>
      <Heading size="md" mb={4}>{title}</Heading>
      <Chart options={options} series={series} type="line" height={height} />
    </Box>
  );
};

export default LineSeriesChart;


