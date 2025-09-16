import { useEffect, useMemo, useState } from 'react';
import { Box, Card, Heading, useBreakpointValue } from '@chakra-ui/react';

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
    <Card.Root>
      <Card.Body>
        <Heading size="md" mb={4}>{title}</Heading>
        <Box overflowX="auto">
          <svg width={monthsToShow * 140} height={140}>
            {points.map((m, mi) => (
              <g key={m.key} transform={`translate(${mi * 140}, 0)`}>
                <text x={0} y={12} fontSize="12" fill="#666">{m.label}</text>
                {m.days.map((d, di) => (
                  <rect key={di} x={(di % 7) * 18} y={20 + Math.floor(di / 7) * 18} width={16} height={16} fill={color(d.value)} rx={3} />
                ))}
              </g>
            ))}
          </svg>
        </Box>
      </Card.Body>
    </Card.Root>
  );
};

function useResponsiveMonths(maxMonths) {
  // rough responsivity similar to Play view
  const size = useBreakpointValue({ base: 'base', sm: 'sm', md: 'md', lg: 'lg', xl: 'xl' });
  if (size === 'base') return Math.min(1, maxMonths);
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
    for (let day = 1; day <= daysInMonth; day++) {
      const iso = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const value = byDate.get(iso) || 0;
      days.push({ date: iso, value });
    }
    months.push({ key, label, days });
  }
  return months;
}

function color(v) {
  if (v <= 0) return '#f0f0f0';
  if (v < 10) return '#c6e48b';
  if (v < 25) return '#7bc96f';
  if (v < 50) return '#239a3b';
  return '#196127';
}

export default CalendarHeatmap;


