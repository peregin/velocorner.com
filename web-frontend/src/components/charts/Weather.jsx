import { useState, useEffect, useRef } from 'react';
import {
  Button,
  Text,
  VStack,
  HStack,
  Progress,
  Box,
  Grid,
  GridItem,
  Icon,
  Flex,
  Stack
} from '@chakra-ui/react';
import AutocompleteCombobox from '../ui/AutocompleteCombobox.jsx';
import { HiRefresh } from "react-icons/hi";
import Highcharts from 'highcharts';
// needed the import to load the module
import HighchartsReact from 'highcharts-react-official';
import Datagrouping from 'highcharts/modules/datagrouping';
import Windbarb from 'highcharts/modules/windbarb';
import Patternfill from 'highcharts/modules/pattern-fill';
import Data from 'highcharts/modules/data';

const Weather = ({ defaultLocation = '' }) => {
  const [location, setLocation] = useState(defaultLocation);
  const [isLoading, setIsLoading] = useState(false);
  const [currentWeather, setCurrentWeather] = useState(null);
  const [forecastData, setForecastData] = useState(null);
  const [suggestions, setSuggestions] = useState([]);
  const chartRef = useRef(null);

  // Helper functions for cookie management
  const getCookie = (name) => {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
  };

  const setCookie = (name, value, days = 30) => {
    const expires = new Date();
    expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
    document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/`;
  };

  // Initialize weather on component mount
  useEffect(() => {
    if (!location && !defaultLocation) {
      const savedLocation = getCookie('weather_location');
      if (savedLocation) {
        setLocation(savedLocation);
        loadWeather(savedLocation);
      } else {
        detectLocation();
      }
    } else if (location || defaultLocation) {
      loadWeather(location || defaultLocation);
    }
  }, []);

  const detectLocation = async () => {
    try {
      const response = await fetch('https://weather.velocorner.com/location/ip');
      const data = await response.json();
      console.info(`location data is ${JSON.stringify(data)}`);
      const detectedLocation = `${data.city}, ${data.country}`;
      setLocation(detectedLocation);
      loadWeather(detectedLocation);
    } catch (error) {
      console.error('Failed to detect location:', error);
    }
  };

  const loadWeather = async (place) => {
    console.log(`load weather for ${place}`);
    if (!place) return;

    setIsLoading(true);
    try {
      // Load current weather
      const currentResponse = await fetch(`https://weather.velocorner.com/weather/current/${encodeURIComponent(place)}`);
      const currentData = await currentResponse.json();
      setCurrentWeather(currentData);

      // Load forecast for meteogram
      const forecastResponse = await fetch(`https://weather.velocorner.com/weather/forecast/${encodeURIComponent(place)}`);
      const forecastXml = await forecastResponse.text();
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(forecastXml, 'text/xml');
      setForecastData(xmlDoc);

      // Set cookie with the location after successful forecast retrieval
      setCookie('weather_location', place);

      // Create meteogram if we have forecast data
      if (xmlDoc) {
        createMeteogram(xmlDoc);
      }
    } catch (error) {
      console.error('Failed to load weather data:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const createMeteogram = (xml) => {
    const meteogram = new Meteogram(xml, chartRef.current);
  };

  const handleLocationSubmit = () => {
    const trimmedLocation = location.trim();
    if (!trimmedLocation) return;
    loadWeather(trimmedLocation);
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleLocationSubmit();
    }
  };

  const fetchSuggestions = async (query) => {
    if (!query || query.length < 2) {
      setSuggestions([]);
      return;
    }

    try {
      const response = await fetch(`https://weather.velocorner.com/location/suggest?query=${encodeURIComponent(query)}`);
      const data = await response.json();
      setSuggestions(data.suggestions || []);
    } catch (error) {
      console.error('Failed to fetch location suggestions:', error);
      setSuggestions([]);
    }
  };

  return (
    <Box p={2}>
      <Stack direction={{ base: 'row', sm: 'column' }} spacing={4} width="100%">
        <VStack spacing={2} align="stretch">
          {/* Location Input */}
          <HStack>
            <AutocompleteCombobox
              value={location}
              items={suggestions}
              placeholder="Enter location"
              width="300px"
              emptyMessage="No locations found"
              onInputValueChange={(val) => {
                setLocation(val);
                fetchSuggestions(val);
              }}
              onSelect={(selected) => {
                setLocation(selected);
                loadWeather(selected);
              }}
              onKeyPress={handleKeyPress}
            />
            <Button
              onClick={handleLocationSubmit}
              loading={isLoading}
              loadingText="Loading.."
              colorPalette="green"
            >
              <HiRefresh /> Weather
            </Button>
          </HStack>

          {/* Loading Progress */}
          <Box minH="20px" position="relative">
            <Progress.Root
              size='sm'
              colorPalette="blue"
              value={null}
              opacity={isLoading ? 1 : 0}
              position="absolute"
              width="100%"
              transition="opacity 0.2s ease-in-out"
            >
              <Progress.Track>
                <Progress.Range />
              </Progress.Track>
            </Progress.Root>
          </Box>

          {/* Current Weather Display */}
          <Grid templateColumns="repeat(2, 1fr)" gap={4}>
            <GridItem>
              <Box textAlign="center">
                <Text fontSize="2xl" fontWeight="bold">
                  {currentWeather?.info?.temp?.toFixed(1)}°C
                </Text>
                <Text fontSize="md">
                  {currentWeather?.current?.description}
                </Text>
                <Flex justify="center" mt={2}>
                  <Text fontSize="sm">Min: {currentWeather?.info?.temp_min?.toFixed(0)}°C</Text>
                  <Text fontSize="sm" ml={4}>Max: {currentWeather?.info?.temp_max?.toFixed(0)}°C</Text>
                </Flex>
              </Box>
            </GridItem>
            <GridItem>
              <Box textAlign="center">
                <Flex justify="center" align="center">
                  <Icon as="span" fontSize="2xl" mr={2}>🌅</Icon>
                  <Text fontSize="lg">{new Date(currentWeather?.sunriseSunset?.sunrise * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</Text>
                </Flex>
                <Flex justify="center" align="center" mt={2}>
                  <Icon as="span" fontSize="2xl" mr={2}>🌇</Icon>
                  <Text fontSize="lg">{new Date(currentWeather?.sunriseSunset?.sunset * 1000).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</Text>
                </Flex>
              </Box>
            </GridItem>
          </Grid>

          {/* Meteogram Chart */}
          <Box id="weather-container" height="250px" display={forecastData ? 'block' : 'none'}>
            <div ref={chartRef} style={{ width: '100%', height: '100%' }} />
          </Box>
        </VStack>

      </Stack>

    </Box>
  );
};

// Meteogram class adapted for React
class Meteogram {
  constructor(xml, container) {
    this.symbols = [];
    this.precipitations = [];
    this.precipitationsError = [];
    this.winds = [];
    this.temperatures = [];
    this.pressures = [];
    this.xml = xml;
    this.container = container;
    this.parseYrData();
  }

  smoothLine(data) {
    let i = data.length;
    let sum, value;

    while (i--) {
      data[i].value = value = data[i].y;
      sum = (data[i - 1] || data[i]).y + value + (data[i + 1] || data[i]).y;
      data[i].y = Math.max(value - 0.5, Math.min(sum / 3, value + 0.5));
    }
  }

  drawWeatherSymbols(chart) {
    const meteogram = this;
    chart.series[0].data.forEach((point, i) => {
      if (meteogram.resolution > 36e5 || i % 2 === 0) {
        chart.renderer
          .image(
            `https://openweathermap.org/img/w/${meteogram.symbols[i]}.png`,
            point.plotX + chart.plotLeft - 8,
            point.plotY + chart.plotTop - 30,
            30,
            30
          )
          .attr({ zIndex: 5 })
          .add();
      }
    });
  }

  drawBlocksForWindArrows(chart) {
    const xAxis = chart.xAxis[0];
    for (
      let pos = xAxis.min, max = xAxis.max, i = 0;
      pos <= max + 36e5; pos += 36e5,
      i += 1
    ) {
      // Get the X position
      const isLast = pos === max + 36e5,
        x = Math.round(xAxis.toPixels(pos)) + (isLast ? 0.5 : -0.5);

      // Draw the vertical dividers and ticks
      const isLong = this.resolution > 36e5 ?
        pos % this.resolution === 0 :
        i % 2 === 0;

      chart.renderer
        .path([
          'M', x, chart.plotTop + chart.plotHeight + (isLong ? 0 : 28),
          'L', x, chart.plotTop + chart.plotHeight + 32,
          'Z'
        ])
        .attr({
          stroke: chart.options.chart.plotBorderColor,
          'stroke-width': 1
        })
        .add();
    }
    // Center items in block
    chart.get('windbarbs').markerGroup.attr({
      translateX: chart.get('windbarbs').markerGroup.translateX + 8
    });

  };


  getTitle() {
    const location = this.xml.querySelector('location name');
    const country = this.xml.querySelector('location country');
    return `Forecast for ${location ? location.textContent : ''}, ${country ? country.textContent : ''}`;
  }

  getChartOptions() {
    const meteogram = this;
    return {
      chart: {
        renderTo: this.container,
        marginBottom: 70,
        marginRight: 40,
        marginTop: 70,
        plotBorderWidth: 1,
        height: 250,
        alignTicks: false,
        scrollablePlotArea: {
          minWidth: 720
        }
      },
      accessibility: {
        enabled: false
      },
      defs: {
        patterns: [{
          'id': 'precipitation-error',
          'path': {
            d: [
              'M', 3.3, 0, 'L', -6.7, 10,
              'M', 6.7, 0, 'L', -3.3, 10,
              'M', 10, 0, 'L', 0, 10,
              'M', 13.3, 0, 'L', 3.3, 10,
              'M', 16.7, 0, 'L', 6.7, 10
            ].join(' '),
            stroke: '#68CFE8',
            strokeWidth: 1
          }
        }]
      },
      title: {
        text: this.getTitle(),
        align: 'left',
        margin: 20,
        style: {
          whiteSpace: 'nowrap',
          textOverflow: 'ellipsis'
        }
      },
      exporting: {
        enabled: false
      },
      credits: {
        text: 'Forecast from <a href="http://velocorner.com">velocorner.com</a>',
        position: { x: -40 }
      },
      tooltip: {
        shared: true,
        useHTML: true,
        headerFormat:
          '<small>{point.x:%A, %b %e, %H:%M} - {point.point.to:%H:%M}</small><br>' +
          '<b>{point.point.symbolName}</b><br>'
      },
      xAxis: [{
        type: 'datetime',
        tickInterval: 2 * 36e5,
        minorTickInterval: 36e5,
        tickLength: 0,
        gridLineWidth: 1,
        gridLineColor: '#F0F0F0',
        startOnTick: false,
        endOnTick: false,
        minPadding: 0,
        maxPadding: 0,
        offset: 30,
        showLastLabel: true,
        labels: { format: '{value:%H}' },
        crosshair: true
      }, {
        linkedTo: 0,
        type: 'datetime',
        tickInterval: 24 * 3600 * 1000,
        labels: {
          format: '{value:<span style="font-size: 12px; font-weight: bold">%a</span> %b %e}',
          align: 'left',
          x: 3,
          y: -5
        },
        opposite: true,
        tickLength: 20,
        gridLineWidth: 1
      }],
      yAxis: [{
        title: { text: null },
        labels: {
          format: '{value}°',
          style: { fontSize: '10px' },
          x: -3
        },
        plotLines: [{
          value: 0,
          color: '#BBBBBB',
          width: 1,
          zIndex: 2
        }],
        maxPadding: 0.3,
        minRange: 8,
        tickInterval: 1,
        gridLineColor: '#F0F0F0'
      }, {
        title: { text: null },
        labels: { enabled: false },
        gridLineWidth: 0,
        tickLength: 0,
        minRange: 10,
        min: 0
      }, {
        allowDecimals: false,
        title: {
          text: 'hPa',
          offset: 0,
          align: 'high',
          rotation: 0,
          style: { fontSize: '10px' },
          textAlign: 'left',
          x: 3
        },
        labels: {
          style: { fontSize: '8px' },
          y: 2,
          x: 3
        },
        gridLineWidth: 0,
        opposite: true,
        showLastLabel: false
      }],
      legend: { enabled: false },
      plotOptions: {
        series: { pointPlacement: 'between' }
      },
      series: [{
        name: 'Temperature',
        data: this.temperatures,
        type: 'spline',
        marker: {
          enabled: false,
          states: { hover: { enabled: true } }
        },
        tooltip: {
          pointFormat: '<span style="color:{point.color}">\u25CF</span> ' +
            '{series.name}: <b>{point.value}°C</b><br/>'
        },
        zIndex: 1,
        color: '#FF3333',
        negativeColor: '#48AFE8'
      }, {
        name: 'Precipitation',
        data: this.precipitationsError,
        type: 'column',
        color: 'url(#precipitation-error)',
        yAxis: 1,
        groupPadding: 0,
        pointPadding: 0,
        tooltip: {
          valueSuffix: ' mm',
          pointFormat: '<span style="color:{point.color}">\u25CF</span> ' +
            '{series.name}: <b>{point.minvalue} mm - {point.maxvalue} mm</b><br/>'
        },
        grouping: false,
        dataLabels: {
          enabled: meteogram.hasPrecipitationError,
          formatter: function () {
            if (this.point.maxvalue > 0) {
              return this.point.maxvalue;
            }
          },
          style: { fontSize: '8px', color: 'gray' }
        }
      }, {
        name: 'Precipitation',
        data: this.precipitations,
        type: 'column',
        color: '#68CFE8',
        yAxis: 1,
        groupPadding: 0,
        pointPadding: 0,
        grouping: false,
        dataLabels: {
          enabled: !meteogram.hasPrecipitationError,
          formatter: function () {
            if (this.y > 0) {
              return this.y;
            }
          },
          style: { fontSize: '8px', color: 'gray' }
        },
        tooltip: { valueSuffix: ' mm' }
      }, {
        name: 'Air pressure',
        color: Highcharts.getOptions().colors[2],
        data: this.pressures,
        marker: { enabled: false },
        shadow: false,
        tooltip: { valueSuffix: ' hPa' },
        dashStyle: 'shortdot',
        yAxis: 2
      }, {
        name: 'Wind',
        type: 'windbarb',
        id: 'windbarbs',
        color: Highcharts.getOptions().colors[1],
        lineWidth: 1.5,
        data: this.winds,
        vectorLength: 18,
        yOffset: -15,
        tooltip: {
          valueSuffix: ' m/s'
        }
      }]
    };
  }

  onChartLoad(chart) {
    this.drawWeatherSymbols(chart);
    this.drawBlocksForWindArrows(chart);
  }

  createChart() {
    const meteogram = this;
    this.chart = Highcharts.chart(this.container, this.getChartOptions(), function (chart) {
      meteogram.onChartLoad(chart);
    });
  }

  error() {
    console.error('Failed loading data, please try again later');
  }

  parseYrData() {
    const meteogram = this;
    const xml = this.xml;
    const forecast = xml && xml.querySelector('forecast');

    if (!forecast) {
      return this.error();
    }

    let pointStart;
    Array.from(forecast.querySelectorAll('tabular time')).forEach((time, i) => {
      const from = time.getAttribute('from') + ' UTC';
      const to = time.getAttribute('to') + ' UTC';

      const fromTime = Date.parse(from.replace(/-/g, '/').replace('T', ' '));
      const toTime = Date.parse(to.replace(/-/g, '/').replace('T', ' '));

      if (i === 0) {
        pointStart = (fromTime + toTime) / 2;
        meteogram.resolution = toTime - fromTime;
      }

      if (pointStart && toTime > pointStart + 4 * 24 * 36e5) {
        return;
      }

      const symbol = time.querySelector('symbol');
      meteogram.symbols.push(
        symbol.getAttribute('var').match(/[0-9]{2}[dnm]?/)[0]
      );

      const temperature = time.querySelector('temperature');
      meteogram.temperatures.push({
        x: fromTime,
        y: parseInt(temperature.getAttribute('value'), 10),
        to: toTime,
        symbolName: symbol.getAttribute('name')
      });

      const precipitation = time.querySelector('precipitation');
      meteogram.precipitations.push({
        x: fromTime,
        y: parseFloat(
          precipitation.getAttribute('minvalue') || precipitation.getAttribute('value')
        )
      });

      if (precipitation.getAttribute('maxvalue')) {
        meteogram.hasPrecipitationError = true;
        meteogram.precipitationsError.push({
          x: fromTime,
          y: parseFloat(precipitation.getAttribute('maxvalue')),
          minvalue: parseFloat(precipitation.getAttribute('minvalue')),
          maxvalue: parseFloat(precipitation.getAttribute('maxvalue')),
          value: parseFloat(precipitation.getAttribute('value'))
        });
      }

      if (i % 2 === 0) {
        const windSpeed = time.querySelector('windSpeed');
        const windDirection = time.querySelector('windDirection');
        meteogram.winds.push({
          x: fromTime,
          value: parseFloat(windSpeed.getAttribute('mps')),
          direction: parseFloat(windDirection.getAttribute('deg'))
        });
      }

      const pressure = time.querySelector('pressure');
      meteogram.pressures.push({
        x: fromTime,
        y: parseFloat(pressure.getAttribute('value'))
      });
    });

    this.smoothLine(this.temperatures);
    this.createChart();
  }
}

export default Weather;
