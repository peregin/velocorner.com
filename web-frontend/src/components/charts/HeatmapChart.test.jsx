import { render, waitFor } from '@testing-library/react';
import { expect, it, vi } from 'vitest';
import Highcharts from 'highcharts';
import { Provider } from '../ui/provider';
import HeatmapChart from './HeatmapChart';

vi.unmock('highcharts');
vi.unmock('highcharts/modules/heatmap');
vi.unmock('highcharts-react-official');

it.each(['array', 'wrapped'])('renders distribution counts from a %s response', async (shape) => {
  const rows = [
    { name: '2026', data: [{ x: '0–10 km', y: 0 }, { x: '10–50 km', y: 3 }] },
    { name: '2025', data: [{ x: '10–50 km', y: 2 }, { x: '0–10 km', y: null }] },
  ];
  const existingCharts = Highcharts.charts.length;
  render(
    <Provider>
      <HeatmapChart title="Distance distribution" fetchHeatmap={async () => shape === 'array' ? rows : { series: rows }} />
    </Provider>
  );

  await waitFor(() => {
    const chart = Highcharts.charts[existingCharts];
    expect(chart?.xAxis[0].categories).toEqual(['0–10 km', '10–50 km']);
    expect(chart.yAxis[0].categories).toEqual(['2026', '2025']);
    expect(chart.series[0].points.map(({ x, y, value }) => [x, y, value])).toEqual([
      [0, 0, 0], [1, 0, 3], [1, 1, 2],
    ]);
  });
});
