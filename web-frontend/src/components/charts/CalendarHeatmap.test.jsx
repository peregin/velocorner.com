import { render, screen, within } from '@testing-library/react';
import { expect, it } from 'vitest';
import { Provider } from '../ui/provider';
import CalendarHeatmap from './CalendarHeatmap';

it('lists daily distances for the same months shown in the calendar', async () => {
  const today = new Date();
  const monthStart = [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, '0'),
    '01',
  ].join('-');
  const fetchDaily = async () => [
    { day: '2000-01-01', value: 90 },
    { day: monthStart, value: 12.5 },
  ];

  render(
    <Provider>
      <CalendarHeatmap title="Activity calendar" fetchDaily={fetchDaily} unitName="mi" />
    </Provider>
  );

  const distances = await screen.findByRole('combobox', { name: 'Daily distances for Activity calendar' });
  const activeDay = within(distances).getByRole('option', { name: monthStart + ' · 12.50 mi' });
  expect(activeDay.value).toBe(monthStart);
  expect(within(distances).queryByRole('option', { name: /2000-01-01/ })).toBeNull();
});
