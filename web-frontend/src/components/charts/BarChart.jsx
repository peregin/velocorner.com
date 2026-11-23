import { useMemo } from 'react';
import Highcharts from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import 'highcharts/modules/accessibility';
import ChartRequest, { useChartData } from './ChartRequest';

const toBars = response => (response?.series ?? response ?? []).flatMap(item => {
  const value = item.series?.[0]?.value;
  return Number.isFinite(value) ? [{ name: item.name, y: value }] : [];
});

const BarChart = ({ title, unit, fetchSeries, height = 400 }) => {
  const { status, data, retry } = useChartData(fetchSeries, toBars);
  const options = useMemo(() => ({
    chart: {
      type: 'bar',
      height,
      zoomType: 'x',
      borderRadius: 20
    },
    accessibility: {
      point: { valueSuffix: unit ? ' ' + unit : '', valueDecimals: 0 }
    },
    title: {
      text: title
    },
    credits: {
      enabled: false
    },
    exporting: {
      enabled: false
    },
    xAxis: {
      categories: data.map(point => point.name)
    },
    yAxis: {
      min: 0,
      title: {
        text: unit,
        align: 'middle'
      },
      labels: {
        overflow: 'justify'
      }
    },
    plotOptions: {
      bar: {
        dataLabels: {
          enabled: true
        }
      }
    },
    legend: {
      layout: 'vertical',
      floating: true,
      backgroundColor: '#FFFFFF',
      align: 'right',
      verticalAlign: 'top',
      y: 60,
      x: -60
    },
    tooltip: {
      formatter: function() {
        return '<b>' + this.name + '</b>: ' + Highcharts.numberFormat(this.y, 0) + ' ' + unit;
      }
    },
    series: [{
      dataLabels: { format: '{point.y:.0f} ' + unit },
      showInLegend: false,
      colorByPoint: true,
      data
    }]
  }), [data, height, title, unit]);

  return (
    <ChartRequest title={title} height={height} status={status} retry={retry} borderRadius="28px" overflow="hidden">
      <HighchartsReact highcharts={Highcharts} options={options} />
    </ChartRequest>
  );
};

export default BarChart;
