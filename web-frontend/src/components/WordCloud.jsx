import Highcharts from 'highcharts';
import HighchartsReact from 'highcharts-react-official';
// needed the import to load the module
import Wordcloud from 'highcharts/modules/wordcloud';
import { Box } from "@chakra-ui/react";



const WordCloud = ({ words }) => {
    const wordOptions = {
        accessibility: {
            screenReaderSection: {
                beforeChartFormat: '<h5>{chartTitle}</h5>>' +
                    '<div>{chartSubtitle}</div>' +
                    '<div>{chartLongdesc}</div>' +
                    '<div>{viewTableButton}</div>'
            }
        },
        series: [{
            type: 'wordcloud',
            rotation: {
                from: 0,
                to: 0,
            },
            name: '#',
            minFontSize: 8,
            data: words
        }],
        title: {
            text: ''
        },
        exporting: {
            buttons: {
                contextButtons: {
                    enabled: false,
                    menuItems: null
                }
            },
            enabled: false
        },
        credits: {
            enabled: false
        },
        accessibility: {
            enabled: false
        }
    };

    return (
        <Box>
            <HighchartsReact
                highcharts={Highcharts}
                options={wordOptions}
            />
        </Box>
    );
};

export default WordCloud;
