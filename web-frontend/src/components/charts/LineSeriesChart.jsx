import { useEffect, useMemo, useState } from 'react';
import Highcharts from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import { Box, Heading } from '@chakra-ui/react';

const LineSeriesChart = ({ title, unit, fetchSeries, seriesToShow = 2, height = 400 }) => {
  const [series, setSeries] = useState([]);
  const [loading, setLoading] = useState(true);

  const options = useMemo(() => ({
    chart: {
      type: 'spline',
      zoomType: 'x',
      backgroundColor: '#FFFADE',
      borderRadius: 20,
      height: height
    },
    accessibility: {
      enabled: false
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
    series: series
  }), [title, unit, height, series]);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      setLoading(true);
      try {
        const resp = await fetchSeries();
        // yearly endpoints return { status, series }, demo or other may return same
        const seriesList = resp.series || resp;
        const mapped = seriesList.map((s, ix) => ({
          name: s.name,
          data: (s.series || []).map(p => {
            const d = p.day; // yyyy-MM-dd
            const dateParts = d.split('-');
            // use fixed year to overlap
            const x = Date.UTC(2016, parseInt(dateParts[1]) - 1, parseInt(dateParts[2]));
            return [x, p.value];
          }),
          visible: ix >= seriesList.length - seriesToShow
        }));
        if (mounted) setSeries(mapped);
      } finally {
        if (mounted) setLoading(false);
      }
    };
    load();
    return () => { mounted = false };
  }, [fetchSeries, seriesToShow]);

  if (loading) {
    return (
      <Box>
        <Heading size="md" mb={4}>{title}</Heading>
        <div>Loading {title} Data...</div>
      </Box>
    );
  }

  return (
    <Box>
      <Heading size="md" mb={4}>{title}</Heading>
      <HighchartsReact highcharts={Highcharts} options={options} />
    </Box>
  );
};

export default LineSeriesChart;
