import { useEffect, useMemo, useRef, useState } from 'react';
import Highcharts from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import 'highcharts/modules/heatmap';
import 'highcharts/modules/accessibility';
import { Box, Button, Heading, Text, VisuallyHidden } from '@chakra-ui/react';

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
              y: point?.y == null || point.y === '' ? NaN : Number(point.y),
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

const HeatmapChart = ({ title, fetchHeatmap, height = 250, palette = 'orange' }) => {
  const [{ status, series }, setResult] = useState({ status: 'loading', series: [] });
  const [attempt, setAttempt] = useState(0);
  const chartGroup = useRef(null);

  const options = useMemo(() => {
    const buckets = [...new Set(series.flatMap(({ data }) => data.map(({ x }) => x)))];
    return {
      chart: { type: 'heatmap', height: height + series.length * 10 },
      title: { text: title, align: 'left', style: { fontSize: '1rem' } },
      credits: { enabled: false },
      accessibility: {
        description: 'Activity counts by year and range.',
        series: { pointDescriptionEnabledThreshold: false },
        point: { valueDescriptionFormat: '{point.name}, {point.value} activities.' },
      },
      xAxis: { categories: buckets },
      yAxis: { categories: series.map(({ name }) => name), reversed: true, title: { text: null } },
      colorAxis: {
        min: 0,
        stops: palette === 'green'
          ? [[0, '#f0fdf4'], [0.5, '#86efac'], [1, '#166534']]
          : [[0, '#fff7ed'], [0.5, '#fdba74'], [1, '#9a3412']],
      },
      tooltip: { headerFormat: '', pointFormat: '{point.name}<br/><b>{point.value}</b> activities' },
      series: [{
        name: title,
        borderWidth: 1,
        borderColor: '#ffffff',
        dataLabels: { enabled: true },
        data: series.flatMap(({ name, data }, row) => data.map(({ x, y }) => ({
          x: buckets.indexOf(x),
          y: row,
          value: y,
          name: `${name}, ${x}`,
        }))),
      }],
    };
  }, [height, palette, series, title]);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      setResult({ status: 'loading', series: [] });
      try {
        const data = await fetchHeatmap();
        if (mounted) {
          const nextSeries = normalizeHeatmapSeries(data);
          setResult({ status: nextSeries.length ? 'ready' : 'empty', series: nextSeries });
        }
      } catch (error) {
        if (mounted) {
          console.error(`Failed to load heatmap data for ${title}:`, error);
          setResult({ status: 'error', series: [] });
        }
      }
    };
    load();
    return () => { mounted = false };
  }, [attempt, fetchHeatmap, title]);

  const statusMessage = {
    loading: `Loading chart data for ${title}.`,
    empty: 'No activities to chart for this selection.',
    error: `Could not load chart data for ${title}.`,
  }[status];

  return (
    <Box ref={chartGroup} role="group" aria-label={title} tabIndex={-1} minH={`${height}px`}>
      {status !== 'ready' && <Heading size="md" pt={4} px={4}>{title}</Heading>}
      <Box role="status">
        {status === 'ready'
          ? <VisuallyHidden>Chart data loaded for {title}.</VisuallyHidden>
          : <Text px={4} pt={4}>{statusMessage}</Text>}
      </Box>
      {status === 'error' && (
        <Button
          size="sm"
          mx={4}
          mt={3}
          aria-label={`Retry loading ${title}`}
          onClick={() => {
            chartGroup.current.focus();
            setAttempt(value => value + 1);
          }}
        >
          Retry
        </Button>
      )}
      {status === 'ready' && (
        <HighchartsReact highcharts={Highcharts} options={options} />
      )}
    </Box>
  );
};

export default HeatmapChart;

