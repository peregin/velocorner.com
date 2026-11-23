import { useMemo } from 'react';
import Highcharts from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
import 'highcharts/modules/wordcloud';
import 'highcharts/modules/accessibility';
import ChartRequest, { useChartData } from './ChartRequest';

const toWords = response => (response ?? []).filter(({ name, weight }) => name && Number.isFinite(weight));

const WordCloud = ({ title, fetchWords, height = 400 }) => {
  const { status, data, retry } = useChartData(fetchWords, toWords);
  const options = useMemo(() => ({
    chart: { height },
    title: { text: title },
    accessibility: {
      description: 'Repeated activity titles and their counts.',
      series: { pointDescriptionEnabledThreshold: false },
      point: { descriptionFormat: '{name}, {weight} activities.' },
    },
    series: [{
      type: 'wordcloud',
      rotation: { from: 0, to: 0 },
      minFontSize: 8,
      data,
    }],
    exporting: { enabled: false },
    credits: { enabled: false },
  }), [data, height, title]);

  return (
    <ChartRequest
      title={title} height={height} status={status} retry={retry}
      emptyMessage="No repeated activity titles yet." borderRadius="28px" overflow="hidden"
    >
      <HighchartsReact highcharts={Highcharts} options={options} />
    </ChartRequest>
  );
};

export default WordCloud;
