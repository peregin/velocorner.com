import { useMemo } from 'react';
import Highcharts from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import 'highcharts/modules/heatmap';
import 'highcharts/modules/accessibility';
import ChartRequest, { useChartData } from './ChartRequest';

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
  const { status, data: series, retry } = useChartData(fetchHeatmap, normalizeHeatmapSeries);

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

  return (
    <ChartRequest title={title} height={height} status={status} retry={retry}>
      <HighchartsReact highcharts={Highcharts} options={options} />
    </ChartRequest>
  );
};

export default HeatmapChart;
