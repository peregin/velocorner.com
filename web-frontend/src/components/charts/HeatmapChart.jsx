import { useEffect, useMemo, useState } from 'react';
import Highcharts from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import 'highcharts/modules/heatmap';
import 'highcharts/modules/accessibility';
import { Box, Heading, Table, Text } from '@chakra-ui/react';

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
  const [series, setSeries] = useState([]);
  const [loading, setLoading] = useState(true);

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
      {(loading || !hasData) && <Heading size="md" mt="1rem" ml="1rem">{title}</Heading>}
      {loading ? (
        <Text px='1rem' py='1.5rem'>Loading {title} Data...</Text>
      ) : hasData ? (
        <>
          <HighchartsReact highcharts={Highcharts} options={options} />
          <Box as="details" px={4} pb={4}>
            <Box as="summary" aria-label={`View data for ${title}`} cursor="pointer" width="fit-content" py={2}>
              View data
            </Box>
            <Table.ScrollArea>
              <Table.Root size="sm">
                <Table.Caption captionSide="top" textAlign="start">{title} (activity counts)</Table.Caption>
                <Table.Header>
                  <Table.Row>
                    <Table.ColumnHeader scope="col">Year</Table.ColumnHeader>
                    {options.xAxis.categories.map(bucket => (
                      <Table.ColumnHeader key={bucket} scope="col">{bucket}</Table.ColumnHeader>
                    ))}
                  </Table.Row>
                </Table.Header>
                <Table.Body>
                  {series.map(({ name, data }) => {
                    const counts = new Map(data.map(({ x, y }) => [x, y]));
                    return (
                      <Table.Row key={name}>
                        <Box as="th" scope="row" px={3} py={2} textAlign="start">{name}</Box>
                        {options.xAxis.categories.map(bucket => (
                          <Table.Cell key={bucket}>{counts.get(bucket) ?? 'No data'}</Table.Cell>
                        ))}
                      </Table.Row>
                    );
                  })}
                </Table.Body>
              </Table.Root>
            </Table.ScrollArea>
          </Box>
        </>
      ) : (
        <Text px='1rem' py='1.5rem'>No data available for {title}.</Text>
      )}
    </Box>
  );
};

export default HeatmapChart;

