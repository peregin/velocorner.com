import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import { expect, it, vi } from 'vitest';
import Highcharts from 'highcharts';
import { Provider } from '../ui/provider';
import HeatmapChart from './HeatmapChart';

vi.unmock('highcharts');
vi.unmock('highcharts/modules/heatmap');
vi.unmock('highcharts/modules/accessibility');
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
    expect(screen.getByRole('region', { name: /Distance distribution/ })).toBeTruthy();
  });

  expect(screen.queryByText('View data', { exact: true })).toBeNull();
});

it('announces request states and retries only the failed chart', async () => {
  const distance = vi.fn()
    .mockRejectedValueOnce(new Error('Request failed'))
    .mockResolvedValueOnce([{ name: '2026', data: [{ x: '0–10 km', y: 2 }] }]);
  const elevation = vi.fn().mockResolvedValue([]);
  render(
    <Provider>
      <HeatmapChart title="Distance distribution" fetchHeatmap={distance} />
      <HeatmapChart title="Elevation distribution" fetchHeatmap={elevation} />
    </Provider>
  );
  const distanceChart = screen.getByRole('group', { name: 'Distance distribution' });
  const elevationChart = screen.getByRole('group', { name: 'Elevation distribution' });
  const distanceStatus = within(distanceChart).getByRole('status');
  const elevationStatus = within(elevationChart).getByRole('status');
  expect(distanceStatus.textContent).toBe('Loading chart data for Distance distribution.');

  const retry = await within(distanceChart).findByRole('button', { name: 'Retry loading Distance distribution' });
  expect(distanceStatus.textContent).toBe('Could not load chart data for Distance distribution.');
  expect(elevationStatus.textContent).toBe('No activities to chart for this selection.');
  expect(within(elevationChart).queryByRole('button')).toBeNull();

  retry.focus();
  fireEvent.click(retry);
  expect(document.activeElement).toBe(distanceChart);
  expect(distanceStatus.textContent).toBe('Loading chart data for Distance distribution.');
  await within(distanceChart).findByRole('region', { name: /Distance distribution/ });
  expect(distanceStatus.textContent).toBe('Chart data loaded for Distance distribution.');
  expect(distance).toHaveBeenCalledTimes(2);
  expect(elevation).toHaveBeenCalledTimes(1);
});

it('ignores a response for a previous activity selection', async () => {
  let resolvePrevious;
  const previousRequest = () => new Promise(resolve => { resolvePrevious = resolve; });
  const currentRequest = async () => [];
  const { rerender } = render(
    <Provider><HeatmapChart title="Distance distribution" fetchHeatmap={previousRequest} /></Provider>
  );
  rerender(
    <Provider><HeatmapChart title="Distance distribution" fetchHeatmap={currentRequest} /></Provider>
  );
  await screen.findByText('No activities to chart for this selection.');

  await act(async () => {
    resolvePrevious([{ name: '2026', data: [{ x: '0–10 km', y: 2 }] }]);
  });
  expect(screen.getByRole('status').textContent).toBe('No activities to chart for this selection.');
  expect(screen.queryByRole('region', { name: /Distance distribution/ })).toBeNull();
});
