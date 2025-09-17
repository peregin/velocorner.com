import { useEffect, useState } from 'react';
import Highcharts from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import 'highcharts/highcharts-3d';
import { Box, Heading } from '@chakra-ui/react';

const BarChart = ({ title, unit, fetchSeries, height = 400 }) => {
  const [options, setOptions] = useState({
    chart: {
      type: 'bar',
      height,
      options3d: {
        enabled: false,
        alpha: 45,
      },
      zoomType: 'x',
      backgroundColor: '#FFFADE',
      borderRadius: 20
    },
    accessibility: {
      enabled: false
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
      categories: []
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
        return '<b>' + this.x + '</b>: ' + Highcharts.numberFormat(this.y, 0) + ' ' + unit;
      }
    },
    series: []
  });

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      const resp = await fetchSeries();
      // expected { status, series: [{ name, series: [{day, value}]}] }
      const bag = resp.series || resp;
      const data = bag.map(item => (item.series && item.series[0] && item.series[0].value) || 0);
      const names = bag.map(item => item.name);
      if (mounted) {
        setOptions(prev => ({
          ...prev,
          series: [{
            name: 'Years',
            dataLabels: {
              formatter: function() {
                return '<b>' + Highcharts.numberFormat(this.y, 0) + '</b> ' + unit;
              }
            },
            showInLegend: false,
            colorByPoint: true,
            data
          }],
          xAxis: {
            categories: names
          }
        }));
      }
    };
    load();
    return () => { mounted = false };
  }, [fetchSeries, unit]);

  return (
    <Box>
      <Heading size="md" mb={4}>{title}</Heading>
      <HighchartsReact highcharts={Highcharts} options={options} />
    </Box>
  );
};

export default BarChart;
