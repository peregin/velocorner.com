import { Box, Heading, NativeSelect, useBreakpointValue } from '@chakra-ui/react';
import ChartRequest, { useChartData } from './ChartRequest';

const activeDays = data => (data ?? []).filter(({ value }) => Number.isFinite(value) && value > 0);

// Lightweight calendar heatmap using simple SVG rects for last N months
const CalendarHeatmap = ({ title, fetchDaily, unitName = 'km', maxMonths = 8 }) => {
  const { status, data, retry } = useChartData(fetchDaily, activeDays);
  const monthsToShow = useResponsiveMonths(maxMonths);
  const points = buildSeries(monthsToShow, new Map(data.map(({ day, value }) => [day, value])));
  const displayedDays = points.flatMap(month => month.days.filter(day => day.value > 0));
  const calendarStatus = status === 'ready' && !displayedDays.length ? 'empty' : status;

  return (
    <ChartRequest
      title={title} height={150} status={calendarStatus} retry={retry}
      emptyMessage="No recorded distance in the displayed months."
    >
      <Heading size="md" mb={4}>{title}</Heading>
      <Box overflowX="auto">
        <svg width={monthsToShow * 140} height={150} aria-hidden="true">
          {points.map((m, mi) => (
            <g key={m.key} transform={`translate(${mi * 140}, 0)`}>
              <text x={0} y={12} fontSize='12' fill="#666">{m.label}</text>
              {m.days.map((d, di) => (
                <g key={di}>
                  <rect
                    x={d.week * 18}
                    y={20 + d.weekday * 18}
                    width={16}
                    height={16}
                    fill={color(d.value)}
                    rx={3}
                  >
                    {d.value > 0 ? <title>{`${d.value.toFixed(2)} ${unitName}`}</title> : null}
                  </rect>
                  <g
                    transform={`translate(${d.week * 18 + 8}, ${20 + d.weekday * 18 + 11}) scale(0.6)`}
                    pointerEvents="none"
                  >
                    <text
                      x={0}
                      y={-2}
                      textAnchor="middle"
                      dominantBaseline="middle"
                      fontSize="8"
                      fill="#8a8a8a"
                    >
                      {d.date.slice(-2)}
                    </text>
                  </g>
                </g>
              ))}
            </g>
          ))}
        </svg>
      </Box>
      <NativeSelect.Root size="sm" maxW="20rem" mt={2}>
        <NativeSelect.Field aria-label={`Daily distances for ${title}`} placeholder="Browse daily distances">
          {displayedDays.map(day => (
            <option key={day.date} value={day.date}>{day.date} · {day.value.toFixed(2)} {unitName}</option>
          ))}
        </NativeSelect.Field>
        <NativeSelect.Indicator />
      </NativeSelect.Root>
    </ChartRequest>
  );
};

function useResponsiveMonths(maxMonths) {
  // rough responsivity similar to Play view
  const size = useBreakpointValue({ base: 'base', sm: 'sm', md: 'md', lg: 'lg', xl: 'xl' });
  if (size === 'base') return Math.min(2, maxMonths);
  if (size === 'sm') return Math.min(3, maxMonths);
  if (size === 'md') return Math.min(5, maxMonths);
  return maxMonths;
}

function buildSeries(monthsToShow, byDate) {
  const now = new Date();
  const months = [];
  for (let i = monthsToShow - 1; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
    const label = d.toLocaleString(undefined, { month: 'short', year: 'numeric' });
    const days = [];
    const daysInMonth = new Date(d.getFullYear(), d.getMonth() + 1, 0).getDate();
    const firstDayWeekday = toMondayFirstWeekdayIndex(d.getDay());
    for (let day = 1; day <= daysInMonth; day++) {
      const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const value = byDate.get(iso) || 0;
      const weekday = toMondayFirstWeekdayIndex(new Date(d.getFullYear(), d.getMonth(), day).getDay());
      const week = Math.floor((firstDayWeekday + day - 1) / 7);
      days.push({ date: iso, value, weekday, week });
    }
    months.push({ key, label, days });
  }
  return months;
}

function toMondayFirstWeekdayIndex(jsWeekday) {
  // JavaScript: Sunday=0..Saturday=6 -> Monday=0..Sunday=6
  return (jsWeekday + 6) % 7;
}

function color(v) {
  if (v <= 0) return '#f0f0f0';
  if (v < 10) return '#c6e48b';
  if (v < 25) return '#7bc96f';
  if (v < 50) return '#239a3b';
  return '#196127';
}

export default CalendarHeatmap;
