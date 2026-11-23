import { useMemo } from 'react';
import Highcharts from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import 'highcharts/modules/accessibility';
import ChartRequest, { useChartData } from './ChartRequest';

const toLines = response => (response?.series ?? response ?? []).map(item => ({
  name: item.name,
  data: (item.series ?? []).flatMap(({ day, value }) => {
    // Align series on the same leap year so February 29 keeps its position.
    const x = Date.parse('2016-' + day?.slice(5));
    return Number.isFinite(x) && Number.isFinite(value) ? [[x, value]] : [];
  }),
})).filter(item => item.data.length);

const LineSeriesChart = ({ title, unit, fetchSeries, seriesToShow = 2, height = 400 }) => {
  const { status, data, retry } = useChartData(fetchSeries, toLines);

  const options = useMemo(() => ({
    chart: {
      type: 'spline',
      zoomType: 'x',
      borderRadius: 20,
      height: height
    },
    accessibility: {
      series: { pointDescriptionEnabledThreshold: false },
      point: { dateFormat: '%e %B', valueSuffix: unit ? ' ' + unit : '', valueDecimals: 0 }
    },
    title: {
      text: title
    },
    subtitle: {
      text: ''
    },
    exporting: {
      enabled: false
    },
    credits: {
      enabled: false
    },
    xAxis: {
      type: 'datetime',
      dateTimeLabelFormats: {
        month: '%e. %b',
        year: '%b'
      },
      title: {
        text: 'Date'
      }
    },
    yAxis: {
      title: {
        text: title
      },
      min: 0
    },
    tooltip: {
      headerFormat: '<b>{series.name}</b><br>',
      pointFormat: '{point.x:%e. %b}: {point.y:.0f} ' + unit
    },
    series: data.map((item, index) => ({ ...item, visible: index >= data.length - seriesToShow }))
  }), [data, height, seriesToShow, title, unit]);

  return (
    <ChartRequest title={title} height={height} status={status} retry={retry} borderRadius="28px" overflow="hidden">
      <HighchartsReact highcharts={Highcharts} options={options} />
    </ChartRequest>
  );
};

export default LineSeriesChart;
