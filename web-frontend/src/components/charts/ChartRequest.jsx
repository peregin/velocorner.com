import { useEffect, useRef, useState } from 'react';
import { Box, Button, Heading, Text, VisuallyHidden } from '@chakra-ui/react';

export function useChartData(fetchData, normalize) {
  const [result, setResult] = useState({ status: 'loading', data: [] });
  const [attempt, setAttempt] = useState(0);

  useEffect(() => {
    let current = true;
    setResult({ status: 'loading', data: [] });
    const load = async () => {
      try {
        const response = await fetchData();
        if (current) {
          const data = normalize(response);
          setResult({ status: data.length ? 'ready' : 'empty', data });
        }
      } catch {
        if (current) setResult({ status: 'error', data: [] });
      }
    };
    load();
    return () => { current = false; };
  }, [attempt, fetchData, normalize]);

  return { ...result, retry: () => setAttempt(value => value + 1) };
}

export default function ChartRequest({
  title, height, status, retry, emptyMessage = 'No activities to chart for this selection.', children, ...boxProps
}) {
  const group = useRef(null);
  const message = {
    loading: `Loading chart data for ${title}.`,
    empty: emptyMessage,
    error: `Could not load chart data for ${title}.`,
  }[status];

  return (
    <Box {...boxProps} ref={group} role="group" aria-label={title} tabIndex={-1} minH={`${height}px`}>
      {status !== 'ready' && <Heading size="md" pt={4} px={4}>{title}</Heading>}
      <Box role="status">
        {status === 'ready'
          ? <VisuallyHidden>Chart data loaded for {title}.</VisuallyHidden>
          : <Text px={4} pt={4}>{message}</Text>}
      </Box>
      {status === 'error' && (
        <Button size="sm" mx={4} mt={3} aria-label={`Retry loading ${title}`} onClick={() => {
          group.current.focus();
          retry();
        }}>
          Retry
        </Button>
      )}
      {status === 'ready' && children}
    </Box>
  );
}
