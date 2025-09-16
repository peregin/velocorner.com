import React, { useEffect, useMemo, useState } from 'react';
import Chart from 'react-apexcharts';
import { Card, CardBody, Heading } from '@chakra-ui/react';

const BarChart = ({ title, unit, fetchSeries, height = 400 }) => {
  const [series, setSeries] = useState([]);
  const [categories, setCategories] = useState([]);

  const options = useMemo(() => ({
    chart: { type: 'bar', toolbar: { show: false } },
    plotOptions: { bar: { horizontal: true, dataLabels: { position: 'right' } } },
    dataLabels: { enabled: true, formatter: (v) => `${Math.round(v)} ${unit}` },
    xaxis: { categories },
    tooltip: { y: { formatter: (v) => `${Math.round(v)} ${unit}` } },
  }), [categories, unit]);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      const resp = await fetchSeries();
      // expected { status, series: [{ name, series: [{day, value}]}] }
      const bag = resp.series || resp;
      const data = bag.map(item => (item.series && item.series[0] && item.series[0].value) || 0);
      const names = bag.map(item => item.name);
      if (mounted) {
        setSeries([{ name: 'Years', data }]);
        setCategories(names);
      }
    };
    load();
    return () => { mounted = false };
  }, [fetchSeries]);

  return (
    <Card.Root>
      <Card.Body>
        <Heading size="md" mb={4}>{title}</Heading>
        <Chart options={options} series={series} type="bar" height={height} />
      </Card.Body>
    </Card.Root>
  );
};

export default BarChart;


