import { useEffect, useState } from 'react';
import { Box, Heading, useBreakpointValue } from '@chakra-ui/react';

// Lightweight calendar heatmap using simple SVG rects for last N months
const CalendarHeatmap = ({ title, fetchDaily, unitName = 'km', maxMonths = 8 }) => {
  const [points, setPoints] = useState([]);
  const monthsToShow = useResponsiveMonths(maxMonths);

  useEffect(() => {
    let mounted = true;
    const load = async () => {
      const data = await fetchDaily();
      // data is array of { day: yyyy-MM-dd, value }
      if (!Array.isArray(data)) return;
      const byDate = new Map(data.map(d => [d.day, d.value]));
      const series = buildSeries(monthsToShow, byDate);
      if (mounted) setPoints(series);
    };
    load();
    return () => { mounted = false };
  }, [fetchDaily, monthsToShow]);

  return (
    <Box>
      <Heading size="md" mb={4}>{title}</Heading>
      <Box overflowX="auto">
        <svg width={monthsToShow * 140} height={150}>
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
                      {String(new Date(d.date).getDate()).padStart(2, "0")}
                    </text>
                  </g>
                </g>
              ))}
            </g>
          ))}
        </svg>
      </Box>
    </Box>
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
